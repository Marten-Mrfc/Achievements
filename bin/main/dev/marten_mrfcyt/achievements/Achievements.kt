package dev.marten_mrfcyt.achievements

import dev.marten_mrfcyt.achievements.api.AchievementAPI
import dev.marten_mrfcyt.achievements.commands.achievementsCommand
import io.papermc.paper.advancement.AdvancementDisplay
import mlib.api.architecture.KotlinPlugin
import org.bukkit.Material

/**
 * Main plugin class for the Achievements system.
 * Provides base achievements and integrates with the AchievementAPI.
 */
class Achievements : KotlinPlugin() {
    companion object {
        lateinit var instance: Achievements
    }

    override fun onEnable() {
        logger.info("Achievements plugin enabled!")
        super.onEnable()
        instance = this
        AchievementAPI.initialize(this)
        AchievementAPI.setReloadHandler("Achievements") {
            registerBaseAchievements()
            achievementsCommand()
        }
        registerBaseAchievements()
        logger.info("Loaded ${AchievementAPI.getAchievementCount()} achievements")
        achievementsCommand()
    }

    override fun onDisable() {
        logger.info("Achievements plugin disabled!")
    }

    private fun registerBaseAchievements() {
        // Root achievement
        AchievementAPI.register("root") {
            title("Kingdom Achievements")
            description("Speel en behaal de Kingdom Achievements!")
            icon(Material.DIAMOND_SWORD)
            showToast(false)
            frame(AdvancementDisplay.Frame.TASK)
            background("husbandry")
            requirements { true }
        }

        // First join achievement
        AchievementAPI.register("first_join") {
            title("Welkom!")
            description("Speel op Kingdom voor het eerst!")
            icon(Material.GRASS_BLOCK)
            showToast(true)
            frame(AdvancementDisplay.Frame.TASK)
            parent("root")
            requirements { true }
        }
    }
}