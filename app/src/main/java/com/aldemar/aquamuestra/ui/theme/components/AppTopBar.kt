package com.aldemar.aquamuestra.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.ui.theme.ThemeManager

/**
 * TopBar único para toda la app (mismo estilo que Historial):
 * título grande y en negrita sobre el fondo del tema.
 *
 * - [onBackClick] != null  -> muestra la flecha de retroceso a la izquierda.
 * - [actions]              -> contenido opcional alineado a la derecha (p. ej. menú de opciones).
 *
 * - [showShadow]          -> sombra sutil inferior (se puede apagar, p. ej. en Inicio).
 *
 * Deja un pequeño espacio inferior para separarse del contenido.
 */
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    showShadow: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val isDark = ThemeManager.isDarkMode
    val lineColor = ThemeManager.borderColor

    Row(
        modifier = modifier
            .fillMaxWidth()
            // Sombra sutil en el borde inferior (debe ir ANTES del background)
            .then(if (showShadow) Modifier.shadow(elevation = 3.dp) else Modifier)
            .background(ThemeManager.backgroundColor)
            // En modo oscuro la sombra casi no se nota: se refuerza con una línea fina
            .drawBehind {
                if (isDark && showShadow) {
                    drawLine(
                        color = lineColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
            .statusBarsPadding()
            .padding(
                start = if (onBackClick != null) 8.dp else 24.dp,
                end = 8.dp,
                top = 8.dp,
                bottom = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBackClick != null) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_volver),
                    tint = ThemeManager.textColor
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = ThemeManager.textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        actions()
    }
}
