package com.aldemar.aquamuestra.ui.theme.detalle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

    Scaffold(
        containerColor = ColorPaper,
        bottomBar = { BottomNavBar(navController = navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            DetailTopAppBar(onBackClick = onBackClick)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                StatusBanner(sampleId = sampleId)
                BiometricSheetSection()
                FieldEvidenceSection()
                SupervisorApprovalSection(
                    selectedAction = selectedAction,
                    onActionSelected = { selectedAction = it },
                    commentsText = commentsText,
                    onCommentsChange = { commentsText = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DetailTopAppBar(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorAbyss)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {


        }
    }
}

@Composable
private fun StatusBanner(sampleId: String) {
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
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = ColorPaper,
                border = BorderStroke(1.dp, ColorMist)
            ) {
                Text(
                    text = "Lote #$sampleId",
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Assignment, contentDescription = null, tint = ColorInk, modifier = Modifier.size(20.dp))
                Text(
                    text = stringResource(R.string.detail_biometric_sheet),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorInk
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
                color = ColorFog,
                border = BorderStroke(1.dp, ColorMist)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Analytics, contentDescription = null, tint = ColorAbyss, modifier = Modifier.size(24.dp))
                        Column {
                            Text(stringResource(R.string.detail_density_estimated), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorGraphite)
                            Text(stringResource(R.string.detail_density_value), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ColorInk)
                        }
                    }
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF00796B)) {
                        Text(
                            "Óptima",
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
        color = ColorFog,
        border = BorderStroke(1.dp, ColorMist)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorGraphite)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                title,
                fontSize = if (isValueLarge) 18.sp else 14.sp,
                fontWeight = FontWeight.Bold,
                color = ColorInk
            )
            if (subtitle.isNotEmpty()) {
                Text(subtitle, fontSize = 11.sp, color = ColorGraphite)
            }
        }
    }
}

@Composable
private fun FieldEvidenceSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = ColorInk, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.detail_field_evidence), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ColorInk)
            }
            Text("Foto 1 de 3", fontSize = 12.sp, color = ColorGraphite)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ColorMist)
        ) {
            Icon(Icons.Default.Image, contentDescription = null, tint = ColorSteel, modifier = Modifier.size(64.dp).align(Alignment.Center))

            Surface(
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ColorFog,
        border = BorderStroke(1.dp, ColorMist)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = ColorAbyss) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ColorPaper, modifier = Modifier.padding(6.dp).size(18.dp))
                    }
                    Column {
                        Text(stringResource(R.string.detail_approval_panel), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorInk)
                        Text(stringResource(R.string.detail_quality_resolution), fontSize = 12.sp, color = ColorGraphite)
                    }
                }
                Text(stringResource(R.string.detail_supervisor_role), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorGraphite)
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
                    Text(stringResource(R.string.detail_supervisor_comments), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ColorGraphite)
                    Text("${commentsText.length} / 300", fontSize = 11.sp, color = ColorSteel)
                }
                TextField(
                    value = commentsText,
                    onValueChange = onCommentsChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .border(1.dp, ColorMist, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = ColorInk),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = ColorPaper,
                        unfocusedContainerColor = ColorPaper,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = ColorEmber
                    )
                )
            }

            Text(
                text = stringResource(R.string.detail_legal_disclaimer),
                fontSize = 11.sp,
                color = ColorGraphite
            )

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorAbyss, contentColor = ColorPaper),
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
            containerColor = if (isSelected) activeColor else ColorPaper,
            contentColor = if (isSelected) ColorPaper else ColorGraphite
        ),
        border = if (!isSelected) BorderStroke(1.dp, ColorMist) else null,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}