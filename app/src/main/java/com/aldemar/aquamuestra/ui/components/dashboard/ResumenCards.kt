package com.aldemar.aquamuestra.ui.components.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aldemar.aquamuestra.ui.theme.AquaAbyss
import com.aldemar.aquamuestra.ui.theme.AquaEmber
import com.aldemar.aquamuestra.ui.theme.AquaEmberSuave
import com.aldemar.aquamuestra.ui.theme.AquaFog
import com.aldemar.aquamuestra.ui.theme.AquaGraphite

// 16. Tarjeta Muestras Hoy.
@Composable
fun MuestrasHoyCard(
    hoy: Int,
    variacion: Int,
    ayer: Int,
    modifier: Modifier = Modifier
) {
    val signo = if (variacion >= 0) "+" else ""

    TarjetaBase(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "MUESTRAS HOY",
                style = MaterialTheme.typography.labelMedium,
                color = AquaGraphite
            )
            CajaIcono(
                icono = Icons.Outlined.Scale,
                tamano = 32.dp,
                tamanoIcono = 18.dp,
                colorFondo = AquaFog,
                colorIcono = AquaEmber
            )
        }

        Column(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(
                text = hoy.toString(),
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Lotes completados",
                style = MaterialTheme.typography.bodySmall,
                color = AquaGraphite
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.TrendingUp,
                contentDescription = null,
                tint = AquaEmber,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$signo$variacion% vs ayer ($ayer)",
                style = MaterialTheme.typography.labelSmall,
                color = AquaEmber
            )
        }
    }
}

// 17. Tarjeta Revisiones.
@Composable
fun RevisionesCard(
    validadas: Int,
    pendientes: Int,
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
            Text(
                text = "REVISIONES",
                style = MaterialTheme.typography.labelMedium,
                color = AquaGraphite
            )
            CajaIcono(
                icono = Icons.Outlined.VerifiedUser,
                tamano = 32.dp,
                tamanoIcono = 18.dp,
                colorFondo = AquaFog,
                colorIcono = AquaEmber
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CajaIcono(
                    icono = Icons.Outlined.CheckCircle,
                    tamano = 20.dp,
                    tamanoIcono = 12.dp,
                    colorFondo = AquaFog,
                    colorIcono = AquaGraphite
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Validadas  $validadas",
                    style = MaterialTheme.typography.bodySmall,
                    color = AquaAbyss
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CajaIcono(
                    icono = Icons.Outlined.Schedule,
                    tamano = 20.dp,
                    tamanoIcono = 12.dp,
                    colorFondo = AquaFog,
                    colorIcono = AquaGraphite
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pendientes  $pendientes",
                    style = MaterialTheme.typography.bodySmall,
                    color = AquaAbyss
                )
            }
        }

        Text(
            text = "Supervisión en pontón",
            style = MaterialTheme.typography.bodySmall,
            color = AquaGraphite
        )
    }
}
