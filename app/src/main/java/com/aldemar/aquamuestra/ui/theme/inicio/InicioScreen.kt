package com.aldemar.aquamuestra.ui.theme.inicio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.ui.theme.AppRoutes
import com.aldemar.aquamuestra.ui.theme.ColorEmber
import com.aldemar.aquamuestra.ui.theme.ColorGraphite
import com.aldemar.aquamuestra.ui.theme.ColorPaper
import com.aldemar.aquamuestra.ui.theme.ThemeManager
import com.aldemar.aquamuestra.ui.theme.components.BottomNavBar

@Composable
fun InicioScreen(navController: NavController) {
    var showMenu by remember { mutableStateOf(false) }

    val isDark = ThemeManager.isDarkMode
    val backgroundColor = ThemeManager.backgroundColor
    val textColor = ThemeManager.textColor
    val subtitleColor = ThemeManager.secondaryTextColor

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.cd_opciones),
                        tint = textColor
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier
                        .width(240.dp)
                        .background(ThemeManager.surfaceColor)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (isDark) "Modo Claro" else "Modo Oscuro",
                                fontSize = 15.sp,
                                color = textColor
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = if (isDark) ColorEmber else ColorGraphite
                            )
                        },
                        trailingIcon = {
                            Switch(
                                checked = ThemeManager.isDarkMode,
                                onCheckedChange = { ThemeManager.isDarkMode = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ColorPaper,
                                    checkedTrackColor = ColorEmber
                                )
                            )
                        },
                        onClick = { ThemeManager.isDarkMode = !ThemeManager.isDarkMode }
                    )

                    HorizontalDivider(color = ThemeManager.borderColor)

                    DropdownMenuItem(
                        text = { Text("Sincronizar", fontSize = 15.sp, color = textColor) },
                        leadingIcon = { Icon(Icons.Default.Sync, contentDescription = null, tint = subtitleColor) },
                        onClick = { showMenu = false }
                    )

                    HorizontalDivider(color = ThemeManager.borderColor)

                    DropdownMenuItem(
                        text = { Text("Cerrar Sesión", fontSize = 15.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFD32F2F)) },
                        onClick = { showMenu = false }
                    )
                }
            }
        },
        bottomBar = { BottomNavBar(navController = navController) }, // LLamada correcta al BottomNavBar
        containerColor = backgroundColor
    ) { paddingValues: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.inicio_titulo),
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                letterSpacing = (-0.02).sp
            )
            Text(
                text = stringResource(R.string.inicio_subtitulo),
                fontSize = 16.sp,
                color = subtitleColor,
                letterSpacing = (-0.01).sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            ActionCard(
                title = stringResource(R.string.inicio_card_nueva_titulo),
                subtitle = stringResource(R.string.inicio_card_nueva_sub),
                icon = Icons.Filled.Add,
                containerColor = ColorEmber,
                contentColor = ColorPaper,
                subtitleColor = ColorPaper.copy(alpha = 0.9f),
                borderColor = ColorEmber,
                onClick = { navController.navigate(AppRoutes.NewSample.ruta) }
            )

            ActionCard(
                title = stringResource(R.string.inicio_card_historial_titulo),
                subtitle = stringResource(R.string.inicio_card_historial_sub),
                icon = Icons.AutoMirrored.Filled.List,
                containerColor = ThemeManager.surfaceColor,
                contentColor = textColor,
                subtitleColor = subtitleColor,
                borderColor = ThemeManager.borderColor,
                onClick = { navController.navigate(AppRoutes.Historial.ruta) }
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    subtitleColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    letterSpacing = (-0.01).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = subtitleColor,
                    letterSpacing = (-0.01).sp
                )
            }
        }
    }
}