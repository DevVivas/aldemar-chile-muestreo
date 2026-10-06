package com.aldemar.aquamuestra.ui.theme.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.ui.theme.AquaSurface
import com.aldemar.aquamuestra.ui.theme.AquaTeal
import com.aldemar.aquamuestra.ui.theme.AquaTextHint

@Composable
fun LoginFormCard(
    onLoginClick: (user: String, password: String, role: RoleOption, keepSession: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Roles de ejemplo: luego se pueden cargar desde la tabla ROL
    val roles = listOf(
        RoleOption(
            stringResource(R.string.role_sampler),
            stringResource(R.string.role_sampler_desc)
        ),
        RoleOption("Revisor de Calidad", "Validación de muestras y fotos"),
        RoleOption("Supervisor de Centro", "Seguimiento y reportes")
    )

    var user by rememberSaveable { mutableStateOf("TEC-CHL-4092") }
    var password by rememberSaveable { mutableStateOf("") }
    var roleIndex by rememberSaveable { mutableStateOf(0) }
    var keepSession by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AquaSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FieldLabel(stringResource(R.string.label_user), stringResource(R.string.label_required), AquaTeal)
            UserField(user) { user = it }

            FieldLabel(
                stringResource(R.string.label_password),
                stringResource(R.string.label_pin),
                AquaTextHint,
                modifier = Modifier.padding(top = 8.dp)
            )
            PasswordField(password) { password = it }

            FieldLabel(
                stringResource(R.string.label_role),
                "",
                AquaTextHint,
                modifier = Modifier.padding(top = 8.dp)
            )
            RoleSelector(
                options = roles,
                selected = roles[roleIndex],
                onSelected = { roleIndex = roles.indexOf(it) }
            )

            KeepSessionRow(
                checked = keepSession,
                onCheckedChange = { keepSession = it },
                title = stringResource(R.string.keep_session),
                description = stringResource(R.string.keep_session_desc),
                modifier = Modifier.padding(top = 8.dp)
            )

            PrimaryButton(
                text = stringResource(R.string.btn_login),
                onClick = { onLoginClick(user, password, roles[roleIndex], keepSession) },
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}