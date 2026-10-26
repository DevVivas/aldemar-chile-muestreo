package com.aldemar.aquamuestra.ui.components.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldemar.aquamuestra.ui.theme.AquaPaper
import com.aldemar.aquamuestra.ui.theme.AquaEmber
import com.aldemar.aquamuestra.ui.theme.AquaEmberSuave
import com.aldemar.aquamuestra.ui.theme.AquaGraphite

// 15. Tarjeta con información del usuario.
@Composable
fun UserCard(
    nombre: String,
    detalle: String,
    registrosOffline: Int
) {
    val iniciales = nombre.trim().take(2).uppercase()
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = AquaEmberSuave,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = AquaEmber,
                modifier = Modifier.size(40.dp)
            ) {
                Column(
                    modifier = Modifier.size(40.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = iniciales,
                        color = AquaPaper,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = nombre,
                    color = AquaEmber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = detalle,
                    color = AquaGraphite,
                    fontSize = 12.sp
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = AquaEmber
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudDone,
                        contentDescription = null,
                        tint = AquaPaper,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Offline Listo ($registrosOffline)",
                        color = AquaPaper,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
