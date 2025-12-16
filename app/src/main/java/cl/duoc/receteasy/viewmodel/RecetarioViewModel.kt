package cl.duoc.receteasy.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.receteasy.data.model.Ingrediente
import cl.duoc.receteasy.data.model.Receta
import cl.duoc.receteasy.data.remote.IngredienteXanoRequest
import cl.duoc.receteasy.data.remote.RecetaXanoRequest
import cl.duoc.receteasy.data.remote.RetrofitInstance
import cl.duoc.receteasy.repository.RecetaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

data class EstadoUI(
    val nombreUsuario: String? = null,
    val recetas: List<Receta> = emptyList(),
    val consulta: String = "",
    val categoriaSeleccionada: String? = null,
    val cargando: Boolean = false,
    val error: String? = null
)

class RecetarioViewModel(private val repositorio: RecetaRepository) : ViewModel() {

    private val _estadoUI = MutableStateFlow(EstadoUI())
    val estadoUI: StateFlow<EstadoUI> = _estadoUI.asStateFlow()

    private val _ingredientesTemp = mutableStateListOf<Ingrediente>()
    val ingredientesTemp: List<Ingrediente> get() = _ingredientesTemp

    private var recetasTotales: List<Receta> = emptyList()

    init {
        viewModelScope.launch {
            repositorio.todasLasRecetas()
                .onStart {
                    _estadoUI.value = _estadoUI.value.copy(cargando = true)
                }
                .catch { e ->
                    _estadoUI.value = _estadoUI.value.copy(error = e.message, cargando = false)
                }
                .collect { lista ->
                    recetasTotales = lista
                    aplicarFiltros()
                }
        }
    }

    fun iniciarSesion(nombre: String) {
        _estadoUI.value = _estadoUI.value.copy(nombreUsuario = nombre)
    }

    fun cerrarSesion() {
        _estadoUI.value = _estadoUI.value.copy(nombreUsuario = null)
    }

    fun setConsulta(q: String) {
        _estadoUI.value = _estadoUI.value.copy(consulta = q)
        aplicarFiltros()
    }

    fun setCategoria(categoria: String?) {
        _estadoUI.value = _estadoUI.value.copy(categoriaSeleccionada = categoria)
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val texto = _estadoUI.value.consulta.trim()
        val categoria = _estadoUI.value.categoriaSeleccionada

        var filtradas = recetasTotales

        if (categoria != null) {
            filtradas = filtradas.filter { it.categoria == categoria }
        }

        if (texto.isNotBlank()) {
            val ingredientesBuscados = texto.lowercase()
                .split(",", " ", ";")
                .map { it.trim() }
                .filter { it.isNotBlank() }

            filtradas = filtradas.filter { receta ->
                val ing = receta.ingredientes.map { it.nombre.lowercase() }
                ingredientesBuscados.all { buscado ->
                    ing.any { it.contains(buscado) }
                }
            }
        }

        _estadoUI.value = _estadoUI.value.copy(recetas = filtradas, cargando = false)
    }

    fun crearReceta(
        titulo: String,
        descripcion: String,
        pasos: String,
        imagenUri: Uri?,
        categoria: String,
        creador: String?
    ): Boolean {
        if (titulo.isBlank()) {
            setError("El título no puede estar vacío.")
            return false
        }
        if (_ingredientesTemp.isEmpty()) {
            setError("Debe agregar al menos un ingrediente.")
            return false
        }

        limpiarError()

        viewModelScope.launch {
            val receta = Receta(
                titulo = titulo.trim(),
                descripcion = descripcion.trim(),
                ingredientes = _ingredientesTemp.toList(),
                pasos = pasos.trim(),
                imagenUri = imagenUri?.toString() ?: "default_receta.png",
                categoria = categoria,
                creador = creador
            )

            repositorio.insertar(receta)
            limpiarIngredientesTemp()
            enviarRecetaAXano(receta)
        }

        return true
    }

    suspend fun obtenerRecetaPorId(id: Long): Receta? {
        return repositorio.buscarPorId(id)
    }

    fun setError(mensaje: String) {
        _estadoUI.value = _estadoUI.value.copy(error = mensaje)
    }

    fun limpiarError() {
        _estadoUI.value = _estadoUI.value.copy(error = null)
    }

    fun agregarIngrediente(nombre: String, cantidad: Double, unidad: String) {
        val limpio = nombre.trim().lowercase()
        if (_ingredientesTemp.any { it.nombre == limpio }) return

        val ingrediente = Ingrediente(
            nombre = limpio,
            cantidad = cantidad,
            unidad = unidad
        )

        _ingredientesTemp.add(ingrediente)
        enviarIngredienteAXano(ingrediente)
    }

    fun limpiarIngredientesTemp() {
        _ingredientesTemp.clear()
    }

    private fun enviarRecetaAXano(receta: Receta) {
        viewModelScope.launch {
            try {
                val request = RecetaXanoRequest(
                    titulo = receta.titulo,
                    descripcion = receta.descripcion,
                    ingredientes = receta.ingredientes.size,
                    pasos = receta.pasos,
                    imagenUri = receta.imagenUri,
                    creador = receta.creador
                )

                val response = RetrofitInstance.api.crearRecetaXano(request)

                if (!response.isSuccessful) {
                    setError("Error Xano receta: ${response.code()}")
                }
            } catch (e: Exception) {
                setError("Error Xano receta: ${e.message}")
            }
        }
    }


    private fun enviarIngredienteAXano(ingrediente: Ingrediente) {
        viewModelScope.launch {
            try {
                val request = IngredienteXanoRequest(
                    nombre = ingrediente.nombre
                )
                RetrofitInstance.api.crearIngredienteXano(request)
            } catch (e: Exception) {
                setError("Error al enviar ingrediente a Xano: ${e.message}")
            }
        }
    }
}