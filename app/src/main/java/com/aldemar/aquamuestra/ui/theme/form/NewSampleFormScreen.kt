package com.aldemar.aquamuestra.ui.theme.form

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldemar.aquamuestra.ui.theme.AquaMuestraTheme

private val ColorPaper = Color(0xFFFFFFFF)
private val ColorAbyss = Color(0xFF000710)
private val ColorInk = Color(0xFF000000)
private val ColorGraphite = Color(0xFF60646c)
private val ColorFog = Color(0xFFF3F3F7)
private val ColorMist = Color(0xFFB9BBC6)
private val ColorEmber = Color(0xFFFF5900)
private val ColorSteel = Color(0xFF8B8D98)
private val ColorCarbon = Color(0xFF15191e)

@Composable
fun NewSampleFormScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    var lengthMeters by remember { mutableFloatStateOf(0.50f) }
    var individualCount by remember { mutableIntStateOf(150) }
    var photoCount by remember { mutableIntStateOf(1) }
    var draftTime by remember { mutableStateOf("hace 2 min") }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Interactive inputs state
    var selectedCenter by remember { mutableStateOf("Centro Chiloé (Sector Huito)") }
    var selectedTren by remember { mutableStateOf("Tren 3 - Módulo B") }
    var selectedLine by remember { mutableStateOf("Línea 12") }
    var selectedSpecies by remember { mutableStateOf("Chorito (Mytilus chilensis)") }
    var observationsText by remember { mutableStateOf("Crecimiento homogéneo, baja presencia de epibiontes, fijación sólida...") }

    val centersList = listOf("Centro Chiloé (Sector Huito)", "Punta Pelluco • Módulo 104", "Calbuco Sur", "Reloncaví Estuario")
    val trenList = listOf("Tren 1 - Módulo A", "Tren 2 - Módulo B", "Tren 3 - Módulo B", "Tren 4 - Módulo C")
    val lineList = listOf("Línea 10", "Línea 12", "Línea 15", "Línea 18")
    val speciesList = listOf("Chorito (Mytilus chilensis)", "Salmón Atlántico (Salmo salar)", "Trucha Arcoíris (Oncorhynchus mykiss)", "Ostra Chilena (Ostrea chilensis)")

    Column(modifier = modifier.fillMaxSize().background(ColorPaper)) {
        TopAppBar(onBackClick = onBackClick)
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            DraftStatusBar(draftTime = draftTime)
            
            if (successMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ColorEmber.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorEmber)
                ) {
                    Text(
                        text = successMessage!!,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorEmber,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            LocationSection(
                selectedCenter = selectedCenter,
                onCenterSelected = { selectedCenter = it; draftTime = "hace un momento" },
                centersList = centersList,
                selectedTren = selectedTren,
                onTrenSelected = { selectedTren = it; draftTime = "hace un momento" },
                trenList = trenList,
                selectedLine = selectedLine,
                onLineSelected = { selectedLine = it; draftTime = "hace un momento" },
                lineList = lineList,
                selectedSpecies = selectedSpecies,
                onSpeciesSelected = { selectedSpecies = it; draftTime = "hace un momento" },
                speciesList = speciesList
            )
            Spacer(modifier = Modifier.height(24.dp))
            BiometricsSection(
                lengthMeters = lengthMeters,
                onLengthChange = { lengthMeters = it; draftTime = "hace un momento" },
                individualCount = individualCount,
                onCountChange = { individualCount = it; draftTime = "hace un momento" },
                observationsText = observationsText,
                onObservationsChange = { observationsText = it; draftTime = "hace un momento" }
            )
            Spacer(modifier = Modifier.height(24.dp))
            PhotoEvidenceSection(
                photoCount = photoCount,
                onAddPhoto = {
                    if (photoCount < 5) photoCount++
                    draftTime = "hace un momento"
                    successMessage = "Fotografía agregada con éxito ($photoCount/5)"
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
        BottomActionBar(
            onSaveDraft = {
                draftTime = "hace un momento"
                successMessage = "¡Borrador guardado localmente con éxito!"
            },
            onFinishRegistration = {
                successMessage = "¡Registro finalizado y sincronizado en búfer offline!"
            }
        )
    }
}

@Composable
private fun TopAppBar(onBackClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(ColorAbyss).statusBarsPadding()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = ColorPaper) }
            Text("Form Nueva Muestra", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = ColorPaper, letterSpacing = (-0.01).sp)
            IconButton(onClick = {}) { Icon(Icons.Default.Person, contentDescription = "Perfil", tint = ColorPaper) }
            IconButton(onClick = {}) { Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = ColorPaper) }
        }
    }
}

@Composable
private fun DraftStatusBar(draftTime: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("Borrador guardado localmente", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ColorGraphite, letterSpacing = (-0.01).sp)
        Text(draftTime, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ColorSteel, letterSpacing = (-0.01).sp)
    }
}

@Composable
private fun LocationSection(
    selectedCenter: String,
    onCenterSelected: (String) -> Unit,
    centersList: List<String>,
    selectedTren: String,
    onTrenSelected: (String) -> Unit,
    trenList: List<String>,
    selectedLine: String,
    onLineSelected: (String) -> Unit,
    lineList: List<String>,
    selectedSpecies: String,
    onSpeciesSelected: (String) -> Unit,
    speciesList: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = ColorInk, modifier = Modifier.size(18.dp))
            Text("Informacion de Ubicacion", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = ColorInk, letterSpacing = (-0.01).sp)
            Spacer(modifier = Modifier.weight(1f))
            Surface(shape = RoundedCornerShape(6.dp), color = ColorFog) {
                Text("GPS FIJADO", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = ColorInk, letterSpacing = (-0.01).sp)
            }
        }
        InfoFieldLocked("Fecha y Hora de Muestreo (Automatico)", "20-10-2026 14:32", hasLock = true)
        InteractiveDropdownField("Centro de Cultivo", selectedCenter, centersList, onCenterSelected)
        InteractiveDropdownField("Tren / Modulo de Concesion", selectedTren, trenList, onTrenSelected)
        InteractiveDropdownField("Linea de Cultivo", selectedLine, lineList, onLineSelected)
        InteractiveDropdownField("Especie", selectedSpecies, speciesList, onSpeciesSelected)
    }
}

@Composable
private fun InfoFieldLocked(label: String, value: String, hasLock: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ColorGraphite, letterSpacing = (-0.01).sp)
        Row(modifier = Modifier.fillMaxWidth().background(ColorFog).border(1.dp, ColorMist, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(value, modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ColorInk, letterSpacing = (-0.01).sp)
            if (hasLock) Icon(Icons.Default.Lock, contentDescription = null, tint = ColorSteel, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun InteractiveDropdownField(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ColorGraphite, letterSpacing = (-0.01).sp)
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorFog, RoundedCornerShape(12.dp))
                    .border(1.dp, ColorMist, RoundedCornerShape(12.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedOption,
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorInk,
                    letterSpacing = (-0.01).sp
                )
                Text("▾", fontSize = 14.sp, color = ColorInk)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(ColorPaper)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, fontSize = 14.sp, color = ColorInk) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BiometricsSection(
    lengthMeters: Float,
    onLengthChange: (Float) -> Unit,
    individualCount: Int,
    onCountChange: (Int) -> Unit,
    observationsText: String,
    onObservationsChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Medicion y Biometria", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = ColorInk, letterSpacing = (-0.01).sp)
            Spacer(modifier = Modifier.weight(1f))
            Surface(shape = RoundedCornerShape(6.dp), color = ColorFog) {
                Text("NORMA SERNAPESCA", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = ColorInk, letterSpacing = (-0.01).sp)
            }
        }
        Text("Longitud del Tramo Medido", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ColorGraphite, letterSpacing = (-0.01).sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (lengthMeters > 0.05f) onLengthChange(lengthMeters - 0.05f) }) {
                Icon(Icons.Default.Remove, contentDescription = "Disminuir", tint = ColorInk)
            }
            Text(
                text = String.format(java.util.Locale.US, "%.2f m", lengthMeters),
                modifier = Modifier.padding(horizontal = 24.dp),
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = ColorInk,
                letterSpacing = (-0.02).sp
            )
            IconButton(onClick = { onLengthChange(lengthMeters + 0.05f) }) {
                Icon(Icons.Default.Add, contentDescription = "Aumentar", tint = ColorInk)
            }
        }
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = ColorFog, border = androidx.compose.foundation.BorderStroke(1.dp, ColorMist)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Conteo Manual de Individuos", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ColorInk, letterSpacing = (-0.01).sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "Reiniciar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorGraphite,
                        letterSpacing = (-0.01).sp,
                        modifier = Modifier.clickable { onCountChange(0) }
                    )
                }
                Text("EJEMPLARES VIVOS: $individualCount ind", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = ColorInk, letterSpacing = (-0.01).sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CounterButton("-5", Modifier.weight(1f)) { onCountChange((individualCount - 5).coerceAtLeast(0)) }
                    CounterButton("-1", Modifier.weight(1f)) { onCountChange((individualCount - 1).coerceAtLeast(0)) }
                    CounterButton("+1", Modifier.weight(1f)) { onCountChange(individualCount + 1) }
                    CounterButton("+5", Modifier.weight(1f), isAccent = true) { onCountChange(individualCount + 5) }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Observaciones Tecnicas en Balsa", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ColorGraphite, letterSpacing = (-0.01).sp)
            TextField(
                value = observationsText,
                onValueChange = onObservationsChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .border(1.dp, ColorMist, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorInk,
                    letterSpacing = (-0.01).sp,
                    lineHeight = 18.sp
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = ColorFog,
                    unfocusedContainerColor = ColorFog,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = ColorEmber
                )
            )
        }
    }
}

@Composable
private fun CounterButton(text: String, modifier: Modifier = Modifier, isAccent: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isAccent) ColorEmber else ColorPaper,
            contentColor = if (isAccent) ColorPaper else ColorInk
        ),
        border = if (!isAccent) androidx.compose.foundation.BorderStroke(1.dp, ColorMist) else null,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).sp)
    }
}

@Composable
private fun PhotoEvidenceSection(photoCount: Int, onAddPhoto: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Evidencia Fotografica", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = ColorInk, letterSpacing = (-0.01).sp)
            Spacer(modifier = Modifier.weight(1f))
            Text("$photoCount de 5 Fotos", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ColorGraphite, letterSpacing = (-0.01).sp)
        }
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)).background(ColorMist)) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = ColorSteel, modifier = Modifier.size(48.dp).align(Alignment.Center))
            Surface(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp), shape = RoundedCornerShape(6.dp), color = ColorCarbon.copy(alpha = 0.8f)) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text("Geostampa", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = ColorPaper, letterSpacing = (-0.01).sp)
                    Text("-42.4728, -73.7831", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = ColorPaper.copy(alpha = 0.9f), letterSpacing = (-0.01).sp)
                }
            }
        }
        Button(
            onClick = onAddPhoto,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorPaper, contentColor = ColorInk),
            border = androidx.compose.foundation.BorderStroke(1.dp, ColorMist),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Capturar Fotografia Adicional", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).sp)
        }
    }
}

@Composable
private fun BottomActionBar(onSaveDraft: () -> Unit, onFinishRegistration: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().background(ColorAbyss).navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onSaveDraft,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = ColorPaper),
            border = androidx.compose.foundation.BorderStroke(1.dp, ColorPaper.copy(alpha = 0.3f)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text("Guardar Borrador", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).sp)
        }
        Button(
            onClick = onFinishRegistration,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorEmber, contentColor = ColorPaper),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text("Finalizar Registro", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).sp)
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun NewSampleFormPreview() {
    AquaMuestraTheme { NewSampleFormScreen() }
}
