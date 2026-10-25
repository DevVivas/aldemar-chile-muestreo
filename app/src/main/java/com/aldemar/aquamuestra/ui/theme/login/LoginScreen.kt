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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aldemar.aquamuestra.ui.theme.AquaMuestraTheme
import com.aldemar.aquamuestra.ui.theme.components.LoginHeader
import com.aldemar.aquamuestra.ui.theme.components.LoginFormCard

private val ColorPaper = Color(0xFFFFFFFF)

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginClick: (user: String, password: String, keepSession: Boolean) -> Unit = { _, _, _ -> },
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorPaper)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoginHeader(modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(32.dp))

        LoginFormCard(
            onLoginClick = onLoginClick
        )
    }
}

@Preview(showBackground = true, widthDp = 392, heightDp = 860)
@Composable
private fun LoginScreenPreview() {
    AquaMuestraTheme { LoginScreen() }
}
