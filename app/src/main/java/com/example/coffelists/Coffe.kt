package cz.g18.coffeelists
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
enum class RoastLevel (val czJmeno: String) {
    LIGHT("Světlé pražení"),
    MEDIUM_LIGHT("Středně světlé"),
    MEDIUM("Střední pražení"),
    MEDIUM_DARK("Středně tmavé"),
    DARK("Tmavé pražení");


    override fun toString(): String = czJmeno
}