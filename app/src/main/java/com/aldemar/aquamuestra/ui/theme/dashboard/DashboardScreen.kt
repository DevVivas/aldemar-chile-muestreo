package com.aldemar.aquamuestra.ui.theme.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.ui.theme.AppRoutes
import com.aldemar.aquamuestra.ui.theme.ColorAbyss
import com.aldemar.aquamuestra.ui.theme.ColorEmber
import com.aldemar.aquamuestra.ui.theme.ColorPaper
import com.aldemar.aquamuestra.ui.theme.SessionManager
import com.aldemar.aquamuestra.ui.theme.ThemeManager
import com.aldemar.aquamuestra.ui.theme.UserRole
import com.aldemar.aquamuestra.ui.theme.components.AppTopBar
import com.aldemar.aquamuestra.ui.theme.components.BottomNavBar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Datos de ejemplo (se reemplazarán por datos reales cuando exista el backend)
private const val CENTRO_NOMBRE = "Centro Demo Norte"
private const val MUESTRAS_HOY = 12
private const val REVISIONES_VALIDADAS = 8
private const val REVISIONES_PENDIENTES = 4
private const val TOTAL_REGISTROS = 124
private const val ID_ULTIMA_MUESTRA = "1001"

@Composable
fun DashboardScreen(navController: NavController) {
    val user = SessionManager.currentUser
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val syncDoneMsg = stringResource(R.string.dashboard_sync_done)
    val today = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()) }

    val roleLabel = when (user?.role) {
        UserRole.SUPERVISOR -> stringResource(R.string.role_supervisor)
        UserRole.OPERARIO -> stringResource(R.string.role_operario)
        null -> ""
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.dashboard_titulo),
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = { BottomNavBar(navController = navController) },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(16.dp)) { data ->
                Snackbar(
                    containerColor = ColorAbyss,
                    contentColor = ColorPaper,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = data.visuals.message, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        containerColor = ThemeManager.backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.dashboard_centro, CENTRO_NOMBRE),
                fontSize = 14.sp,
                color = ThemeManager.secondaryTextColor
            )

            OperatorCard(
                name = user?.name ?: "",
                role = stringResource(R.string.dashboard_role_line, roleLabel)
            )

            // Resumen operativo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Waves, contentDescription = null, tint = ColorEmber, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.dashboard_resumen),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ThemeManager.textColor,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.dashboard_hoy, today),
                    fontSize = 12.sp,
                    color = ThemeManager.secondaryTextColor
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SamplesTodayCard(modifier = Modifier.weight(1f).fillMaxHeight())
                ReviewsCard(modifier = Modifier.weight(1f).fillMaxHeight())
            }

            LastSampleCard(
                onClick = { navController.navigate(AppRoutes.SampleDetail.createRoute(ID_ULTIMA_MUESTRA)) }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HistoryCard(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    onClick = {
                        navController.navigate(AppRoutes.Historial.ruta) {
                            popUpTo(AppRoutes.Inicio.ruta) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
                SyncCard(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    onSyncClick = { scope.launch { snackbarHostState.showSnackbar(syncDoneMsg) } }
                )
            }

            TelemetryCard()
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Componentes del dashboard
// ---------------------------------------------------------------------------------------------

@Composable
private fun DashCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = ThemeManager.surfaceColor,
        border = BorderStroke(1.dp, ThemeManager.borderColor)
    ) {
        Column(
            modifier = Modifier
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun IconBox(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(ColorEmber.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = ColorEmber, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun EmberChip(text: String) {
    Surface(shape = RoundedCornerShape(50), color = ColorEmber.copy(alpha = 0.12f)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = ColorEmber
        )
    }
}

@Composable
private fun OperatorCard(name: String, role: String) {
    val initials = name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ColorEmber.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, ColorEmber.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(ColorEmber, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = initials, color = ColorPaper, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Column {
                Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorEmber)
                Text(text = role, fontSize = 13.sp, color = ThemeManager.secondaryTextColor)
            }
        }
    }
}

@Composable
private fun SamplesTodayCard(modifier: Modifier = Modifier) {
    DashCard(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = stringResource(R.string.dashboard_muestras_hoy),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeManager.secondaryTextColor,
                modifier = Modifier.weight(1f)
            )
            IconBox(Icons.Filled.HourglassEmpty)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = MUESTRAS_HOY.toString(),
            fontSize = 36.sp,
            fontWeight = FontWeight.SemiBold,
            color = ThemeManager.textColor
        )
        Text(
            text = stringResource(R.string.dashboard_lotes_completados),
            fontSize = 12.sp,
            color = ThemeManager.secondaryTextColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = ColorEmber, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.dashboard_vs_ayer),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ColorEmber
            )
        }
    }
}

@Composable
private fun ReviewsCard(modifier: Modifier = Modifier) {
    DashCard(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = stringResource(R.string.dashboard_revisiones),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeManager.secondaryTextColor,
                modifier = Modifier.weight(1f)
            )
            IconBox(Icons.Filled.Shield)
        }
        Spacer(modifier = Modifier.height(12.dp))
        StatLine(Icons.Outlined.CheckCircle, stringResource(R.string.dashboard_validadas), REVISIONES_VALIDADAS)
        Spacer(modifier = Modifier.height(6.dp))
        StatLine(Icons.Outlined.Schedule, stringResource(R.string.dashboard_pendientes), REVISIONES_PENDIENTES)
        Spacer(modifier = Modifier.weight(1f, fill = false))
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.dashboard_supervision),
            fontSize = 11.sp,
            color = ThemeManager.secondaryTextColor
        )
    }
}

@Composable
private fun StatLine(icon: ImageVector, label: String, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = ThemeManager.secondaryTextColor, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 12.sp, color = ThemeManager.secondaryTextColor)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = value.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ThemeManager.textColor)
    }
}

@Composable
private fun LastSampleCard(onClick: () -> Unit) {
    DashCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBox(Icons.Filled.History)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.dashboard_ultima_muestra),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = ThemeManager.textColor,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            EmberChip(stringResource(R.string.dashboard_reciente))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.dashboard_ultima_linea),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ThemeManager.textColor
                )
                Text(
                    text = stringResource(R.string.dashboard_ultima_detalle),
                    fontSize = 12.sp,
                    color = ThemeManager.secondaryTextColor
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.dashboard_ultima_medida),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorEmber
                )
                Text(
                    text = stringResource(R.string.dashboard_ultima_calibre),
                    fontSize = 12.sp,
                    color = ThemeManager.secondaryTextColor
                )
            }
        }
    }
}

@Composable
private fun HistoryCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    DashCard(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBox(Icons.AutoMirrored.Filled.List)
            Surface(shape = RoundedCornerShape(50), color = ThemeManager.borderColor.copy(alpha = 0.6f)) {
                Text(
                    text = stringResource(R.string.dashboard_total, TOTAL_REGISTROS),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ThemeManager.textColor
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.weight(1f, fill = false))
        Text(
            text = stringResource(R.string.dashboard_ver_historial),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = ThemeManager.textColor
        )
        Text(
            text = stringResource(R.string.dashboard_registros),
            fontSize = 12.sp,
            color = ThemeManager.secondaryTextColor
        )
    }
}

@Composable
private fun SyncCard(modifier: Modifier = Modifier, onSyncClick: () -> Unit) {
    DashCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            IconBox(Icons.Filled.SwapHoriz)
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(ColorEmber, CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.dashboard_sincronizacion),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = ThemeManager.textColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onSyncClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorEmber, contentColor = ColorPaper),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
        ) {
            Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.dashboard_sync_btn),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TelemetryCard() {
    DashCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBox(Icons.Filled.Thermostat)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.dashboard_telemetria),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = ThemeManager.textColor,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.dashboard_sonda),
                fontSize = 11.sp,
                color = ThemeManager.secondaryTextColor
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TelemetryItem(
                label = stringResource(R.string.dashboard_temp_agua),
                value = stringResource(R.string.dashboard_temp_valor),
                modifier = Modifier.weight(1f)
            )
            TelemetryItem(
                label = stringResource(R.string.dashboard_salinidad),
                value = stringResource(R.string.dashboard_sal_valor),
                modifier = Modifier.weight(1f)
            )
            TelemetryItem(
                label = stringResource(R.string.dashboard_oxigeno),
                value = stringResource(R.string.dashboard_oxi_valor),
                valueColor = ColorEmber,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.dashboard_datos_ejemplo),
            fontSize = 11.sp,
            color = ThemeManager.secondaryTextColor
        )
    }
}

@Composable
private fun TelemetryItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = ThemeManager.textColor
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = ThemeManager.backgroundColor,
        border = BorderStroke(1.dp, ThemeManager.borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 11.sp, color = ThemeManager.secondaryTextColor, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
