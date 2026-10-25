package com.aldemar.aquamuestra.ui.theme.form

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NewSampleFormScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var lengthMeters by remember { mutableFloatStateOf(0.50f) }
    var individualCount by remember { mutableIntStateOf(150) }
    var draftTime by remember { mutableStateOf("hace 2 min") }

    // Colores dinámicos para Modo Oscuro
    val bgColor = ThemeManager.backgroundColor
    val textColor = ThemeManager.textColor

    val currentDateTimeFormatted = remember {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        dateFormat.format(Date())
    }

    var operatorName by remember { mutableStateOf("D. Villamizar (TEC-4092)") }
    var concessionText by remember { mutableStateOf("Concesión 103B") }
    val capturedPhotos = remember { mutableStateListOf<Uri>() }

    var selectedCenter by remember { mutableStateOf("Centro Chiloé (Sector Huito)") }
    var selectedTren by remember { mutableStateOf("Tren 3 - Módulo B") }
    var selectedLine by remember { mutableStateOf("Línea 12") }
    var selectedSpecies by remember { mutableStateOf("Chorito (Mytilus chilensis)") }
    var observationsText by remember { mutableStateOf("Crecimiento homogéneo, baja presencia de epibiontes...") }

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPhotoForPreview by remember { mutableStateOf<Uri?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val msgPhotoSuccess = stringResource(R.string.msg_photo_success)
    val msgPermissionDenied = stringResource(R.string.msg_photo_permission_denied)
    val msgMaxPhotos = stringResource(R.string.msg_max_photos_reached)
    val msgDraftSaved = stringResource(R.string.msg_draft_saved_success)
    val msgFinishReg = stringResource(R.string.msg_finish_reg_success)
    val msgPhotoDeleted = stringResource(R.string.msg_photo_deleted)
    val msgRequiredError = stringResource(R.string.msg_required_fields_error)

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            capturedPhotos.add(tempPhotoUri!!)
            draftTime = "hace un momento"
            scope.launch {
                snackbarHostState.showSnackbar(String.format(msgPhotoSuccess, capturedPhotos.size))
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = createTempImageUri(context)
            tempPhotoUri = uri
            takePictureLauncher.launch(uri)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(msgPermissionDenied)
            }
        }
    }

    val centersList = listOf("Centro Chiloé (Sector Huito)", "Punta Pelluco • Módulo 104", "Calbuco Sur", "Reloncaví Estuario")
    val trenList = listOf("Tren 1 - Módulo A", "Tren 2 - Módulo B", "Tren 3 - Módulo B", "Tren 4 - Módulo C")
    val lineList = listOf("Línea 10", "Línea 12", "Línea 15", "Línea 18")
    val speciesList = listOf("Chorito (Mytilus chilensis)", "Salmón Atlántico (Salmo salar)", "Trucha Arcoíris (Oncorhynchus mykiss)")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = bgColor,
        topBar = { TopAppBar(onBackClick = onBackClick) },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
                Snackbar(
                    containerColor = ColorAbyss,
                    contentColor = ColorPaper,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = data.visuals.message,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            DraftStatusBar(draftTime = draftTime)

            Spacer(modifier = Modifier.height(24.dp))
            LocationAndOperatorSection(
                currentDateTime = currentDateTimeFormatted,
                operatorName = operatorName,
                onOperatorChange = { operatorName = it; draftTime = "hace un momento" },
                selectedCenter = selectedCenter,
                onCenterSelected = { selectedCenter = it; draftTime = "hace un momento" },
                centersList = centersList,
                concessionText = concessionText,
                onConcessionChange = { concessionText = it; draftTime = "hace un momento" },
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
                capturedPhotos = capturedPhotos,
                onPhotoClick = { uri -> selectedPhotoForPreview = uri },
                onAddPhotoClick = {
                    if (capturedPhotos.size >= 5) {
                        scope.launch {
                            snackbarHostState.showSnackbar(msgMaxPhotos)
                        }
                    } else {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            val uri = createTempImageUri(context)
                            tempPhotoUri = uri
                            takePictureLauncher.launch(uri)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        draftTime = "hace un momento"
                        scope.launch {
                            snackbarHostState.showSnackbar(msgDraftSaved)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ThemeManager.borderColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
                ) {
                    Text(
                        text = stringResource(R.string.form_save_draft),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.01).sp
                    )
                }

                Button(
                    onClick = {
                        val isOperatorValid = operatorName.trim().isNotEmpty()
                        val isConcessionValid = concessionText.trim().isNotEmpty()
                        val isPhotoValid = capturedPhotos.isNotEmpty()

                        if (isOperatorValid && isConcessionValid && isPhotoValid) {
                            scope.launch {
                                snackbarHostState.showSnackbar(msgFinishReg)
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(msgRequiredError)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorEmber,
                        contentColor = ColorPaper
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.form_finish_reg),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.01).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (selectedPhotoForPreview != null) {
        PhotoPreviewDialog(
            photoUri = selectedPhotoForPreview!!,
            onDismiss = { selectedPhotoForPreview = null },
            onDelete = {
                capturedPhotos.remove(selectedPhotoForPreview)
                selectedPhotoForPreview = null
                draftTime = "hace un momento"
                scope.launch {
                    snackbarHostState.showSnackbar(msgPhotoDeleted)
                }
            }
        )
    }
}

private fun createTempImageUri(context: Context): Uri {
    val imageFile = File.createTempFile(
        "sample_${System.currentTimeMillis()}_",
        ".jpg",
        context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}

@Composable
private fun LocationAndOperatorSection(
    currentDateTime: String,
    operatorName: String,
    onOperatorChange: (String) -> Unit,
    selectedCenter: String,
    onCenterSelected: (String) -> Unit,
    centersList: List<String>,
    concessionText: String,
    onConcessionChange: (String) -> Unit,
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
        InfoFieldLocked(stringResource(R.string.form_datetime_label), currentDateTime, hasLock = true)

        CustomInputField(
            label = stringResource(R.string.form_operator_label),
            value = operatorName,
            onValueChange = onOperatorChange,
            isError = operatorName.trim().isEmpty()
        )

        InteractiveDropdownField(stringResource(R.string.form_center_label), selectedCenter, centersList, onCenterSelected)

        CustomInputField(
            label = stringResource(R.string.form_concession_label) + " *",
            value = concessionText,
            onValueChange = onConcessionChange,
            isError = concessionText.trim().isEmpty()
        )

        InteractiveDropdownField(stringResource(R.string.form_tren_label), selectedTren, trenList, onTrenSelected)
        InteractiveDropdownField(stringResource(R.string.form_line_label), selectedLine, lineList, onLineSelected)
        InteractiveDropdownField(stringResource(R.string.form_species_label), selectedSpecies, speciesList, onSpeciesSelected)
    }
}

@Composable
private fun CustomInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cardBg = ThemeManager.surfaceColor
    val textColor = ThemeManager.textColor
    val subTextColor = ThemeManager.secondaryTextColor
    val borderColor = ThemeManager.borderColor

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = subTextColor)
            if (isError) {
                Text("Requerido", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
            }
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(
                    width = 1.dp,
                    color = if (isError) Color(0xFFD32F2F) else borderColor,
                    shape = RoundedCornerShape(10.dp)
                ),
            shape = RoundedCornerShape(10.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = textColor, fontWeight = FontWeight.Medium),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = cardBg,
                unfocusedContainerColor = cardBg,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = ColorEmber
            ),
            singleLine = true
        )
    }
}

@Composable
private fun PhotoEvidenceSection(
    capturedPhotos: List<Uri>,
    onPhotoClick: (Uri) -> Unit,
    onAddPhotoClick: () -> Unit
) {
    val isPhotoEmpty = capturedPhotos.isEmpty()
    val textColor = ThemeManager.textColor
    val subTextColor = ThemeManager.secondaryTextColor
    val borderColor = ThemeManager.borderColor

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.form_evidence_title) + " *",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                letterSpacing = (-0.01).sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "${capturedPhotos.size}/5",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isPhotoEmpty) Color(0xFFD32F2F) else subTextColor,
                letterSpacing = (-0.01).sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ThemeManager.surfaceColor)
                .border(
                    width = if (isPhotoEmpty) 1.dp else 0.dp,
                    color = if (isPhotoEmpty) Color(0xFFD32F2F) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            if (capturedPhotos.isNotEmpty()) {
                val lastPhoto = capturedPhotos.last()
                Image(
                    painter = rememberAsyncImagePainter(lastPhoto),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onPhotoClick(lastPhoto) },
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = subTextColor,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("Fotografía requerida (*)", fontSize = 11.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (capturedPhotos.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                capturedPhotos.forEach { photoUri ->
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                            .clickable { onPhotoClick(photoUri) }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(photoUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        Button(
            onClick = onAddPhotoClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ThemeManager.surfaceColor, contentColor = textColor),
            border = BorderStroke(1.dp, if (isPhotoEmpty) Color(0xFFD32F2F) else borderColor),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(R.string.form_capture_photo),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.01).sp
            )
        }
    }
}

@Composable
private fun PhotoPreviewDialog(
    photoUri: Uri,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ThemeManager.surfaceColor,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dialog_preview_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeManager.textColor
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = ThemeManager.secondaryTextColor)
                    }
                }

                Image(
                    painter = rememberAsyncImagePainter(photoUri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ThemeManager.borderColor)
                    ) {
                        Text(stringResource(R.string.dialog_close), color = ThemeManager.textColor, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onDelete,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.dialog_delete), color = ColorPaper, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TopAppBar(onBackClick: () -> Unit) {
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
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_volver),
                    tint = ColorPaper
                )
            }
            Text(
                text = stringResource(R.string.form_title),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 48.dp),
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorPaper,
                letterSpacing = (-0.01).sp
            )
        }
    }
}

@Composable
private fun DraftStatusBar(draftTime: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.form_draft_saved), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ThemeManager.secondaryTextColor, letterSpacing = (-0.01).sp)
        Text(draftTime, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ColorSteel, letterSpacing = (-0.01).sp)
    }
}

@Composable
private fun InfoFieldLocked(label: String, value: String, hasLock: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ThemeManager.secondaryTextColor, letterSpacing = (-0.01).sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ThemeManager.surfaceColor)
                .border(1.dp, ThemeManager.borderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(value, modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ThemeManager.textColor, letterSpacing = (-0.01).sp)
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
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ThemeManager.secondaryTextColor, letterSpacing = (-0.01).sp)
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ThemeManager.surfaceColor, RoundedCornerShape(12.dp))
                    .border(1.dp, ThemeManager.borderColor, RoundedCornerShape(12.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedOption,
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ThemeManager.textColor,
                    letterSpacing = (-0.01).sp
                )
                Text("▾", fontSize = 14.sp, color = ThemeManager.textColor)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(ThemeManager.surfaceColor)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, fontSize = 14.sp, color = ThemeManager.textColor) },
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
    val textColor = ThemeManager.textColor
    val subTextColor = ThemeManager.secondaryTextColor
    val cardBg = ThemeManager.surfaceColor
    val borderColor = ThemeManager.borderColor

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.form_biometrics_title), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = textColor, letterSpacing = (-0.01).sp)
            Spacer(modifier = Modifier.weight(1f))
            Surface(shape = RoundedCornerShape(6.dp), color = cardBg) {
                Text(stringResource(R.string.form_sernapesca_norm), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = textColor, letterSpacing = (-0.01).sp)
            }
        }
        Text(stringResource(R.string.form_length_label), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = subTextColor, letterSpacing = (-0.01).sp)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (lengthMeters > 0.05f) onLengthChange(lengthMeters - 0.05f) }) {
                Icon(Icons.Default.Remove, contentDescription = stringResource(R.string.cd_disminuir), tint = textColor)
            }
            Text(
                text = String.format(java.util.Locale.US, "%.2f m", lengthMeters),
                modifier = Modifier.padding(horizontal = 24.dp),
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                letterSpacing = (-0.02).sp
            )
            IconButton(onClick = { onLengthChange(lengthMeters + 0.05f) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_aumentar), tint = textColor)
            }
        }
        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = cardBg, border = BorderStroke(1.dp, borderColor)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.form_manual_count_title), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor, letterSpacing = (-0.01).sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        stringResource(R.string.form_reset),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = subTextColor,
                        letterSpacing = (-0.01).sp,
                        modifier = Modifier.clickable { onCountChange(0) }
                    )
                }
                Text(stringResource(R.string.form_alive_units, individualCount), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor, letterSpacing = (-0.01).sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CounterButton("-5", Modifier.weight(1f)) { onCountChange((individualCount - 5).coerceAtLeast(0)) }
                    CounterButton("-1", Modifier.weight(1f)) { onCountChange((individualCount - 1).coerceAtLeast(0)) }
                    CounterButton("+1", Modifier.weight(1f)) { onCountChange(individualCount + 1) }
                    CounterButton("+5", Modifier.weight(1f), isAccent = true) { onCountChange(individualCount + 5) }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.form_observations_label), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = subTextColor, letterSpacing = (-0.01).sp)
            TextField(
                value = observationsText,
                onValueChange = onObservationsChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    letterSpacing = (-0.01).sp,
                    lineHeight = 18.sp
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg,
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
    val bgColor = if (isAccent) ColorEmber else ThemeManager.surfaceColor
    val contentColor = if (isAccent) ColorPaper else ThemeManager.textColor
    val borderColor = ThemeManager.borderColor

    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = contentColor
        ),
        border = if (!isAccent) BorderStroke(1.dp, borderColor) else null,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).sp)
    }
}