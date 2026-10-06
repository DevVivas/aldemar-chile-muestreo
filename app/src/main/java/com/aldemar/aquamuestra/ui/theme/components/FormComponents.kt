package com.aldemar.aquamuestra.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.aldemar.aquamuestra.ui.theme.AquaNavy
import com.aldemar.aquamuestra.ui.theme.AquaSurfaceVariant
import com.aldemar.aquamuestra.ui.theme.AquaTeal
import com.aldemar.aquamuestra.ui.theme.AquaTextHint
import com.aldemar.aquamuestra.ui.theme.AquaTextSecondary

/** Fila superior de cada campo: etiqueta a la izquierda, ayuda a la derecha. */
@Composable
fun FieldLabel(
    label: String,
    hint: String,
    hintColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = AquaNavy)
        Text(hint, style = MaterialTheme.typography.labelSmall, color = hintColor)
    }
}

/** Campo con fondo suave, sin borde, ícono a la izquierda y slot a la derecha. */
@Composable
fun AquaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        textStyle = MaterialTheme.typography.titleMedium.copy(color = AquaNavy),
        leadingIcon = {
            Icon(leadingIcon, contentDescription = null, tint = AquaNavy)
        },
        trailingIcon = trailing,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = AquaSurfaceVariant,
            unfocusedContainerColor = AquaSurfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = AquaNavy
        )
    )
}

@Composable
fun UserField(value: String, onValueChange: (String) -> Unit) {
    AquaTextField(
        value = value,
        onValueChange = onValueChange,
        leadingIcon = Icons.Outlined.Badge,
        trailing = {
            Icon(Icons.Outlined.Verified, contentDescription = null, tint = AquaTeal)
        }
    )
}

@Composable
fun PasswordField(value: String, onValueChange: (String) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    AquaTextField(
        value = value,
        onValueChange = onValueChange,
        leadingIcon = Icons.Outlined.Lock,
        keyboardType = KeyboardType.Password,
        visualTransformation = if (visible) VisualTransformation.None
        else PasswordVisualTransformation(),
        trailing = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff
                    else Icons.Outlined.Visibility,
                    contentDescription = if (visible) "Ocultar contraseña"
                    else "Mostrar contraseña",
                    tint = AquaTextSecondary
                )
            }
        }
    )
}

data class RoleOption(val title: String, val description: String)

@Composable
fun RoleSelector(
    options: List<RoleOption>,
    selected: RoleOption,
    onSelected: (RoleOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AquaSurfaceVariant, RoundedCornerShape(16.dp))
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.ManageAccounts, contentDescription = null, tint = AquaTeal)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    selected.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = AquaNavy
                )
                Text(
                    selected.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = AquaTextSecondary
                )
            }
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Cambiar rol", tint = AquaNavy)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(option.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                option.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = AquaTextSecondary
                            )
                        }
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun KeepSessionRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = AquaNavy,
                uncheckedColor = AquaNavy
            )
        )
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, color = AquaNavy)
            Text(description, style = MaterialTheme.typography.labelSmall, color = AquaTextHint)
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        )
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
    }
}