package com.aldemar.aquamuestra.ui.theme.detalle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.ui.theme.*
import com.aldemar.aquamuestra.ui.theme.components.AppTopBar
import com.aldemar.aquamuestra.ui.theme.components.BottomNavBar

@Composable
fun SampleDetailScreen(
    sampleId: String,
    navController: NavController,
    onBackClick: () -> Unit = { navController.popBackStack() }
) {
    var commentsText by remember {
        mutableStateOf("Muestra representativa, talla homogénea. Aprobada para informe de biomasa semanal.")
    }
    var selectedAction by remember { mutableStateOf("Validar") }

    // El AppTopBar va en el slot topBar del Scaffold (igual que en las demás pantallas).
    // Antes estaba dentro del contenido: el Scaffold ya descontaba la barra de estado en
    // paddingValues y el AppTopBar la sumaba otra vez, por eso se veía más abajo.
    Scaffold(
        containerColor = ThemeManager.backgroundColor,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.detail_title),
                onBackClick = onBackClick
            )
        },
        bottomBar = { BottomNavBar(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            StatusBanner(sampleId = sampleId)
            BiometricSheetSection()
            FieldEvidenceSection()
            // Panel de aprobación técnica: solo lo ve el supervisor
            if (SessionManager.isSupervisor) {
                SupervisorApprovalSection(
                    selectedAction = selectedAction,
                    onActionSelected = { selectedAction = it },
                    commentsText = commentsText,
                    onCommentsChange = { commentsText = it }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatusBanner(sampleId: String) {
    // El banner naranja es un color de "estado": se ve bien en ambos temas, se deja fijo.
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFE0B2),
        border = BorderStroke(1.dp, Color(0xFFFFB74D))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFB74D)
                ) {
                    Icon(
                        Icons.Default.Assignment,
                        contentDescription = null,
                        tint = Color(0xFF5D4037),
                        modifier = Modifier
                            .padding(6.dp)
                            .size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.detail_status_label),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                    Text(
                        text = stringResource(R.string.detail_status_pending),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5D4037)
                    )
                }
            }
            // Etiqueta del lote: sobre el banner naranja, se mantiene clara con texto oscuro
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = ColorPaper,
                border = BorderStroke(1.dp, ColorMist)
            ) {
                Text(
                    text = stringResource(R.string.lote_label, sampleId),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ColorGraphite
                )
            }
        }
    }
}

@Composable
private fun BiometricSheetSection() {
    val textColor = ThemeManager.textColor
    val subTextColor = ThemeManager.secondaryTextColor

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Assignment, contentDescription = null, tint = textColor, modifier = Modifier.size(20.dp))
                Text(
                    text = stringResource(R.string.detail_biometric_sheet),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE0F7FA)) {
                Text(
                    text = stringResource(R.string.detail_sernapesca_cert),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00796B)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DataCard(stringResource(R.string.detail_center_label), "Centro Chiloé", "Sector Huito", Modifier.weight(1f))
                DataCard(stringResource(R.string.detail_concession_label), "Tren 04-B", "Polígono Sur Aysén", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DataCard(stringResource(R.string.detail_line_label), "Línea A-04", "Profundidad 8.5m", Modifier.weight(1f))
                DataCard(stringResource(R.string.detail_species_label), "M. chilensis", "Mejillón Chileno", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DataCard(stringResource(R.string.detail_datetime_label), "14/09/2026", "11:45 hrs GMT-3", Modifier.weight(1f))
                DataCard(stringResource(R.string.detail_operator_label), "D. Villamizar", "TEC-4092 · Terreno", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DataCard(stringResource(R.string.detail_rope_section_label), "0.50 m", "", Modifier.weight(1f), isValueLarge = true)
                DataCard(stringResource(R.string.detail_effective_count_label), "150 ind", "", Modifier.weight(1f), isValueLarge = true)
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = ThemeManager.surfaceColor,
                border = BorderStroke(1.dp, ThemeManager.borderColor)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Analytics, contentDescription = null, tint = textColor, modifier = Modifier.size(24.dp))
                        Column {
                            Text(stringResource(R.string.detail_density_estimated), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = subTextColor)
                            Text(stringResource(R.string.detail_density_value), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor)
                        }
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF00796B)) {
                        Text(
                            stringResource(R.string.detail_density_optimal),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorPaper
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DataCard(
    label: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    isValueLarge: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = ThemeManager.surfaceColor,
        border = BorderStroke(1.dp, ThemeManager.borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ThemeManager.secondaryTextColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                title,
                fontSize = if (isValueLarge) 18.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                color = ThemeManager.textColor
            )
            if (subtitle.isNotEmpty()) {
                Text(subtitle, fontSize = 11.sp, color = ThemeManager.secondaryTextColor)
            }
        }
    }
}

@Composable
private fun FieldEvidenceSection() {
    val textColor = ThemeManager.textColor
    val subTextColor = ThemeManager.secondaryTextColor

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = textColor, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.detail_field_evidence), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
            }
            Text(stringResource(R.string.detail_photo_counter, 1, 3), fontSize = 12.sp, color = subTextColor)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ThemeManager.borderColor)
        ) {
            Icon(
                Icons.Default.Image,
                contentDescription = null,
                tint = subTextColor,
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center)
            )

            // Etiqueta sobre la foto: siempre oscura con texto claro (se lee igual en ambos temas)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(6.dp),
                color = ColorCarbon.copy(alpha = 0.85f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("📍 -42.4728° S, -73.7831° W", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorPaper)
                    Text("Alt: 2.1m s.n.m. • 14-Sep-2026 11:45:18", fontSize = 10.sp, color = ColorPaper.copy(alpha = 0.9f))
                }
            }
        }
    }
}

@Composable
private fun SupervisorApprovalSection(
    selectedAction: String,
    onActionSelected: (String) -> Unit,
    commentsText: String,
    onCommentsChange: (String) -> Unit
) {
    val textColor = ThemeManager.textColor
    val subTextColor = ThemeManager.secondaryTextColor
    val borderColor = ThemeManager.borderColor
    val isDark = ThemeManager.isDarkMode

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ThemeManager.surfaceColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = ColorAbyss) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = ColorPaper,
                            modifier = Modifier
                                .padding(6.dp)
                                .size(18.dp)
                        )
                    }
                    Column {
                        Text(stringResource(R.string.detail_approval_panel), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                        Text(stringResource(R.string.detail_quality_resolution), fontSize = 12.sp, color = subTextColor)
                    }
                }
                Text(stringResource(R.string.detail_supervisor_role), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = subTextColor)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton(
                    text = stringResource(R.string.detail_validate),
                    icon = Icons.Default.CheckCircle,
                    isSelected = selectedAction == "Validar",
                    activeColor = Color(0xFF00796B),
                    modifier = Modifier.weight(1f),
                    onClick = { onActionSelected("Validar") }
                )
                ActionButton(
                    text = stringResource(R.string.detail_observe),
                    icon = Icons.Default.Warning,
                    isSelected = selectedAction == "Observar",
                    activeColor = Color(0xFFF57C00),
                    modifier = Modifier.weight(1f),
                    onClick = { onActionSelected("Observar") }
                )
                ActionButton(
                    text = stringResource(R.string.detail_correct),
                    icon = Icons.Default.Cancel,
                    isSelected = selectedAction == "Corregir",
                    activeColor = Color(0xFFD32F2F),
                    modifier = Modifier.weight(1f),
                    onClick = { onActionSelected("Corregir") }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.detail_supervisor_comments), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = subTextColor)
                    Text(stringResource(R.string.detail_comments_counter, commentsText.length, 300), fontSize = 11.sp, color = subTextColor)
                }
                TextField(
                    value = commentsText,
                    onValueChange = onCommentsChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = textColor),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = ThemeManager.backgroundColor,
                        unfocusedContainerColor = ThemeManager.backgroundColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = ColorEmber
                    )
                )
            }

            Text(
                text = stringResource(R.string.detail_legal_disclaimer),
                fontSize = 11.sp,
                color = subTextColor
            )

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    // En modo oscuro el azul marino casi no se distingue del fondo: se usa el acento
                    containerColor = if (isDark) ColorEmber else ColorAbyss,
                    contentColor = ColorPaper
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.detail_update_status_btn), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) activeColor else ThemeManager.backgroundColor,
            contentColor = if (isSelected) ColorPaper else ThemeManager.secondaryTextColor
        ),
        border = if (!isSelected) BorderStroke(1.dp, ThemeManager.borderColor) else null,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
