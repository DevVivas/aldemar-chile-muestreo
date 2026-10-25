package com.aldemar.aquamuestra.ui.theme.historial

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.ui.theme.AppRoutes
import com.aldemar.aquamuestra.ui.theme.ThemeManager
import com.aldemar.aquamuestra.ui.theme.components.AppTopBar
import com.aldemar.aquamuestra.ui.theme.components.BottomNavBar

data class MuestraPrueba(
    val id: String,
    val centro: String,
    val fecha: String,
    val sincronizado: Boolean
)

@Composable
fun HistorialScreen(navController: NavController) {
    val muestrasFake = listOf(
        MuestraPrueba("1001", "Centro Los Fiordos", "25 Oct 2026 - 08:30", true),
        MuestraPrueba("1002", "Centro Bahía Azul", "24 Oct 2026 - 15:45", false),
        MuestraPrueba("1003", "Centro Isla Chauques", "23 Oct 2026 - 10:15", true)
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.historial_titulo),
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = { BottomNavBar(navController = navController) },
        containerColor = ThemeManager.backgroundColor
    ) { paddingValues: PaddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            // El padding superior separa la lista de la barra de título
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(muestrasFake) { muestra ->
                MuestraCard(
                    muestra = muestra,
                    onClick = {
                        navController.navigate(AppRoutes.SampleDetail.createRoute(muestra.id))
                    }
                )
            }
        }
    }
}

@Composable
fun MuestraCard(
    muestra: MuestraPrueba,
    onClick: () -> Unit
) {
    val colorEstado = if (muestra.sincronizado) Color(0xFF4CAF50) else Color(0xFFFFA000)
    val textoEstado = if (muestra.sincronizado) stringResource(R.string.historial_estado_sincronizado)
    else stringResource(R.string.historial_estado_pendiente)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeManager.surfaceColor),
        border = BorderStroke(1.dp, ThemeManager.borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = muestra.centro,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = ThemeManager.textColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = muestra.fecha,
                    fontSize = 14.sp,
                    color = ThemeManager.secondaryTextColor
                )
                Text(
                    text = stringResource(R.string.lote_label, muestra.id),
                    fontSize = 12.sp,
                    color = ThemeManager.secondaryTextColor,
                    fontWeight = FontWeight.Light
                )
            }
            Text(
                text = textoEstado,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colorEstado
            )
        }
    }
}
