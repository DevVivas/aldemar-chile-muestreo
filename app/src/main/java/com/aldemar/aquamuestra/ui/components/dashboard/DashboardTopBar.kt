package com.aldemar.aquamuestra.ui.components.dashboard


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldemar.aquamuestra.ui.theme.AquaNavyButton
import com.aldemar.aquamuestra.ui.theme.AquaOutline
import com.aldemar.aquamuestra.ui.theme.AquaTeal
import com.aldemar.aquamuestra.ui.theme.AquaTealLight

// 14. Barra superior del dashboard.
@Composable
fun DashboardTopBar(
    centro: String,
    internet: String,
    onInternet: () -> Unit,
    onPerfil: () -> Unit
) {
    Surface(
        color = AquaNavyButton
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Dashboard",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Centro: $centro",
                    color = AquaOutline,
                    fontSize = 12.sp
                )
            }
            // Píldora de conexión
            Surface(
                modifier = Modifier
                    .clickable { onInternet() },
                shape = RoundedCornerShape(50),
                color = AquaTealLight
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (internet == "Con conexión") {
                        Icon(
                            imageVector = Icons.Outlined.CloudDone,
                            contentDescription = null,
                            tint = AquaTeal,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = "ONLINE",
                            color = AquaTeal,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.CloudSync,
                            contentDescription = null,
                            tint = AquaTeal,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = "OFFLINE ACTIVO",
                            color = AquaTeal,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = "Cerrar sesión",
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.clickable { onPerfil() }
            )
        }
    }
}
