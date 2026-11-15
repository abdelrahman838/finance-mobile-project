package com.example.mobilefinance.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobilefinance.ui.theme.ErrorRed
import com.example.mobilefinance.ui.theme.Green500
import com.example.mobilefinance.ui.theme.Green700
import com.example.mobilefinance.ui.theme.Green800
import com.example.mobilefinance.ui.theme.Ink
import com.example.mobilefinance.ui.theme.InkHint
import com.example.mobilefinance.ui.theme.InkLabel
import com.example.mobilefinance.ui.theme.InkMuted
import com.example.mobilefinance.ui.theme.Outline
import com.example.mobilefinance.ui.theme.OutlineFocused

private val FieldShape = RoundedCornerShape(18.dp)
private val CardShape = RoundedCornerShape(28.dp)

@Composable
fun AuthHeadline(
    firstLine: String,
    secondLine: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = buildAnnotatedString {
                append(firstLine)
                append("\n")
                append(secondLine)
                withStyle(SpanStyle(color = Green500)) { append(".") }
            },
            style = MaterialTheme.typography.displaySmall,
            color = Ink
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = InkMuted
        )
    }
}

@Composable
fun AuthCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Outline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            content = content
        )
    }
}

@Composable
fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    error: String? = null,
    isValid: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FieldLabel(label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = FieldShape,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Ink),
            placeholder = { Text(placeholder, color = InkHint) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = InkLabel) },
            trailingIcon = if (isValid && error == null) {
                { Icon(Icons.Outlined.CheckCircle, contentDescription = "Valid", tint = Green500) }
            } else null,
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onAny = { onImeAction() }),
            colors = fieldColors()
        )
        FieldError(error)
    }
}

@Composable
fun AuthPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    error: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {}
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        FieldLabel(label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = FieldShape,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Ink),
            placeholder = { Text(placeholder, color = InkHint) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = InkLabel) },
            trailingIcon = {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (visible) "Hide password" else "Show password",
                        tint = InkLabel
                    )
                }
            },
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
            keyboardActions = KeyboardActions(onAny = { onImeAction() }),
            colors = fieldColors()
        )
        FieldError(error)
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Green700,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = Green800,
            disabledContentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge, fontSize = 17.sp)
            Spacer(Modifier.width(10.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun AuthFooterLink(
    prompt: String,
    action: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(prompt, style = MaterialTheme.typography.bodyLarge, color = InkMuted)
        Spacer(Modifier.width(4.dp))
        Text(
            text = action,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = Ink,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .clickableNoRipple(onClick)
                .padding(vertical = 8.dp)
        )
    }
}

@Composable
fun FormErrorBanner(message: String?, modifier: Modifier = Modifier) {
    if (message == null) return
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = ErrorRed.copy(alpha = 0.08f)
    ) {
        Text(
            text = message,
            color = ErrorRed,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = InkLabel,
        modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
    )
}

@Composable
private fun FieldError(error: String?) {
    if (error == null) return
    Text(
        text = error,
        color = ErrorRed,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    errorContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = OutlineFocused,
    unfocusedBorderColor = Outline,
    errorBorderColor = ErrorRed,
    cursorColor = Green700,
    errorCursorColor = ErrorRed,
    focusedTextColor = Ink,
    unfocusedTextColor = Ink
)
