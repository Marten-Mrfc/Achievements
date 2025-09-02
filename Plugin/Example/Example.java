import dev.marten_mrfcyt.achievements.api.AchievementAPI;
import io.papermc.paper.advancement.AdvancementDisplay;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Example plugin of how to use the AchievementAPI for custom achievements.
 * <p>
 * This plugin shows the recommended code for:
 * - Registering reload handlers to make sure achievements don't get lost on
 * reloads
 * - Creating custom achievements in Java.
 * - Creating a family using achievements (:
 *
 * @author marten_mrfcyt
 */
public final class Temporary extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("Setting up achievements with AchievementAPI...");
        setupAchievements();
    }

    /**
     * Initialize achievement integration with proper reload handling.
     * <p>
     * A simple method demonstrating how to set up achievements:
     * 1. Register a reload handler to re-register achievements when the system
     * reloads
     * 2. Register achievements initially
     */
    private void setupAchievements() {
        try {
            // Register reload handler. Makes sure that achievements don't get lost on
            // reload.
            AchievementAPI.setReloadHandler(this.getName(), this::registerCustomAchievements);

            // Register achievements for the first time
            registerCustomAchievements();
            getLogger().info("Temporary plugin enabled with custom achievements registered!");
        } catch (Exception e) {
            getLogger().severe("Error setting up achievements: " + e.getMessage());
        }
    }

    /**
     * Register custom achievements using the Java Consumer API.
     * <p>
     * This method demonstrates how to:
     * - Use the Consumer-based builder pattern for Java compatibility
     * - Set achievement properties (title, description, icon, etc.)
     * - Create achievement hierarchies using parent relationships
     * - Configure toast notifications and server announcements
     */
    private void registerCustomAchievements() {
        AchievementAPI.register("example", builderConsumer -> {
        builderConsumer.title("Examplé Acheevement");
        builderConsumer.description("Een very goed acheevement für testing.");
        builderConsumer.icon(Material.BEDROCK);
        builderConsumer.showToast(true);
        builderConsumer.frame(AdvancementDisplay.Frame.CHALLENGE);
        builderConsumer.parent("root");
    });
    }

    @Override
    public void onDisable() {
        getLogger().info("Temporary plugin disabled!");
    }
}
