package com.aldemar.aquamuestra.ui.theme.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.data.local.CentroUbicacion
import com.aldemar.aquamuestra.data.remote.marine.CondicionesMarinas
import com.aldemar.aquamuestra.viewmodel.EstadoMarino
import com.aldemar.aquamuestra.viewmodel.MarineUiState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val LOCALE_CL: Locale = Locale.forLanguageTag("es-CL")

/**
 * Tarjeta "Condiciones marinas" con datos de la API EXTERNA Open-Meteo Marine.
 * Siempre deja claro de dónde viene el dato, de cuándo es y que es una estimación.
 */
@Composable
fun TarjetaCondicionesMarinas(
    state: MarineUiState,
    onCentro: (CentroUbicacion) -> Unit,
    onRecargar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado = state.estado
    // Datos a mostrar: los frescos o, si no hay conexión / hubo error, los del cache
    val datos: CondicionesMarinas? = when (estado) {
        is EstadoMarino.Ok -> estado.datos
        is EstadoMarino.SinConexion -> estado.cache
        is EstadoMarino.Error -> estado.cache
        EstadoMarino.Cargando -> null
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // ---- Título + actualizar
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp, end = 4.dp)) {
                Icon(Icons.Filled.Waves, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Text(
                    stringResource(R.string.mar_titulo),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() }
                )
                IconButton(onClick = onRecargar, enabled = estado !is EstadoMarino.Cargando) {
                    Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.mar_actualizar))
                }
            }

            // ---- Selector de centro
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                state.centros.forEach { centro ->
                    val cd = stringResource(R.string.mar_centro_cd, centro.nombre)
                    FilterChip(
                        selected = centro == state.centro,
                        onClick = { if (centro != state.centro) onCentro(centro) },
                        label = { Text(centro.nombreCorto) },
                        modifier = Modifier.semantics { contentDescription = cd }
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // ---- Aviso de estado (sin conexión / error)
                when (estado) {
                    is EstadoMarino.SinConexion -> Aviso(
                        icono = Icons.Filled.CloudOff,
                        texto = estado.cache?.let { stringResource(R.string.mar_sin_conexion_cache, horaCorta(it.consultadoEn)) }
                            ?: stringResource(R.string.mar_sin_conexion_vacio),
                        error = false,
                        onReintentar = onRecargar
                    )
                    is EstadoMarino.Error -> Aviso(
                        icono = Icons.Filled.ErrorOutline,
                        texto = estado.cache?.let { stringResource(R.string.mar_error_cache, horaCorta(it.consultadoEn)) }
                            ?: stringResource(R.string.mar_error),
                        error = true,
                        onReintentar = onRecargar
                    )
                    else -> Unit
                }

                // ---- Valores
                when {
                    estado is EstadoMarino.Cargando -> Esqueleto()
                    datos != null -> Valores(datos)
                }

                // ---- Siempre visible: de cuándo es, dónde, qué es y de dónde viene
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        stringResource(R.string.mar_dato_de, datos?.horaDato?.let(::fechaDato) ?: "—"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        stringResource(R.string.mar_coordenadas, coordenadas(state.centro)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        stringResource(R.string.mar_leyenda),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        stringResource(R.string.mar_fuente),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** Las cuatro cifras en una grilla 2 × 2. */
@Composable
private fun Valores(d: CondicionesMarinas) {
    val sinDato = stringResource(R.string.mar_sin_dato)
    val ola = d.alturaOlaM?.let { decimal(it, 2) } ?: sinDato
    val periodo = d.periodoOlaS?.let { decimal(it, 1) } ?: sinDato
    val desde = d.direccionOlaGrados?.let { cardinal(it) } ?: sinDato
    val temp = d.temperaturaMarC?.let { decimal(it, 1) } ?: sinDato
    val corriente = d.corrienteKmh?.let { decimal(it, 1) } ?: sinDato
    val hacia = d.direccionCorrienteGrados?.let { cardinal(it) } ?: sinDato

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Celda(
                etiqueta = stringResource(R.string.mar_ola),
                valor = if (d.alturaOlaM != null) "$ola m" else sinDato,
                detalle = stringResource(R.string.mar_periodo, periodo),
                descripcion = stringResource(R.string.mar_ola_cd, ola, periodo),
                icono = Icons.Filled.Waves,
                modifier = Modifier.weight(1f)
            )
            // Las olas se informan "desde" dónde vienen: la flecha apunta hacia donde van (+180°)
            Celda(
                etiqueta = stringResource(R.string.mar_direccion),
                valor = d.direccionOlaGrados?.let { "${it.roundToInt()}°" } ?: sinDato,
                detalle = stringResource(R.string.mar_desde, desde),
                descripcion = stringResource(R.string.mar_direccion_cd, desde, d.direccionOlaGrados?.roundToInt()?.toString() ?: sinDato),
                icono = Icons.Filled.North,
                rotacion = d.direccionOlaGrados?.let { (it + 180f).toFloat() },
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Celda(
                etiqueta = stringResource(R.string.mar_temperatura),
                valor = if (d.temperaturaMarC != null) "$temp °C" else sinDato,
                detalle = null,
                descripcion = stringResource(R.string.mar_temperatura_cd, temp),
                icono = Icons.Filled.Thermostat,
                modifier = Modifier.weight(1f)
            )
            // La corriente se informa "hacia" dónde va: la flecha usa el ángulo tal cual
            Celda(
                etiqueta = stringResource(R.string.mar_corriente),
                valor = if (d.corrienteKmh != null) "$corriente km/h" else sinDato,
                detalle = stringResource(R.string.mar_hacia, hacia),
                descripcion = stringResource(R.string.mar_corriente_cd, corriente, hacia),
                icono = Icons.Filled.North,
                rotacion = d.direccionCorrienteGrados?.toFloat(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Celda(
    etiqueta: String,
    valor: String,
    detalle: String?,
    descripcion: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    rotacion: Float? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.clearAndSetSemantics { contentDescription = descripcion }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .then(if (rotacion != null) Modifier.rotate(rotacion) else Modifier)
                )
                Spacer(Modifier.width(6.dp))
                Text(valor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }
            if (detalle != null) {
                Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Indicador de carga con forma de las celdas (shimmer simple: brillo que sube y baja). */
@Composable
private fun Esqueleto() {
    val descripcion = stringResource(R.string.mar_cargando)
    val brillo by rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "brillo"
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.semantics { contentDescription = descripcion }
    ) {
        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(84.dp)
                            .alpha(brillo)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(14.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun Aviso(icono: ImageVector, texto: String, error: Boolean, onReintentar: () -> Unit) {
    val fondo: Color = if (error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHighest
    val contenido: Color = if (error) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
    Surface(color = fondo, contentColor = contenido, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .heightIn(min = 48.dp)
                .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
        ) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                texto,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
            TextButton(onClick = onReintentar) { Text(stringResource(R.string.mar_reintentar), color = contenido) }
        }
    }
}

// ------------------------------------------------------------------------------------- Formato

private fun decimal(valor: Double, decimales: Int, minimo: Int = 0): String =
    NumberFormat.getNumberInstance(LOCALE_CL).apply {
        maximumFractionDigits = decimales
        minimumFractionDigits = minimo
    }.format(valor)

/** Punto cardinal de 16 direcciones, en español (O = oeste). */
private fun cardinal(grados: Double): String {
    val puntos = listOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSO", "SO", "OSO", "O", "ONO", "NO", "NNO")
    val i = (((grados % 360 + 360) % 360) / 22.5).roundToInt() % 16
    return puntos[i]
}

/** "41,79° S, 73,13° O" */
private fun coordenadas(c: CentroUbicacion): String {
    val lat = "${decimal(abs(c.latitud), 2, 2)}° ${if (c.latitud < 0) "S" else "N"}"
    val lon = "${decimal(abs(c.longitud), 2, 2)}° ${if (c.longitud < 0) "O" else "E"}"
    return "$lat, $lon"
}

/** "2026-11-24T01:45" (hora local de Open-Meteo) → "24 nov, 01:45" */
private fun fechaDato(iso: String): String = try {
    val fecha = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).parse(iso)
    SimpleDateFormat("d MMM, HH:mm", LOCALE_CL).format(fecha!!)
} catch (e: Exception) {
    iso
}

/** Hora de consulta: "14:05" si es de hoy; "23 nov, 14:05" si es de otro día. */
private fun horaCorta(millis: Long): String {
    val hoy = Calendar.getInstance()
    val dia = Calendar.getInstance().apply { timeInMillis = millis }
    val mismoDia = hoy.get(Calendar.YEAR) == dia.get(Calendar.YEAR) && hoy.get(Calendar.DAY_OF_YEAR) == dia.get(Calendar.DAY_OF_YEAR)
    return SimpleDateFormat(if (mismoDia) "HH:mm" else "d MMM, HH:mm", LOCALE_CL).format(Date(millis))
}
