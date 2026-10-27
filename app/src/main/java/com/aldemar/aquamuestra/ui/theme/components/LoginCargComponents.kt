package com.aldemar.aquamuestra.ui.theme.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.ui.theme.ThemeManager

@Composable
fun LoginFormCard(
    onLoginClick: (user: String, password: String, keepSession: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    onFieldEdited: () -> Unit = {}
) {

    var user by rememberSaveable { mutableStateOf("") }
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
            UserField(
                value = user,
                onValueChange = { user = it; onFieldEdited() },
                placeholder = stringResource(R.string.label_email),
                keyboardType = KeyboardType.Email
            )

            PasswordField(
                value = password,
                onValueChange = { password = it; onFieldEdited() },
                placeholder = stringResource(R.string.label_password)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFD32F2F),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

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
