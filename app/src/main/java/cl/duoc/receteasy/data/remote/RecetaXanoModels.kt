package cl.duoc.receteasy.data.remote

import com.google.gson.annotations.SerializedName

data class IngredienteXanoRequest(
    @SerializedName("name")
    val nombre: String
)

data class RecetaXanoRequest(
    val titulo: String,
    val descripcion: String,
    val ingredientes: Int,
    val pasos: String,
    val imagenUri: String?,
    val creador: String?
)
