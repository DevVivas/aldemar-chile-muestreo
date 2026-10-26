package com.aldemar.aquamuestra.ui.components.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HistoryToggleOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aldemar.aquamuestra.model.UltimaMuestra
import com.aldemar.aquamuestra.ui.theme.AquaAbyss
import com.aldemar.aquamuestra.ui.theme.AquaEmber
import com.aldemar.aquamuestra.ui.theme.AquaEmberSuave
import com.aldemar.aquamuestra.ui.theme.AquaGraphite

// 19. Tarjeta con la última muestra registrada.
@Composable
fun UltimaMuestraCard(
    muestra: UltimaMuestra
) {
    TarjetaBase(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                CajaIcono(
                    icono = Icons.Outlined.HistoryToggleOff,
                    tamano = 32.dp,
                    tamanoIcono = 18.dp,
                    colorFondo = com.aldemar.aquamuestra.ui.theme.AquaFog,
                    colorIcono = AquaEmber
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Última Muestra Registrada",
                    style = MaterialTheme.typography.titleMedium,
                    color = AquaAbyss
                )
            }
            PildoraEstado(
                texto = "Reciente",
                icono = null,
                colorFondo = AquaEmberSuave,
                colorTexto = AquaEmber
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = muestra.linea,
                    style = MaterialTheme.typography.titleMedium,
                    color = AquaAbyss
                )
                Text(
                    text = "${muestra.hace} • Profundidad ${muestra.profundidad}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AquaGraphite
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = muestra.densidad,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AquaEmber,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Calibre: ${muestra.calibre}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AquaGraphite
                )
            }
        }
    }
}
