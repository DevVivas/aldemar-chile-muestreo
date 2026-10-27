package com.aldemar.aquamuestra.ui.theme.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.ui.theme.AquaMuestraTheme
import com.aldemar.aquamuestra.ui.theme.SessionManager
import com.aldemar.aquamuestra.ui.theme.ThemeManager
import com.aldemar.aquamuestra.ui.theme.components.LoginFormCard
import com.aldemar.aquamuestra.ui.theme.components.LoginHeader

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit = {},
) {
    var hasError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeManager.backgroundColor)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoginHeader(modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(32.dp))

        LoginFormCard(
            errorMessage = if (hasError) stringResource(R.string.login_error_invalid) else null,
            onFieldEdited = { hasError = false },
            onLoginClick = { email, password, keepSession ->
                if (SessionManager.login(email, password, keepSession)) {
                    hasError = false
                    onLoginSuccess()
                } else {
                    hasError = true
                }
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 392, heightDp = 860)
@Composable
private fun LoginScreenPreview() {
    AquaMuestraTheme { LoginScreen() }
}
