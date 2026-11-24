package com.aldemar.aquamuestra.ui.theme.conteo

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.ui.theme.components.AppTopBar
import com.aldemar.aquamuestra.ui.theme.components.FotoZoomConMarcas
import com.aldemar.aquamuestra.viewmodel.AppViewModelFactory
import com.aldemar.aquamuestra.viewmodel.CambioConteo
import com.aldemar.aquamuestra.viewmodel.ConteoMarcas
import com.aldemar.aquamuestra.viewmodel.ConteoViewModel
import com.aldemar.aquamuestra.viewmodel.ResultadoConteo

/**
 * Conteo asistido: el operador marca cada chorito sobre la foto (con zoom) y puede corregir
 * el total con un ajuste manual. Devuelve el resultado con [onGuardar].
 */
@Composable
fun ConteoScreen(
    fotoPath: String?,
    marcasJson: String?,
    ajusteManual: Int,
    onGuardar: (ResultadoConteo) -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: ConteoViewModel = viewModel(factory = AppViewModelFactory(context))
    LaunchedEffect(Unit) { viewModel.iniciar(fotoPath, marcasJson, ajusteManual) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var confirmarLimpiar by rememberSaveable { mutableStateOf(false) }
    var confirmarSalir by rememberSaveable { mutableStateOf(false) }
    val conteoInicial = remember { ConteoMarcas(ConteoMarcas.marcasDesdeJson(marcasJson), ajusteManual) }
    val hayCambios = state.conteo != conteoInicial

    // Feedback háptico en cada cambio: distinto al marcar que al quitar o deshacer.
    LaunchedEffect(state.ultimoCambio) {
        when (state.ultimoCambio) {
            CambioConteo.AGREGADA, CambioConteo.AJUSTE -> haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            CambioConteo.QUITADA, CambioConteo.DESHECHA -> haptic.performHapticFeedback(HapticFeedbackType.Reject)
            CambioConteo.LIMPIADO -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            null -> return@LaunchedEffect
        }
        viewModel.onCambioMostrado()
    }

    val salir = { if (hayCambios) confirmarSalir = true else onCancelar() }
    BackHandler(onBack = salir)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = stringResource(R.string.conteo_titulo), onBackClick = salir) },
        bottomBar = {
            PanelConteo(
                conteo = state.conteo,
                onDeshacer = viewModel::deshacer,
                onLimpiar = { confirmarLimpiar = true },
                onAjustar = viewModel::ajustar,
                onGuardar = { onGuardar(viewModel.resultado()) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.inverseSurface) // fondo oscuro: la foto destaca
        ) {
            val foto = state.fotoPath
            if (foto == null) {
                Text(
                    stringResource(R.string.conteo_sin_foto),
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                FotoZoomConMarcas(
                    fotoPath = foto,
                    marcas = state.conteo.marcas,
                    descripcion = stringResource(R.string.conteo_foto_descripcion, state.conteo.conteoMarcas),
                    onToque = viewModel::tocar,
                    // Cada toque marca al instante: zoom solo con pellizco (el detalle sí usa doble toque)
                    zoomConDobleToque = false,
                    modifier = Modifier.fillMaxSize()
                )
                // Ayuda: se ve hasta la primera marca
                AnimatedVisibility(
                    visible = state.conteo.marcas.isEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(12.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.85f),
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            stringResource(R.string.conteo_ayuda),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .widthIn(max = 420.dp)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }

    if (confirmarLimpiar) {
        AlertDialog(
            onDismissRequest = { confirmarLimpiar = false },
            icon = { Icon(Icons.Filled.DeleteSweep, contentDescription = null) },
            title = { Text(stringResource(R.string.conteo_limpiar_titulo)) },
            text = { Text(stringResource(R.string.conteo_limpiar_texto, state.conteo.conteoMarcas)) },
            confirmButton = {
                TextButton(onClick = { viewModel.limpiar(); confirmarLimpiar = false }) {
                    Text(stringResource(R.string.conteo_limpiar_confirmar), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarLimpiar = false }) { Text(stringResource(R.string.muestra_cancelar)) }
            }
        )
    }
    if (confirmarSalir) {
        AlertDialog(
            onDismissRequest = { confirmarSalir = false },
            title = { Text(stringResource(R.string.conteo_salir_titulo)) },
            text = { Text(stringResource(R.string.conteo_salir_texto)) },
            confirmButton = {
                TextButton(onClick = { confirmarSalir = false; onCancelar() }) {
                    Text(stringResource(R.string.conteo_salir_confirmar), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalir = false }) { Text(stringResource(R.string.conteo_seguir)) }
            }
        )
    }
}

/** Panel inferior: contador siempre visible y controles grandes (56 dp) para usar con guantes. */
@Composable
private fun PanelConteo(
    conteo: ConteoMarcas,
    onDeshacer: () -> Unit,
    onLimpiar: () -> Unit,
    onAjustar: (Int) -> Unit,
    onGuardar: () -> Unit
) {
    val ajusteTexto = if (conteo.ajusteManual > 0) "+${conteo.ajusteManual}" else conteo.ajusteManual.toString()
    val resumenCd = stringResource(R.string.conteo_resumen_cd, conteo.conteoMarcas, ajusteTexto, conteo.total)

    Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Contador: Marcados · Ajuste · Total (TalkBack lo lee como una frase y anuncia los cambios)
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .clearAndSetSemantics {
                        contentDescription = resumenCd
                        liveRegion = LiveRegionMode.Polite
                    }
            ) {
                Dato(stringResource(R.string.conteo_marcados), conteo.conteoMarcas.toString(), Modifier.weight(1f))
                Dato(stringResource(R.string.conteo_ajuste), ajusteTexto, Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1.2f)) {
                    Text(stringResource(R.string.conteo_total), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    // El total "salta" al cambiar: confirma el toque sin tener que leerlo con atención
                    AnimatedContent(
                        targetState = conteo.total,
                        transitionSpec = {
                            val sube = targetState > initialState
                            (slideInVertically { if (sube) it else -it } + fadeIn()) togetherWith
                                (slideOutVertically { if (sube) -it else it } + fadeOut())
                        },
                        label = "total"
                    ) { total ->
                        Text(
                            total.toString(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Fila 1: corregir marcas
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = onDeshacer,
                    enabled = conteo.marcas.isNotEmpty(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.conteo_deshacer))
                }
                FilledTonalButton(
                    onClick = onLimpiar,
                    enabled = conteo.marcas.isNotEmpty(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.conteo_limpiar))
                }
            }

            // Fila 2: ajuste manual −1 [valor] +1
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.conteo_ajuste),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                FilledTonalIconButton(onClick = { onAjustar(-1) }, enabled = conteo.total > 0, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.conteo_restar))
                }
                Text(
                    ajusteTexto,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(64.dp)
                )
                FilledTonalIconButton(onClick = { onAjustar(1) }, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.conteo_sumar))
                }
            }

            Button(
                onClick = onGuardar,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) { Text(stringResource(R.string.conteo_guardar), style = MaterialTheme.typography.titleMedium) }
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(etiqueta, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
    }
}
