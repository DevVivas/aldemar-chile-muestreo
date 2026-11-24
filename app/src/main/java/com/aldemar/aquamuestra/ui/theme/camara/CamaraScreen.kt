package com.aldemar.aquamuestra.ui.theme.camara

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.data.local.FotosMuestra
import kotlinx.coroutines.launch

/** Estado del permiso de cámara. */
private enum class Permiso { CONCEDIDO, PREGUNTAR, RECHAZADO, BLOQUEADO }

/**
 * Cámara con CameraX. Guarda la foto en el almacenamiento privado de la app (filesDir/fotos)
 * y devuelve su ruta con [onFotoLista]. Pensada para terreno: fondo oscuro, botones grandes.
 */
@Composable
fun CamaraScreen(
    muestraId: String,
    onFotoLista: (ruta: String) -> Unit,
    onCerrar: () -> Unit
) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    fun concedido() = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

    var permiso by remember { mutableStateOf(if (concedido()) Permiso.CONCEDIDO else Permiso.PREGUNTAR) }

    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        permiso = when {
            ok -> Permiso.CONCEDIDO
            // Si Android ya no deja volver a preguntar, solo queda ir a Ajustes
            activity != null && activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> Permiso.RECHAZADO
            else -> Permiso.BLOQUEADO
        }
    }

    // Al volver de Ajustes, revisar si ahora sí hay permiso
    LifecycleResumeEffect(Unit) {
        if (concedido()) permiso = Permiso.CONCEDIDO
        else if (permiso == Permiso.PREGUNTAR) pedirPermiso.launch(Manifest.permission.CAMERA)
        onPauseOrDispose { }
    }

    // La cámara siempre es oscura: el visor se ve mejor y no deslumbra en terreno.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (permiso) {
            Permiso.CONCEDIDO -> Camara(muestraId, onFotoLista, onCerrar)
            Permiso.PREGUNTAR -> Unit // el diálogo del sistema está a la vista
            Permiso.RECHAZADO, Permiso.BLOQUEADO -> SinPermiso(
                bloqueado = permiso == Permiso.BLOQUEADO,
                onPermitir = { pedirPermiso.launch(Manifest.permission.CAMERA) },
                onAbrirAjustes = {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                    )
                },
                onCerrar = onCerrar
            )
        }
    }
}

@Composable
private fun Camara(muestraId: String, onFotoLista: (String) -> Unit, onCerrar: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var flashEncendido by rememberSaveable { mutableStateOf(false) }
    var capturando by remember { mutableStateOf(false) }
    var hayCaptura by rememberSaveable { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    val destello = remember { Animatable(0f) } // feedback visual: destello blanco al disparar

    // LifecycleCameraController: CameraX se enciende y apaga solo con el ciclo de vida de la pantalla.
    val controller = remember {
        LifecycleCameraController(context).apply { setEnabledUseCases(CameraController.IMAGE_CAPTURE) }
    }
    DisposableEffect(lifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner)
        onDispose { controller.unbind() }
    }
    controller.imageCaptureFlashMode = if (flashEncendido) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF

    fun salir() {
        FotosMuestra.descartarCaptura(context, muestraId)
        onCerrar()
    }
    BackHandler { if (hayCaptura) { FotosMuestra.descartarCaptura(context, muestraId); hayCaptura = false } else salir() }

    fun disparar() {
        if (capturando) return
        capturando = true
        error = false
        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        scope.launch {
            destello.snapTo(0.8f)
            destello.animateTo(0f, tween(250))
        }
        val archivo = FotosMuestra.archivoCaptura(context, muestraId)
        controller.takePicture(
            ImageCapture.OutputFileOptions.Builder(archivo).build(),
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(resultado: ImageCapture.OutputFileResults) {
                    capturando = false
                    hayCaptura = true
                }

                override fun onError(e: ImageCaptureException) {
                    capturando = false
                    error = true
                }
            }
        )
    }

    if (hayCaptura) {
        VistaPrevia(
            muestraId = muestraId,
            onRepetir = {
                FotosMuestra.descartarCaptura(context, muestraId)
                hayCaptura = false
            },
            onUsar = { onFotoLista(FotosMuestra.confirmar(context, muestraId)) }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FIT_CENTER
                    this.controller = controller
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = destello.value))
        )

        // Barra superior: cerrar y flash
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .safeDrawingPadding()
                .padding(16.dp)
        ) {
            BotonRedondo(Icons.Filled.Close, stringResource(R.string.cam_cerrar), onClick = ::salir)
            BotonRedondo(
                icono = if (flashEncendido) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                descripcion = stringResource(if (flashEncendido) R.string.cam_flash_encendido else R.string.cam_flash_apagado),
                activo = flashEncendido,
                onClick = { flashEncendido = !flashEncendido }
            )
        }

        // Parte inferior: ayuda y obturador
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .safeDrawingPadding()
                .padding(bottom = 24.dp)
        ) {
            Surface(color = Color.Black.copy(alpha = 0.6f), contentColor = Color.White, shape = RoundedCornerShape(50)) {
                Text(
                    text = stringResource(if (error) R.string.cam_error_captura else R.string.cam_ayuda),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Obturador(capturando = capturando, onClick = ::disparar)
        }
    }
}

/** Obturador grande (88 dp): se acierta con guantes y sin mirar. */
@Composable
private fun Obturador(capturando: Boolean, onClick: () -> Unit) {
    val descripcion = stringResource(R.string.cam_obturador)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(88.dp)
            .border(4.dp, Color.White, CircleShape)
            .padding(8.dp)
    ) {
        FilledIconButton(
            onClick = onClick,
            enabled = !capturando,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.White,
                contentColor = Color.Black,
                disabledContainerColor = Color.White.copy(alpha = 0.6f)
            ),
            modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = descripcion }
        ) {
            if (capturando) {
                CircularProgressIndicator(color = Color.Black, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
            } else {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
private fun BotonRedondo(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    descripcion: String,
    onClick: () -> Unit,
    activo: Boolean = false
) {
    FilledIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (activo) Color(0xFFFFC400) else Color.Black.copy(alpha = 0.55f),
            contentColor = if (activo) Color.Black else Color.White
        ),
        modifier = Modifier.size(56.dp)
    ) {
        Icon(icono, contentDescription = descripcion, modifier = Modifier.size(28.dp))
    }
}

/** Revisión de la foto antes de usarla. */
@Composable
private fun VistaPrevia(muestraId: String, onRepetir: () -> Unit, onUsar: () -> Unit) {
    val context = LocalContext.current
    val archivo = remember { FotosMuestra.archivoCaptura(context, muestraId) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context).data(archivo).memoryCacheKey("captura-${archivo.lastModified()}").build(),
            contentDescription = stringResource(R.string.cam_vista_previa),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onRepetir,
                border = BorderStroke(2.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
            ) { Text(stringResource(R.string.cam_repetir), style = MaterialTheme.typography.titleMedium) }
            Button(
                onClick = onUsar,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
            ) { Text(stringResource(R.string.cam_usar), style = MaterialTheme.typography.titleMedium) }
        }
    }
}

/** Sin permiso: se explica para qué sirve la cámara y cómo activarla. */
@Composable
private fun SinPermiso(bloqueado: Boolean, onPermitir: () -> Unit, onAbrirAjustes: () -> Unit, onCerrar: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Box(modifier = Modifier.padding(16.dp)) {
            BotonRedondo(Icons.Filled.Close, stringResource(R.string.cam_cerrar), onClick = onCerrar)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {
            Icon(Icons.Filled.NoPhotography, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(20.dp))
            Text(
                stringResource(R.string.cam_permiso_titulo),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(if (bloqueado) R.string.cam_permiso_bloqueado else R.string.cam_permiso_texto),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = if (bloqueado) onAbrirAjustes else onPermitir,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    stringResource(if (bloqueado) R.string.cam_permiso_ajustes else R.string.cam_permiso_permitir),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
