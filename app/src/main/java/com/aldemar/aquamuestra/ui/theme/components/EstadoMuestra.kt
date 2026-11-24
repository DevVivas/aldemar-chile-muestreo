package com.aldemar.aquamuestra.ui.theme.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.data.local.EstadoRevision

// Componentes compartidos por el historial y el detalle para mostrar el estado de una muestra.

/** Colores e icono de cada estado (los usa el chip y el gráfico de estados del dashboard). */
data class EstiloEstado(val fondo: Color, val contenido: Color, val icono: ImageVector)

@Composable
fun estiloEstado(estado: EstadoRevision): EstiloEstado {
    val c = MaterialTheme.colorScheme
    return when (estado) {
        EstadoRevision.BORRADOR -> EstiloEstado(c.surfaceContainerHighest, c.onSurfaceVariant, Icons.Filled.Edit)
        EstadoRevision.ENVIADA -> EstiloEstado(c.primaryContainer, c.onPrimaryContainer, Icons.AutoMirrored.Filled.Send)
        EstadoRevision.OBSERVADA -> EstiloEstado(c.tertiaryContainer, c.onTertiaryContainer, Icons.Filled.Flag)
        EstadoRevision.VALIDADA -> EstiloEstado(c.secondaryContainer, c.onSecondaryContainer, Icons.Filled.CheckCircle)
    }
}

@Composable
fun textoEstado(estado: EstadoRevision): String = stringResource(
    when (estado) {
        EstadoRevision.BORRADOR -> R.string.hist_estado_borrador
        EstadoRevision.ENVIADA -> R.string.hist_estado_enviada
        EstadoRevision.OBSERVADA -> R.string.hist_estado_observada
        EstadoRevision.VALIDADA -> R.string.hist_estado_validada
    }
)

/** Chip de estado (no interactivo): color + icono + texto, para no depender solo del color. */
@Composable
fun ChipEstado(estado: EstadoRevision) {
    val estilo = estiloEstado(estado)
    Surface(color = estilo.fondo, contentColor = estilo.contenido, shape = RoundedCornerShape(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp)
        ) {
            Icon(estilo.icono, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(textoEstado(estado), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun EtiquetaPendiente() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.CloudUpload,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            stringResource(R.string.hist_pendiente_sync),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}
