package cl.duoc.receteasy.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("ingrediente")
    suspend fun obtenerIngredientes(): List<IngredienteRemote>
    @POST("ingrediente")
    suspend fun crearIngredienteXano(@Body ingrediente: IngredienteXanoRequest)
    @POST("receta")
    suspend fun crearRecetaXano(@Body receta: RecetaXanoRequest)
}

