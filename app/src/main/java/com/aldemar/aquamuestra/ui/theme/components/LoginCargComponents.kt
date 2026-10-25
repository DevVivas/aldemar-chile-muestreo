package com.aldemar.aquamuestra.ui.theme.components

import androidx.compose.foundation.border
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
import com.aldemar.aquamuestra.ui.theme.ThemeManager

@Composable
fun LoginFormCard(
    onLoginClick: (user: String, password: String, keepSession: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    var user by rememberSaveable { mutableStateOf("TEC-CHL-4092") }
    var password by rememberSaveable { mutableStateOf("") }
    var keepSession by rememberSaveable { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, ThemeManager.borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeManager.backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UserField(user, { user = it }, placeholder = stringResource(R.string.label_user))

            PasswordField(password, { password = it }, placeholder = stringResource(R.string.label_password))

            KeepSessionRow(
                checked = keepSession,
                onCheckedChange = { keepSession = it },
                title = stringResource(R.string.login_keep_session_title),
                description = stringResource(R.string.login_keep_session_desc)
            )

            PrimaryButton(
                text = stringResource(R.string.btn_login),
                onClick = { onLoginClick(user, password, keepSession) },
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
