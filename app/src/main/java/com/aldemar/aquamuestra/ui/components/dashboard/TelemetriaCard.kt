package com.aldemar.aquamuestra.ui.components.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aldemar.aquamuestra.model.Telemetria
import com.aldemar.aquamuestra.ui.theme.AquaFog
import com.aldemar.aquamuestra.ui.theme.AquaAbyss
import com.aldemar.aquamuestra.ui.theme.AquaEmber
import com.aldemar.aquamuestra.ui.theme.AquaGraphite

// 22. Tarjeta con telemetría del centro.
@Composable
fun TelemetriaCard(
    telemetria: Telemetria
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = AquaFog,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        icono = Icons.Outlined.Thermostat,
                        tamano = 32.dp,
                        tamanoIcono = 18.dp,
                        colorFondo = Color.White,
                        colorIcono = AquaEmber
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Telemetría de Centro",
                        style = MaterialTheme.typography.titleMedium,
                        color = AquaAbyss
                    )
                }
                Text(
                    text = telemetria.sonda,
                    style = MaterialTheme.typography.bodySmall,
                    color = AquaGraphite
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .height(60.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Temp Agua",
                            style = MaterialTheme.typography.labelSmall,
                            color = AquaGraphite
                        )
                        Text(
                            text = telemetria.temperatura,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AquaAbyss,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .height(60.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Salinidad",
                            style = MaterialTheme.typography.labelSmall,
                            color = AquaGraphite
                        )
                        Text(
                            text = telemetria.salinidad,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AquaAbyss,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .height(60.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Oxígeno",
                            style = MaterialTheme.typography.labelSmall,
                            color = AquaGraphite
                        )
                        Text(
                            text = telemetria.oxigeno,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AquaEmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                text = "Datos de ejemplo",
                style = MaterialTheme.typography.labelSmall,
                color = AquaGraphite
            )
        }
    }
}
