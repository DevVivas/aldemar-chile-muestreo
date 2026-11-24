package com.aldemar.aquamuestra.ui.theme.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.request.ImageRequest
import com.aldemar.aquamuestra.viewmodel.Punto
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import androidx.compose.foundation.Image
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import me.saket.telephoto.zoomable.ZoomableContentLocation
import me.saket.telephoto.zoomable.zoomable
import java.io.File

/** Colores de las marcas: ámbar con doble borde (blanco + negro) para que se vean sobre cualquier foto. */
private val MarcaRelleno = Color(0xFFFFC400)
private val MarcaTexto = Color.Black
private val MarcaBordeInterior = Color.White
private val MarcaBordeExterior = Color.Black

/** Radio de la marca en pantalla (no crece con el zoom: siempre fácil de ver y de tocar). */
private val RADIO_MARCA = 13.dp

/** Distancia máxima a una marca para que un toque la quite (más grande que la marca: dedos con guante). */
private val RADIO_TOQUE = 24.dp

/**
 * Foto con zoom (Telephoto: pellizcar, doble toque y desplazar) y las marcas dibujadas encima.
 *
 * Las marcas se guardan en coordenadas de la IMAGEN (0..1). Para dibujarlas se usa el rectángulo
 * donde Telephoto está mostrando la imagen en este momento ([transformedContentBounds], que cambia
 * con el zoom y el desplazamiento): posición en pantalla = borde + fracción × tamaño.
 * Por eso la marca sigue pegada al mismo chorito aunque se amplíe o se mueva la foto.
 *
 * @param onToque si no es null, la foto es editable: recibe el toque en coordenadas de la imagen (0..1)
 *   y la tolerancia normalizada para decidir si se tocó una marca existente.
 * @param zoomConDobleToque true = el doble toque amplía (detalle). false = solo pellizco y desplazamiento,
 *   y cada toque llega al instante: con doble toque activo, Telephoto espera ~300 ms para saber si viene
 *   un segundo toque, y marcar se siente lento (conteo).
 */
// transformedContentBounds está marcado como obsoleto en Telephoto 0.19 (lo reemplaza
// coordinateSystem.contentBounds, que devuelve un SpatialRect). Sigue funcionando y es más simple de explicar.
@Suppress("DEPRECATION")
@Composable
fun FotoZoomConMarcas(
    fotoPath: String,
    marcas: List<Punto>,
    descripcion: String,
    modifier: Modifier = Modifier,
    onToque: ((x: Float, y: Float, toleranciaX: Float, toleranciaY: Float) -> Unit)? = null,
    zoomConDobleToque: Boolean = true
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val zoomState = rememberZoomableState(zoomSpec = ZoomSpec(maxZoomFactor = 8f))
    val imageState = rememberZoomableImageState(zoomState)
    val medidorTexto = rememberTextMeasurer()
    val archivo = remember(fotoPath) { File(fotoPath) }

    Box(modifier = modifier) {
        val alTocar: ((Offset) -> Unit)? = onToque?.let { callback ->
            { posicion: Offset ->
                val limites = zoomState.transformedContentBounds
                if (limites.width > 0f && limites.height > 0f) {
                    // Pantalla → imagen: qué fracción del ancho y del alto de la foto se tocó
                    val x = (posicion.x - limites.left) / limites.width
                    val y = (posicion.y - limites.top) / limites.height
                    val radio = with(density) { RADIO_TOQUE.toPx() }
                    callback(x, y, radio / limites.width, radio / limites.height)
                }
            }
        }
        // La clave incluye la fecha del archivo: si se reemplaza la foto, Coil no usa la vieja de su caché.
        val solicitud = remember(fotoPath, archivo.lastModified()) {
            ImageRequest.Builder(context).data(archivo).build()
        }

        if (zoomConDobleToque) {
            ZoomableAsyncImage(
                model = solicitud,
                contentDescription = descripcion,
                state = imageState,
                onClick = alTocar,
                modifier = Modifier.matchParentSize()
            )
        } else {
            // Sin doble toque: imagen normal + Modifier.zoomable(onDoubleClick = null).
            // Hay que decirle a Telephoto dónde queda la foto dentro del recuadro (ContentScale.Fit).
            val pintor = rememberAsyncImagePainter(solicitud)
            val tamano = pintor.intrinsicSize
            LaunchedEffect(tamano) {
                if (tamano.isSpecified) zoomState.setContentLocation(ZoomableContentLocation.scaledToFitAndCenterAligned(tamano))
            }
            Image(
                painter = pintor,
                contentDescription = descripcion,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .matchParentSize()
                    .zoomable(state = zoomState, onClick = alTocar, onDoubleClick = null)
            )
        }

        // Capa de marcas: solo dibuja, no recibe toques (los toques los maneja Telephoto).
        Canvas(modifier = Modifier.matchParentSize()) {
            val limites = zoomState.transformedContentBounds
            if (limites.width <= 0f || limites.height <= 0f) return@Canvas
            marcas.forEachIndexed { i, p ->
                // Imagen → pantalla
                val centro = Offset(limites.left + p.x * limites.width, limites.top + p.y * limites.height)
                dibujarMarca(centro, i + 1, medidorTexto)
            }
        }
    }
}

private fun DrawScope.dibujarMarca(centro: Offset, numero: Int, medidor: TextMeasurer) {
    val r = RADIO_MARCA.toPx()
    drawCircle(MarcaBordeExterior, radius = r + 3.dp.toPx(), center = centro)
    drawCircle(MarcaBordeInterior, radius = r + 1.5.dp.toPx(), center = centro)
    drawCircle(MarcaRelleno, radius = r, center = centro)
    drawCircle(MarcaBordeExterior, radius = r, center = centro, style = Stroke(width = 1.dp.toPx()))

    val texto = medidor.measure(
        text = numero.toString(),
        style = TextStyle(
            color = MarcaTexto,
            fontSize = if (numero < 100) 12.sp else 9.sp,
            fontWeight = FontWeight.Bold
        )
    )
    drawText(
        texto,
        topLeft = Offset(centro.x - texto.size.width / 2f, centro.y - texto.size.height / 2f)
    )
}
