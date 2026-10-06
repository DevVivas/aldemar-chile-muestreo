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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aldemar.aquamuestra.ui.theme.AquaMuestraTheme
import com.aldemar.aquamuestra.ui.theme.components.LoginHeader
import com.aldemar.aquamuestra.ui.theme.components.LoginFormCard

@Composable
fun LoginScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoginHeader(modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(24.dp))

        LoginFormCard(
            onLoginClick = { user, password, role, keepSession ->
                // Paso posterior: validación y navegación
            }
        )
        // Paso 4: tarjetas informativas
        // Paso 5: footer
    }
}

@Preview(showBackground = true, widthDp = 392, heightDp = 860)
@Composable
private fun LoginScreenPreview() {
    AquaMuestraTheme { LoginScreen() }
}
