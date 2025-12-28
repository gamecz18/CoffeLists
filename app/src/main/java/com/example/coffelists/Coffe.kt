package cz.g18.coffeelists

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable

@Serializable
data class Coffee(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val roastLevel: RoastLevel? = null,
    val notes: String,
    val imagePath: String? = null,
    val grindLevel: Float? = null,
    var weightInGrams: Float? = null,
    var weighOut: Float? = null
)

@Serializable
enum class RoastLevel(@StringRes val displayNameRes: Int) {
    LIGHT(R.string.roast_light),
    MEDIUM_LIGHT(R.string.roast_medium_light),
    MEDIUM(R.string.roast_medium),
    MEDIUM_DARK(R.string.roast_medium_dark),
    DARK(R.string.roast_dark)
}
