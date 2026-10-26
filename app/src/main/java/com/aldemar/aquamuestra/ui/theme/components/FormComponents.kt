package com.aldemar.aquamuestra.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aldemar.aquamuestra.R
import com.aldemar.aquamuestra.ui.theme.ColorEmber
import com.aldemar.aquamuestra.ui.theme.ColorPaper
import com.aldemar.aquamuestra.ui.theme.ThemeManager

// Los colores de estructura (fondo, texto, bordes) vienen de ThemeManager,
// así cambian solos al activar el modo oscuro.

@Composable
fun AquaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val fieldBg = ThemeManager.surfaceColor
    val textColor = ThemeManager.textColor
    val hintColor = ThemeManager.secondaryTextColor

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(1.dp, ThemeManager.borderColor, RoundedCornerShape(12.dp)),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            letterSpacing = (-0.01).sp
        ),
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = hintColor
            )
        },
        trailingIcon = trailing,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = fieldBg,
            unfocusedContainerColor = fieldBg,
            disabledContainerColor = fieldBg,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = ColorEmber
        )
    )
}

@Composable
fun UserField(value: String, onValueChange: (String) -> Unit, placeholder: String = "") {
    AquaTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder
    )
}

@Composable
fun PasswordField(value: String, onValueChange: (String) -> Unit, placeholder: String = "") {
    var visible by remember { mutableStateOf(false) }
    AquaTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        keyboardType = KeyboardType.Password,
        visualTransformation = if (visible) VisualTransformation.None
        else PasswordVisualTransformation(),
        trailing = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff
                    else Icons.Outlined.Visibility,
                    contentDescription = null,
                    tint = ThemeManager.secondaryTextColor
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

    val surface = ThemeManager.surfaceColor
    val textColor = ThemeManager.textColor
    val subTextColor = ThemeManager.secondaryTextColor
    val borderColor = ThemeManager.borderColor

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(surface, RoundedCornerShape(12.dp))
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selected.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    letterSpacing = (-0.01).sp
                )
            }
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = stringResource(R.string.cd_cambiar_rol), tint = subTextColor)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(surface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(option.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                            Text(
                                option.description,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = subTextColor
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
                checkedColor = ColorEmber,
                uncheckedColor = ThemeManager.secondaryTextColor,
                checkmarkColor = ColorPaper
            )
        )
        Column(modifier = Modifier.padding(start = 4.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = ThemeManager.textColor, letterSpacing = (-0.01).sp)
            Text(description, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ThemeManager.secondaryTextColor, letterSpacing = (-0.01).sp)
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorEmber,
            contentColor = ColorPaper
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).sp)
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
    }
}
