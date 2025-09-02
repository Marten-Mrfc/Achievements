package dev.marten_mrfcyt.achievements.commands

import com.mojang.brigadier.arguments.StringArgumentType.string
import dev.marten_mrfcyt.achievements.api.AchievementAPI
import mlib.api.commands.builders.LiteralDSLBuilder
import mlib.api.commands.builders.reloadableCommand
import mlib.api.utilities.error
import mlib.api.utilities.message
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/**
 * Command interface for achievement management.
 * Uses reloadable commands to support hot-reloading during development.
 */
fun Plugin.achievementsCommand() = reloadableCommand("achievements") {
    requiresPermissions("achievements.use")
    alias("ach", "achievement")
    setup()
    executes {
        plugin.logger.info("Main achievements command executed by ${source.name}")
        source.message("<gold>Achievement Commands:")
        source.message("  <yellow>/achievements list <gray>- List all achievements")
        if (source is Player) {
            source.message("  <yellow>/achievements progress <gray>- Show your progress")
            source.message("  <yellow>/achievements complete <id> <gray>- Complete an achievement")
        }
        if (source.hasPermission("achievements.edit")) {
            source.message("  <yellow>/achievements edit <id> <property> <value> <gray>- Edit achievement")
        }
        if (source.hasPermission("achievements.reload")) {
            source.message("  <yellow>/achievements reload <gray>- Reload achievements")
        }
    }
}

private fun LiteralDSLBuilder.setup() {
    literal("complete") {
        requiresPermissions("achievements.use")

        argument("achievement_id", string()) {
            suggests { builder ->
                AchievementAPI.getAllAchievements().keys.forEach { builder.suggest(it) }
                builder.build()
            }

            executes {
                if (source !is Player) {
                    source.error("Only players can complete achievements!")
                    return@executes
                }

                val player = source as Player
                val achievementId = getArgument<String>("achievement_id")
                val success = AchievementAPI.complete(player, achievementId)

                if (!success) {
                    val achievement = AchievementAPI.getAchievement(achievementId)
                    when {
                        achievement == null -> {
                            player.error("Achievement '$achievementId' does not exist!")
                        }
                        AchievementAPI.hasCompleted(player, achievementId) -> {
                            player.message("<yellow>You have already completed this achievement!")
                        }
                        else -> {
                            player.error("You don't meet the requirements for this achievement!")
                        }
                    }
                }
            }
        }
    }

    literal("uncomplete") {
        requiresPermissions("achievements.use")

        argument("achievement_id", string()) {
            suggests { builder ->
                AchievementAPI.getAllAchievements().keys.forEach { builder.suggest(it) }
                builder.build()
            }

            executes {
                if (source !is Player) {
                    source.error("Only players can uncomplete achievements!")
                    return@executes
                }

                val player = source as Player
                val achievementId = getArgument<String>("achievement_id")
                val success = AchievementAPI.unComplete(player, achievementId)

                if (!success) {
                    player.error("You have not completed this achievement or it does not exist!")
                } else {
                    player.message("<green>Successfully uncompleted achievement: $achievementId")
                }
            }
        }
    }

    literal("reload") {
        requiresPermissions("achievements.reload")

        executes {
            plugin.logger.info("Reload command executed by ${source.name}")
            try {
                AchievementAPI.reload()
                val count = AchievementAPI.getAchievementCount()
                source.message("<green>Successfully reloaded achievements! Loaded: $count")
            } catch (e: Exception) {
                source.error("Error during reload: ${e.message}")
                plugin.logger.warning("Reload error: ${e.message}")
            }
        }
    }

    literal("list") {
        requiresPermissions("achievements.use")

        executes {
            val achievements = AchievementAPI.getAllAchievements()
            if (achievements.isEmpty()) {
                source.message("<yellow>No achievements registered!")
                return@executes
            }

            source.message("<gold>Registered Achievements:")
            achievements.forEach { (id, achievement) ->
                val completed = if (source is Player) {
                    if (AchievementAPI.hasCompleted(source as Player, id)) "<green>✓" else "<red>✗"
                } else {
                    ""
                }
                source.message("  $completed <yellow>$id<gray>: <white>${achievement.title}")
            }
        }
    }
}
