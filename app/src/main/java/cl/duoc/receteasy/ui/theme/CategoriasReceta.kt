package cl.duoc.receteasy.ui.theme

import androidx.compose.ui.graphics.Color

object CategoriasReceta {
    val lista = listOf("Sopas", "Pastas", "Guarniciones", "Postres", "Otros")

    fun color(categoria: String): Color {
        return when (categoria) {
            "Sopas" -> Color(0xFFEF9A9A)
            "Pastas" -> Color(0xFFFFF176)
            "Guarniciones" -> Color(0xFFA5D6A7)
            "Postres" -> Color(0xFFCE93D8)
            else -> Color(0xFFB0BEC5)
        }
    }
}