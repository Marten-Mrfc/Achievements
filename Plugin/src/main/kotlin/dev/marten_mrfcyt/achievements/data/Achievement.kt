package dev.marten_mrfcyt.achievements.data

import io.papermc.paper.advancement.AdvancementDisplay
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * Represents a custom achievement that can be completed by players.
 * Use [requirements] to define when this achievement should be completed.
 * Complete achievements using [dev.marten_mrfcyt.achievements.api.AchievementAPI.complete].
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: Material = Material.GRASS_BLOCK,
    val showToast: Boolean = true,
    val frame: AdvancementDisplay.Frame = AdvancementDisplay.Frame.TASK,
    val background: String? = null,
    val parent: String? = null,
    val requirements: ((Player) -> Boolean)? = null,
    val hidden: Boolean = false
) {
    fun meetsRequirements(player: Player): Boolean {
        return requirements?.invoke(player) ?: true
    }
}
