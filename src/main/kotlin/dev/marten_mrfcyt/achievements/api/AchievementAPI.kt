@file:Suppress("unused")
package dev.marten_mrfcyt.achievements.api

import dev.marten_mrfcyt.achievements.builder.AchievementBuilder
import dev.marten_mrfcyt.achievements.builder.achievement
import dev.marten_mrfcyt.achievements.data.Achievement
import dev.marten_mrfcyt.achievements.manager.AdvancementManager
import mlib.api.utilities.asMini
import mlib.api.utilities.message
import mlib.api.utilities.debug
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Central API for managing custom achievements in Minecraft plugins.
 * 
 * Call [initialize] with your plugin instance before using any other methods.
 */
object AchievementAPI {

    private val achievements = ConcurrentHashMap<String, Achievement>()
    private val playerProgress = ConcurrentHashMap<UUID, MutableSet<String>>()
    private val reloadHandlers = ConcurrentHashMap<String, () -> Unit>()

    private var pluginInstance: JavaPlugin? = null
    private var advancementManager: AdvancementManager? = null

    /**
     * Initialize the achievement system with your plugin.
     * Must be called before using any other API methods.
     */
    @JvmStatic
    fun initialize(plugin: JavaPlugin) {
        if (pluginInstance != null) {
            debug("AchievementAPI already initialized with plugin: ${pluginInstance?.name}, ignoring initialization request from: ${plugin.name}")
            return
        }
        pluginInstance = plugin
        advancementManager = AdvancementManager(plugin)
        debug("AchievementAPI initialized with plugin: ${plugin.name}")
    }

    @JvmStatic
    fun register(achievement: Achievement) {
        val plugin = pluginInstance ?: throw IllegalStateException("AchievementAPI must be initialized with a plugin instance")
        val manager = advancementManager ?: throw IllegalStateException("AdvancementManager not initialized")

        if (achievements.containsKey(achievement.id)) {
            throw IllegalArgumentException("Achievement with ID '${achievement.id}' already exists")
        }

        achievements[achievement.id] = achievement

        try {
            manager.registerAchievement(achievement)
            plugin.logger.info("Registered achievement: ${achievement.id}")
        } catch (e: Exception) {
            plugin.logger.warning("Failed to register Minecraft advancement for ${achievement.id}: ${e.message}")
        }
    }

    /**
     * Register an achievement using Kotlin DSL syntax.
     * Prefer this over the Java builder when writing Kotlin code.
     */
    fun register(id: String, block: AchievementBuilder.() -> Unit) {
        val achievement = achievement {
            id(id)
            block()
        }
        register(achievement)
    }

    /**
     * Register an achievement using Java Consumer pattern.
     * Use this when calling from Java code.
     */
    @JvmStatic
    fun register(id: String, builderAction: java.util.function.Consumer<AchievementBuilder>) {
        val builder = AchievementBuilder().id(id)
        builderAction.accept(builder)
        register(builder.build())
    }

    /**
     * Complete an achievement for a player if they meet the requirements.
     * Returns true if the achievement was newly completed.
     */
    @JvmStatic
    fun complete(player: Player, achievementId: String): Boolean {
        val achievement = achievements[achievementId] ?: return false

        val playerAchievements = playerProgress.getOrPut(player.uniqueId) { ConcurrentHashMap.newKeySet() }

        if (playerAchievements.contains(achievementId)) {
            return false
        }

        if (!achievement.meetsRequirements(player)) {
            return false
        }

        playerAchievements.add(achievementId)

        try {
            val command = "advancement grant ${player.name} only achievements:${achievement.id.lowercase()}"
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
        } catch (e: Exception) {
            pluginInstance?.logger?.warning("Failed to grant advancement for ${achievement.id}: ${e.message}")
        }

        debug("Player ${player.name} completed achievement: $achievementId")
        return true
    }

    /**
     * Remove an achievement completion from a player.
     * Use this when you need to revoke an achievement.
     */
    @JvmStatic
    fun unComplete(player: Player, achievementId: String): Boolean {
        val playerAchievements = playerProgress[player.uniqueId] ?: return false

        if (!playerAchievements.remove(achievementId)) {
            return false
        }

        try {
            val command = "advancement revoke ${player.name} only achievements:${achievementId.lowercase()}"
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
        } catch (e: Exception) {
            pluginInstance?.logger?.warning("Failed to revoke advancement for $achievementId: ${e.message}")
        }

        debug("Player ${player.name} uncompleted achievement: $achievementId")
        return true
    }

    @JvmStatic
    fun getAchievement(id: String): Achievement? = achievements[id]

    @JvmStatic
    fun getAllAchievements(): Map<String, Achievement> = achievements.toMap()

    @JvmStatic
    fun getCompletedAchievements(player: Player): Set<String> {
        return playerProgress[player.uniqueId]?.toSet() ?: emptySet()
    }

    @JvmStatic
    fun getCompletedAchievements(playerId: UUID): Set<String> {
        return playerProgress[playerId]?.toSet() ?: emptySet()
    }

    @JvmStatic
    fun hasCompleted(player: Player, achievementId: String): Boolean {
        return playerProgress[player.uniqueId]?.contains(achievementId) ?: false
    }

    @JvmStatic
    fun hasCompleted(playerId: UUID, achievementId: String): Boolean {
        return playerProgress[playerId]?.contains(achievementId) ?: false
    }

    /**
     * Register your plugin's achievements to be reloaded when the system reloads.
     * Call this during your plugin's onEnable to ensure achievements persist through reloads.
     */
    @JvmStatic
    fun setReloadLambda(plugin: JavaPlugin, reloadAction: () -> Unit) {
        setReloadHandler(plugin.name, reloadAction)
    }

    /**
     * Register your plugin's achievements to be reloaded when the system reloads.
     * Java-friendly version using Runnable.
     */
    @JvmStatic
    fun setReloadLambda(plugin: JavaPlugin, reloadAction: Runnable) {
        setReloadHandler(plugin.name) { reloadAction.run() }
    }

    @JvmStatic
    fun setReloadHandler(pluginName: String, reloadAction: () -> Unit) {
        reloadHandlers[pluginName] = reloadAction
        debug("Registered reload handler for plugin: $pluginName")
    }

    @JvmStatic
    fun setReloadHandler(pluginName: String, reloadAction: Runnable) {
        setReloadHandler(pluginName) { reloadAction.run() }
    }

    /**
     * Reload all achievements and call registered reload handlers.
     * Use this when you need to refresh the achievement system.
     */
    @JvmStatic
    fun reload() {
        val manager = advancementManager

        manager?.clearAllAchievements()
        achievements.clear()

        reloadHandlers.forEach { (pluginName, handler) ->
            try {
                debug("Calling reload handler for plugin: $pluginName")
                handler.invoke()
            } catch (e: Exception) {
                pluginInstance?.logger?.warning("Error in reload handler for plugin $pluginName: ${e.message}")
            }
        }

        manager?.reloadDatapack()
        debug("Reloaded achievement system. Total achievements: ${achievements.size}")
    }

    /**
     * Clear all achievements and player progress.
     * Use this for complete system reset - cannot be undone.
     */
    @JvmStatic
    fun clear() {
        val manager = advancementManager

        manager?.clearAllAchievements()
        achievements.clear()
        playerProgress.clear()
        manager?.reloadDatapack()

        debug("Cleared all achievements and player progress")
    }

    @JvmStatic
    fun getAchievementCount(): Int = achievements.size

    @JvmStatic
    fun getPlayerCount(): Int = playerProgress.size

    @JvmStatic
    fun getAdvancementManager(): AdvancementManager? = advancementManager
}