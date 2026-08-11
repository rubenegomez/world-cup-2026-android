
import java.text.SimpleDateFormat
import java.util.Locale

fun main() {
    val baseMatchDate = "2026-07-23 19:30"
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    try {
        val matchDate = sdf.parse(baseMatchDate)
        println("matchDate.time = ${matchDate?.time}")
        println("currentTimeMillis = ${System.currentTimeMillis()}")
        if (matchDate != null && System.currentTimeMillis() >= matchDate.time) {
            println("LIVE!")
        } else {
            println("NOT LIVE")
        }
    } catch (e: Exception) {
        println("Exception: ${e.message}")
    }
}

