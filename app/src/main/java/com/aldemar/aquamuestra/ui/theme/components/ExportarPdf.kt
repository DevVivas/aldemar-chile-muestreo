package com.aldemar.aquamuestra.ui.theme.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.data.local.UserRole
import com.aldemar.aquamuestra.data.reporte.GeneradorPdf
import com.aldemar.aquamuestra.ui.theme.SessionManager
import com.aldemar.aquamuestra.viewmodel.Reporte
import com.aldemar.aquamuestra.viewmodel.ReporteMuestras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Controla la exportación: pide dónde guardar, escribe el PDF y ofrece abrirlo. */
class ExportadorPdf internal constructor(
    private val iniciar: (Reporte) -> Unit,
    private val estaGenerando: () -> Boolean
) {
    val generando: Boolean get() = estaGenerando()

    /** Si el reporte no tiene validadas, avisa en vez de crear un PDF vacío. */
    fun exportar(reporte: Reporte) = iniciar(reporte)
}

/**
 * Flujo de exportación:
 *  1. Si no hay validadas → Snackbar de aviso.
 *  2. Selector del sistema "Guardar como" (CreateDocument) con nombre aquamuestra-reporte-AAAAMMDD.pdf.
 *  3. Se escribe el PDF en el archivo elegido (en segundo plano).
 *  4. Snackbar "PDF guardado" con acción "Abrir" (ACTION_VIEW con el uri).
 */
@Composable
fun rememberExportadorPdf(snackbar: SnackbarHostState): ExportadorPdf {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendiente by remember { mutableStateOf<Reporte?>(null) }
    var generando by remember { mutableStateOf(false) }

    val msgSinValidadas = stringResource(R.string.pdf_sin_validadas)
    val msgListo = stringResource(R.string.pdf_listo)
    val msgAbrir = stringResource(R.string.pdf_abrir)
    val msgError = stringResource(R.string.pdf_error)
    val msgSinVisor = stringResource(R.string.pdf_sin_visor)

    val guardarComo = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri: Uri? ->
        val reporte = pendiente
        pendiente = null
        if (uri == null || reporte == null) return@rememberLauncherForActivityResult // el usuario canceló
        generando = true
        scope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { GeneradorPdf.escribir(reporte, it) }
                        ?: error("No se pudo abrir el archivo")
                }.isSuccess
            }
            generando = false
            if (!ok) {
                snackbar.showSnackbar(msgError)
                return@launch
            }
            val r = snackbar.showSnackbar(msgListo, actionLabel = msgAbrir, duration = SnackbarDuration.Long)
            if (r == SnackbarResult.ActionPerformed) {
                val ver = Intent(Intent.ACTION_VIEW)
                    .setDataAndType(uri, "application/pdf")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                try {
                    context.startActivity(ver)
                } catch (_: ActivityNotFoundException) {
                    snackbar.showSnackbar(msgSinVisor)
                }
            }
        }
    }

    return remember(guardarComo) {
        ExportadorPdf(
            iniciar = { reporte ->
                if (reporte.vacio) {
                    scope.launch { snackbar.showSnackbar(msgSinValidadas) }
                } else {
                    pendiente = reporte
                    guardarComo.launch(ReporteMuestras.nombreArchivo(System.currentTimeMillis()))
                }
            },
            estaGenerando = { generando }
        )
    }
}

/** "Supervisor Demo (Supervisor de Calidad)" para el encabezado del PDF. */
@Composable
fun textoGeneradoPor(): String {
    val user = SessionManager.currentUser ?: return ""
    val rol = stringResource(
        when (user.role) {
            UserRole.SUPERVISOR -> R.string.role_supervisor
            UserRole.OPERARIO -> R.string.role_operario
            UserRole.ANALISTA -> R.string.role_analista
        }
    )
    return stringResource(R.string.pdf_generado_por, user.name, rol)
}

/** Botón "Exportar PDF" (solo lo muestran las pantallas si el rol puede exportar). */
@Composable
fun BotonExportarPdf(generando: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilledTonalButton(onClick = onClick, enabled = !generando, modifier = modifier) {
        if (generando) {
            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
        } else {
            Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(stringResource(if (generando) R.string.pdf_generando else R.string.pdf_exportar))
    }
}
