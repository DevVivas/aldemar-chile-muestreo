package com.aldemar.aquamuestra.ui.theme.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.viewmodel.BannerSync
import kotlinx.coroutines.delay

/** Cuánto se ve "Todo sincronizado" antes de esconderse. */
private const val DURACION_OK_MS = 2_000L

/**
 * Banner de sincronización (Inicio e Historial).
 *  - Aparece y desaparece con una animación corta (expandir + fundido).
 *  - Al cambiar de estado, el contenido se funde en el nuevo, sin saltos.
 *  - Cuando termina de sincronizar muestra "Todo sincronizado" 2 segundos y se esconde.
 *  - TalkBack lee el cambio de texto (liveRegion).
 */
@Composable
fun BannerSincronizacion(
    estado: BannerSync,
    onSincronizar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrarOk by remember { mutableStateOf(false) }
    var anterior by remember { mutableStateOf<BannerSync>(BannerSync.Oculto) }

    // Solo se celebra el fin de una sincronización real (venía sincronizando o con pendientes).
    LaunchedEffect(estado) {
        val veniaSincronizando = anterior is BannerSync.Sincronizando || anterior is BannerSync.Pendientes
        anterior = estado
        if (estado is BannerSync.Oculto && veniaSincronizando) {
            mostrarOk = true
            delay(DURACION_OK_MS)
            mostrarOk = false
        } else {
            mostrarOk = false
        }
    }

    AnimatedVisibility(
        visible = estado !is BannerSync.Oculto || mostrarOk,
        enter = expandVertically(tween(220)) + fadeIn(tween(220)),
        exit = shrinkVertically(tween(200)) + fadeOut(tween(160)),
        modifier = modifier
    ) {
        AnimatedContent(
            targetState = estado,
            contentKey = { it::class }, // anima al cambiar de tipo, no cada vez que cambia el número
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "banner-sync"
        ) { actual ->
            when (actual) {
                is BannerSync.SinConexion -> FilaBanner(
                    fondo = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contenido = MaterialTheme.colorScheme.onSurface,
                    icono = Icons.Filled.CloudOff,
                    texto = if (actual.pendientes > 0) {
                        pluralStringResource(R.plurals.sync_sin_conexion, actual.pendientes, actual.pendientes)
                    } else {
                        stringResource(R.string.sync_sin_conexion_cero)
                    }
                )

                is BannerSync.Sincronizando -> FilaBanner(
                    fondo = MaterialTheme.colorScheme.secondaryContainer,
                    contenido = MaterialTheme.colorScheme.onSecondaryContainer,
                    cargando = true,
                    texto = if (actual.pendientes > 0) {
                        pluralStringResource(R.plurals.sync_sincronizando, actual.pendientes, actual.pendientes)
                    } else {
                        stringResource(R.string.sync_actualizando)
                    }
                )

                is BannerSync.Pendientes -> FilaBanner(
                    fondo = MaterialTheme.colorScheme.tertiaryContainer,
                    contenido = MaterialTheme.colorScheme.onTertiaryContainer,
                    icono = Icons.Filled.CloudUpload,
                    texto = pluralStringResource(R.plurals.sync_pendientes, actual.pendientes, actual.pendientes),
                    accion = stringResource(R.string.sync_sincronizar_ahora),
                    onAccion = onSincronizar
                )

                // Oculto: solo se llega aquí mientras se muestra "Todo sincronizado"
                BannerSync.Oculto -> FilaBanner(
                    fondo = MaterialTheme.colorScheme.secondaryContainer,
                    contenido = MaterialTheme.colorScheme.onSecondaryContainer,
                    icono = Icons.Filled.CheckCircle,
                    texto = stringResource(R.string.sync_todo_ok)
                )
            }
        }
    }
}

@Composable
private fun FilaBanner(
    fondo: Color,
    contenido: Color,
    texto: String,
    icono: ImageVector? = null,
    cargando: Boolean = false,
    accion: String? = null,
    onAccion: () -> Unit = {}
) {
    Surface(
        color = fondo,
        contentColor = contenido,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .padding(start = 14.dp, end = if (accion != null) 4.dp else 14.dp, top = 6.dp, bottom = 6.dp)
        ) {
            if (cargando) {
                CircularProgressIndicator(strokeWidth = 2.dp, color = contenido, modifier = Modifier.size(18.dp))
            } else if (icono != null) {
                Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
            if (accion != null) {
                TextButton(onClick = onAccion) { Text(accion, color = contenido) }
            }
        }
    }
}
