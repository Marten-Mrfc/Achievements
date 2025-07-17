package dev.marten_mrfcyt.achievements.builder

import dev.marten_mrfcyt.achievements.data.Achievement
import io.papermc.paper.advancement.AdvancementDisplay
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * Builder for creating achievements with a fluent API.
 * Use this when building achievements step by step, or prefer the [achievement] DSL function.
 */
class AchievementBuilder {
    private var id: String = ""
    private var title: String = ""
    private var description: String = ""
    private var icon: Material = Material.GRASS_BLOCK
    private var showToast: Boolean = true
    private var frame: AdvancementDisplay.Frame = AdvancementDisplay.Frame.TASK
    private var background: String? = null
    private var parent: String? = null
    private var requirements: ((Player) -> Boolean)? = null
    private var hidden: Boolean = false

    fun id(id: String): AchievementBuilder = apply { this.id = id }
    fun title(title: String): AchievementBuilder = apply { this.title = title }
    fun description(description: String): AchievementBuilder = apply { this.description = description }
    fun icon(icon: Material): AchievementBuilder = apply { this.icon = icon }
    fun showToast(showToast: Boolean): AchievementBuilder = apply { this.showToast = showToast }
    fun frame(frame: AdvancementDisplay.Frame): AchievementBuilder = apply { this.frame = frame }
    fun background(background: String?): AchievementBuilder = apply { this.background = background }
    fun parent(parent: String?): AchievementBuilder = apply { this.parent = parent }
    fun requirements(requirements: (Player) -> Boolean): AchievementBuilder = apply { this.requirements = requirements }

    fun build(): Achievement {
        require(id.isNotBlank()) { "Achievement ID cannot be blank" }
        require(title.isNotBlank()) { "Achievement title cannot be blank" }
        require(description.isNotBlank()) { "Achievement description cannot be blank" }

        return Achievement(
            id = id,
            title = title,
            description = description,
            icon = icon,
            showToast = showToast,
            frame = frame,
            background = background,
            parent = parent,
            requirements = requirements,
            hidden = hidden
        )
    }
}

/**
 * Create an achievement using Kotlin DSL syntax.
 * Prefer this over the builder when writing Kotlin code.
 */
fun achievement(block: AchievementBuilder.() -> Unit): Achievement {
    return AchievementBuilder().apply(block).build()
}