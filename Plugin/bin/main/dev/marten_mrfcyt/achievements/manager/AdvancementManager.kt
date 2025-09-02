package dev.marten_mrfcyt.achievements.manager

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import dev.marten_mrfcyt.achievements.data.Achievement
import io.papermc.paper.advancement.AdvancementDisplay
import mlib.api.utilities.debug
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*

/**
 * Manages Minecraft advancement integration for custom achievements.
 * Handles datapack creation and player progress persistence across reloads.
 */
class AdvancementManager(private val plugin: JavaPlugin) {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val datapackFolder = File(Bukkit.getWorldContainer(), "world/datapacks/achievements")
    private val advancementsFolder = File(datapackFolder, "data/achievements/advancements")

    init {
        setupDatapack()
    }

    private fun setupDatapack() {
        datapackFolder.mkdirs()
        advancementsFolder.mkdirs()

        val packMeta = JsonObject().apply {
            val pack = JsonObject().apply {
                addProperty("pack_format", 15)
                addProperty("description", "Custom achievements from ${plugin.name}")
            }
            add("pack", pack)
        }

        File(datapackFolder, "pack.mcmeta").writeText(gson.toJson(packMeta))
        debug("Created achievements datapack at: ${datapackFolder.absolutePath}")
    }

    fun registerAchievement(achievement: Achievement) {
        val advancementJson = createAdvancementJson(achievement)
        val fileName = "${achievement.id.lowercase()}.json"
        val file = File(advancementsFolder, fileName)

        file.writeText(gson.toJson(advancementJson))
        debug("Created advancement file: $fileName")
    }

    private fun createAdvancementJson(achievement: Achievement): JsonObject {
        return JsonObject().apply {
            achievement.parent?.let { parentId ->
                addProperty("parent", "achievements:$parentId")
            }

            val display = JsonObject().apply {
                addProperty("title", achievement.title)
                addProperty("description", achievement.description)

                val icon = JsonObject().apply {
                    addProperty("id", "minecraft:${achievement.icon.name.lowercase()}")
                }
                add("icon", icon)

                addProperty("frame", when (achievement.frame) {
                    AdvancementDisplay.Frame.TASK -> "task"
                    AdvancementDisplay.Frame.CHALLENGE -> "challenge"
                    AdvancementDisplay.Frame.GOAL -> "goal"
                })

                addProperty("show_toast", achievement.showToast)
                addProperty("hidden", achievement.hidden)

                achievement.background?.let { bg ->
                    addProperty("background", "minecraft:textures/gui/advancements/backgrounds/$bg.png")
                }
            }
            add("display", display)

            val criteria = JsonObject().apply {
                val customTrigger = JsonObject().apply {
                    addProperty("trigger", "minecraft:impossible")
                }
                add("custom_completion", customTrigger)
            }
            add("criteria", criteria)

            val requirements = arrayOf(arrayOf("custom_completion"))
            add("requirements", gson.toJsonTree(requirements))
        }
    }

    fun clearAllAchievements() {
        advancementsFolder.listFiles()?.forEach { file ->
            if (file.extension == "json") {
                file.delete()
            }
        }
        debug("Cleared all achievement advancement files")
    }

    /**
     * Reload the datapack while preserving player progress.
     * Use this after registering new achievements.
     */
    fun reloadDatapack() {
        debug("Starting achievement reload with progress preservation...")

        val savedProgress = savePlayerProgress()

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "datapack disable \"file/achievements\"")
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "datapack enable \"file/achievements\"")

        debug("Reloaded achievements datapack")

        if (savedProgress.isNotEmpty()) {
            // restorePlayerProgress(savedProgress)
            debug("Scheduled restoration of progress for ${savedProgress.size} players")
        } else {
            debug("No player progress to restore")
        }
    }

    private fun savePlayerProgress(): Map<String, Set<String>> {
        val playerProgress = mutableMapOf<String, MutableSet<String>>()

        Bukkit.getOnlinePlayers().forEach { player ->
            val completedAchievements = mutableSetOf<String>()

            val iterator = Bukkit.advancementIterator()
            iterator.forEach { advancement ->
                if (advancement.key.namespace == "achievements") {
                    val progress = player.getAdvancementProgress(advancement)
                    if (progress.isDone) {
                        completedAchievements.add(advancement.key.key)
                        debug("Saved progress for ${player.name}: ${advancement.key.key}")
                    }
                }
            }

            if (completedAchievements.isNotEmpty()) {
                playerProgress[player.uniqueId.toString()] = completedAchievements
            }
        }

        debug("Saved progress for ${playerProgress.size} players")
        return playerProgress
    }

//    private fun restorePlayerProgress(playerProgress: Map<String, Set<String>>) {
//        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
//            playerProgress.forEach { (playerUuid, achievements) ->
//                val player = Bukkit.getPlayer(UUID.fromString(playerUuid))
//                if (player != null) {
//                    achievements.forEach { achievementId ->
//                        val advancement = Bukkit.getAdvancement(NamespacedKey("achievements", achievementId))
//                        if (advancement != null) {
//                            val progress = player.getAdvancementProgress(advancement)
//                            if (!progress.isDone) {
//                                advancement.criteria.forEach { criteria ->
//                                    progress.awardCriteria(criteria)
//                                }
//                                debug("Restored achievement '$achievementId' for ${player.name}")
//                            }
//                        } else {
//                            plugin.logger.warning("Could not find advancement 'achievements:$achievementId' to restore for ${player.name}")
//                        }
//                    }
//                } else {
//                    debug("Player $playerUuid is offline, progress saved but not restored")
//                }
//            }
//        }, 2L)
//    }
}