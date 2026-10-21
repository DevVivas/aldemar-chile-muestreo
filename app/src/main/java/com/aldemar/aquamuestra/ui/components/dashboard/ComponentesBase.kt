package com.aldemar.aquamuestra.ui.components.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aldemar.aquamuestra.ui.theme.AquaNavy
import com.aldemar.aquamuestra.ui.theme.AquaSurface
import com.aldemar.aquamuestra.ui.theme.AquaTeal

// 13. Un Component recibe datos y se preocupa solamente de mostrarlos.
@Composable
fun TarjetaBase(
    modifier: Modifier = Modifier,
    color: Color = AquaSurface,
    onClick: (() -> Unit)? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick == null) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            color = color,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = verticalArrangement,
                content = content
            )
        }
    } else {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            color = color,
            shadowElevation = 1.dp,
            onClick = onClick
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = verticalArrangement,
                content = content
            )
        }
    }
}

@Composable
fun PildoraEstado(
    texto: String,
    icono: ImageVector?,
    colorFondo: Color,
    colorTexto: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = colorFondo
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icono != null) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = colorTexto,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = texto,
                color = colorTexto,
                fontSize = androidx.compose.ui.unit.TextUnit(10f, androidx.compose.ui.unit.TextUnitType.Sp),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
    }
}

@Composable
fun CajaIcono(
    icono: ImageVector,
    tamano: Dp,
    tamanoIcono: Dp,
    colorFondo: Color,
    colorIcono: Color,
    forma: Shape = RoundedCornerShape(8.dp)
) {
    Surface(
        shape = forma,
        color = colorFondo,
        modifier = Modifier.size(tamano)
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = colorIcono,
            modifier = Modifier.size(tamanoIcono).padding((tamano - tamanoIcono) / 2)
        )
    }
}

@Composable
fun TituloSeccion(
    icono: ImageVector,
    titulo: String,
    extra: String? = null
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = AquaTeal
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = titulo,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            color = AquaNavy
        )
        if (extra != null) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = extra,
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = AquaNavy
            )
        }
    }
}
