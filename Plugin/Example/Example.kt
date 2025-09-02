import dev.marten_mrfcyt.achievements.api.AchievementAPI.register
import dev.marten_mrfcyt.achievements.api.AchievementAPI.setReloadHandler
import dev.marten_mrfcyt.achievements.builder.AchievementBuilder
import io.papermc.paper.advancement.AdvancementDisplay
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Consumer

/**
 * Example plugin of how to use the AchievementAPI for custom achievements.
 *
 *
 * This plugin shows the recommended code for:
 * - Registering reload handlers to make sure achievements don't get lost on
 * reloads
 * - Creating custom achievements in Java.
 * - Creating a family using achievements (:
 *
 * @author marten_mrfcyt
 */
class Example : JavaPlugin() {
    override fun onEnable() {
        logger.info("Setting up achievements with AchievementAPI...")
        setupAchievements()
    }

    /**
     * Initialize achievement integration with proper reload handling.
     *
     *
     * A simple method demonstrating how to set up achievements:
     * 1. Register a reload handler to re-register achievements when the system
     * reloads
     * 2. Register achievements initially
     */
    private fun setupAchievements() {
        try {
            // Register reload handler. Makes sure that achievements don't get lost on
            // reload.
            setReloadHandler(this.name, Runnable { this.registerCustomAchievements() })

            // Register achievements for the first time
            registerCustomAchievements()
            logger.info("Temporary plugin enabled with custom achievements registered!")
        } catch (e: Exception) {
            logger.severe("Error setting up achievements: " + e.message)
        }
    }

    /**
     * Register custom achievements using the Java Consumer API.
     *
     *
     * This method demonstrates how to:
     * - Use the Consumer-based builder pattern for Java compatibility
     * - Set achievement properties (title, description, icon, etc.)
     * - Create achievement hierarchies using parent relationships
     * - Configure toast notifications and server announcements
     */
    private fun registerCustomAchievements() {
        register("example", Consumer { builderConsumer: AchievementBuilder? ->
            builderConsumer!!.title("Examplé Acheevement")
            builderConsumer.description("Een very goed acheevement für testing.")
            builderConsumer.icon(Material.BEDROCK)
            builderConsumer.showToast(true)
            builderConsumer.frame(AdvancementDisplay.Frame.CHALLENGE)
            builderConsumer.parent("root")
        })
    }

    override fun onDisable() {
        logger.info("Temporary plugin disabled!")
    }
}
