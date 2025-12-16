package cl.duoc.receteasy.ui.pantalla

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import cl.duoc.receteasy.R
import cl.duoc.receteasy.viewmodel.UsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPerfilUsuario(
    navController: NavController,
    usuarioViewModel: UsuarioViewModel
) {
    val usuario = usuarioViewModel.obtenerUsuarioLogueado()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (usuario == null) {
                Text(text = "No hay usuario logueado")
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val painter =
                        if (usuario.fotoUri.isNullOrBlank() || usuario.fotoUri == "default_user.png") {
                            painterResource(R.drawable.default_user)
                        } else {
                            rememberAsyncImagePainter(Uri.parse(usuario.fotoUri))
                        }

                    Image(
                        painter = painter,
                        contentDescription = "Foto usuario",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = usuario.nombre,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}
