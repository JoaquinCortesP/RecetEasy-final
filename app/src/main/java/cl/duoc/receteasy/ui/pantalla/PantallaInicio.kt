package cl.duoc.receteasy.ui.pantalla

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import cl.duoc.receteasy.R
import cl.duoc.receteasy.data.model.Receta
import cl.duoc.receteasy.ui.navegacion.Rutas
import cl.duoc.receteasy.ui.theme.CategoriasReceta
import cl.duoc.receteasy.viewmodel.RecetarioViewModel
import cl.duoc.receteasy.viewmodel.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel,
    recetarioViewModel: RecetarioViewModel
) {
    val estado by recetarioViewModel.estadoUI.collectAsState()
    val usuario = usuarioViewModel.obtenerUsuarioLogueado()
    var consulta by remember { mutableStateOf(estado.consulta) }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text(text = "RecetEasy") },
                actions = {
                    IconButton(onClick = { navController.navigate(Rutas.CREAR) }) {
                        Icon(Icons.Default.Add, contentDescription = "Crear")
                    }

                    val avatarPainter =
                        if (usuario?.fotoUri.isNullOrBlank()) {
                            painterResource(R.drawable.default_user)
                        } else {
                            rememberAsyncImagePainter(Uri.parse(usuario!!.fotoUri!!))
                        }

                    IconButton(onClick = { }) {
                        Image(
                            painter = avatarPainter,
                            contentDescription = "Foto usuario",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ) {
            TextButton(
                onClick = {
                    usuarioViewModel.cerrarSesion()
                    recetarioViewModel.cerrarSesion()
                    navController.navigate(Rutas.BIENVENIDA) {
                        popUpTo(Rutas.INICIO) { inclusive = true }
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Cerrar sesión")
            }

            Button(
                onClick = { navController.navigate(Rutas.INGREDIENTES_XANO) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text("Ver ingredientes desde Xano")
            }

            OutlinedTextField(
                value = consulta,
                onValueChange = {
                    consulta = it
                    recetarioViewModel.setConsulta(it)
                },
                label = { Text("Buscar por título o ingrediente (ej: ajo, sal)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = estado.categoriaSeleccionada == null,
                    onClick = { recetarioViewModel.setCategoria(null) },
                    label = { Text("Todas") },
                    colors = FilterChipDefaults.filterChipColors()
                )

                CategoriasReceta.lista.forEach { cat ->
                    val color = CategoriasReceta.color(cat)
                    FilterChip(
                        selected = estado.categoriaSeleccionada == cat,
                        onClick = { recetarioViewModel.setCategoria(cat) },
                        label = { Text(cat) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(color = color, shape = CircleShape)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            AnimatedVisibility(
                visible = estado.cargando,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(estado.recetas) { receta ->
                    FilaReceta(receta = receta, onClick = {
                        navController.navigate("${Rutas.DETALLE_BASE}/${receta.id}")
                    })
                }
            }
        }
    }
}

@Composable
fun FilaReceta(receta: Receta, onClick: () -> Unit) {
    val colorCategoria = CategoriasReceta.color(receta.categoria)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(72.dp)
                    .background(colorCategoria)
            )

            Spacer(modifier = Modifier.width(8.dp))

            val painter =
                if (receta.imagenUri.isNullOrBlank() || receta.imagenUri == "default_receta.png") {
                    painterResource(R.drawable.default_receta)
                } else {
                    rememberAsyncImagePainter(model = receta.imagenUri)
                }

            Image(
                painter = painter,
                contentDescription = "Imagen receta",
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = receta.titulo, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = receta.categoria,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorCategoria
                )

                Spacer(modifier = Modifier.height(4.dp))

                val ingredientesText = receta.ingredientes
                    .joinToString(separator = ", ") { ing ->
                        val unidad = ing.unidad?.takeIf { it.isNotBlank() } ?: ""
                        val textoCantidad = ing.cantidad.toString()
                        val nombre = ing.nombre
                        "$nombre $textoCantidad${if (unidad.isNotEmpty()) " $unidad" else ""}"
                    }

                Text(
                    text = ingredientesText.ifBlank { "Sin ingredientes" },
                    maxLines = 2,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
