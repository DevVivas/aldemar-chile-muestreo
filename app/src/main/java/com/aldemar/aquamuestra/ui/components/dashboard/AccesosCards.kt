package com.aldemar.aquamuestra.ui.components.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldemar.aquamuestra.ui.theme.AquaNavy
import com.aldemar.aquamuestra.ui.theme.AquaSurface
import com.aldemar.aquamuestra.ui.theme.AquaSurfaceVariant
import com.aldemar.aquamuestra.ui.theme.AquaTeal
import com.aldemar.aquamuestra.ui.theme.AquaTealLight
import com.aldemar.aquamuestra.ui.theme.AquaTextSecondary

// 20. Tarjeta Historial.
@Composable
fun HistorialCard(
    total: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TarjetaBase(
        modifier = modifier.fillMaxHeight(),
        onClick = onClick,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CajaIcono(
                icono = Icons.AutoMirrored.Outlined.FormatListBulleted,
                tamano = 32.dp,
                tamanoIcono = 18.dp,
                colorFondo = AquaSurfaceVariant,
                colorIcono = AquaTeal
            )
            PildoraEstado(
                texto = "$total total",
                icono = null,
                colorFondo = AquaSurfaceVariant,
                colorTexto = AquaTextSecondary
            )
        }
        Column {
            Text(
                text = "Ver Historial",
                style = MaterialTheme.typography.titleMedium,
                color = AquaNavy
            )
            Text(
                text = "Registros históricos",
                style = MaterialTheme.typography.bodySmall,
                color = AquaTextSecondary
            )
        }
    }
}

// 21. Tarjeta Sincronización.
@Composable
fun SyncCard(
    estadoSync: String,
    onSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    TarjetaBase(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                CajaIcono(
                    icono = Icons.Outlined.SyncAlt,
                    tamano = 32.dp,
                    tamanoIcono = 18.dp,
                    colorFondo = AquaTealLight,
                    colorIcono = AquaTeal
                )
            }
            Surface(
                shape = CircleShape,
                color = AquaTeal,
                modifier = Modifier.size(10.dp)
            ) {}
        }
        Column {
            Text(
                text = "Sincronización",
                style = MaterialTheme.typography.titleMedium,
                color = AquaNavy
            )
            Spacer(modifier = Modifier.size(8.dp))
            Button(
                onClick = { if (estadoSync == "Inactivo") onSync() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AquaTeal
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.CloudUpload,
                    contentDescription = null,
                    tint = AquaSurface,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (estadoSync) {
                        "Enviando" -> "Enviando datos..."
                        "Completado" -> "Completado"
                        else -> "Sincronizar ahora"
                    },
                    color = AquaSurface,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
