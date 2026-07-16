package com.approagency.base.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.approagency.base.R

@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    errorText: String? = null,
    placeholder: String? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    labelStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    supportingTextStyle: TextStyle = MaterialTheme.typography.bodySmall,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error,
        errorCursorColor = MaterialTheme.colorScheme.error,
        errorSupportingTextColor = MaterialTheme.colorScheme.error,
        cursorColor = MaterialTheme.colorScheme.primary
    ),
    imeAction: ImeAction = ImeAction.Done,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyboardActions: KeyboardActions? = null,
    onDone: (() -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    maxLength: Int = 100,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    require(minLines in 1..maxLines)
    require(maxLength > 0)
    val keyboardController = LocalSoftwareKeyboardController.current

    val textState = remember {
        mutableStateOf(
            TextFieldValue(
                value,
                selection = TextRange(value.length)
            )
        )
    }

    LaunchedEffect(value) {
        if (value != textState.value.text) {
            textState.value = TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    OutlinedTextField(
        value = textState.value,
        onValueChange = {
            if (it.text.length <= maxLength) onValueChange(it.text)
        },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        shape = shape,
        colors = colors,
        textStyle = textStyle,
        isError = isError,
        singleLine = maxLines == 1,
        minLines = minLines,
        maxLines = maxLines,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        label = {
            Text(
                text = label,
                style = labelStyle
            )
        },
        placeholder = placeholder?.let {
            {
                Text(
                    text = it,
                    style = labelStyle
                )
            }
        },
        supportingText = errorText
            ?.takeIf { isError }
            ?.let {
                {
                    Text(
                        text = it,
                        modifier = Modifier.fillMaxWidth(),
                        style = supportingTextStyle,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Start
                    )
                }
            },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = keyboardActions ?: KeyboardActions(
            onDone = {
                keyboardController?.hide()
                onDone?.invoke()
            }
        )
    )
}

@Composable
fun OtpTextField(
    otpText: String,
    onOtpTextChange: (String, Boolean) -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    otpCount: Int = 5,
    size: Dp = 48.dp,
    focusedSize: Dp = 56.dp,
    spacing: Dp = 4.dp,
    borderWidth: Dp = 1.dp,
    shape: Shape = MaterialTheme.shapes.small,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
    focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor: Color = MaterialTheme.colorScheme.outline,
    focusedContainerColor: Color = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor: Color = MaterialTheme.colorScheme.surface,
    focusedTextColor: Color = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    emptyCharacter: String = "",
    focusedCharacter: String = "_",
    requestFocus: Boolean = true
) {
    require(otpCount > 0)
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var completedOtp by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(enabled, requestFocus) {
        if (enabled && requestFocus) focusRequester.requestFocus()
    }
    LaunchedEffect(otpText, otpCount) {
        if (otpText.length == otpCount && completedOtp != otpText) {
            completedOtp = otpText
            focusManager.clearFocus()
            onComplete()
        } else if (otpText.length < otpCount) {
            completedOtp = null
        }
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        BasicTextField(
            value = TextFieldValue(
                text = otpText,
                selection = TextRange(otpText.length)
            ),
            onValueChange = {
                val value = it.text.filter(Char::isDigit).take(otpCount)
                onOtpTextChange(value, value.length == otpCount)
            },
            modifier = modifier.focusRequester(focusRequester),
            enabled = enabled,
            textStyle = TextStyle(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = if (otpText.length == otpCount) ImeAction.Done else ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (otpText.length == otpCount) focusManager.clearFocus()
                }
            ),
            decorationBox = { innerTextField ->
                Box {
                    Row(
                        modifier = Modifier.animateContentSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(otpCount) { index ->
                            val isFocused = enabled && otpText.length == index
                            val animatedSize by animateDpAsState(
                                targetValue = if (isFocused) focusedSize else size,
                                animationSpec = tween(300),
                                label = "otpSize"
                            )
                            val character = when {
                                index < otpText.length -> otpText[index].toString()
                                isFocused -> focusedCharacter
                                else -> emptyCharacter
                            }
                            Box(
                                modifier = Modifier
                                    .size(animatedSize)
                                    .border(
                                        width = borderWidth,
                                        color = if (isFocused) focusedBorderColor else unfocusedBorderColor,
                                        shape = shape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = character,
                                    style = textStyle,
                                    color = if (isFocused) focusedTextColor else unfocusedTextColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                            if (index < otpCount - 1) Spacer(Modifier.width(spacing))
                        }
                    }
                    Box(Modifier.size(1.dp)) {
                        innerTextField()
                    }
                }
            }
        )
    }
}

@Composable
fun PasswordTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visibleIcon: Painter = painterResource(R.drawable.eye_on),
    hiddenIcon: Painter = painterResource(R.drawable.eye_off),
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    errorText: String? = null,
    shape: Shape = MaterialTheme.shapes.medium,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    labelStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    supportingTextStyle: TextStyle = MaterialTheme.typography.bodySmall,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error,
        errorCursorColor = MaterialTheme.colorScheme.error,
        errorSupportingTextColor = MaterialTheme.colorScheme.error,
        cursorColor = MaterialTheme.colorScheme.primary
    ),
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions? = null,
    onDone: (() -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    maxLength: Int = 100
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val textState = remember {
        mutableStateOf(
            TextFieldValue(
                value,
                selection = TextRange(value.length)
            )
        )
    }

    LaunchedEffect(value) {
        if (value != textState.value.text) {
            textState.value = TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        }
    }

    CustomOutlinedTextField(
        value = textState.value.text,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        isError = isError,
        errorText = errorText,
        shape = shape,
        textStyle = textStyle,
        labelStyle = labelStyle,
        supportingTextStyle = supportingTextStyle,
        colors = colors,
        imeAction = imeAction,
        keyboardType = KeyboardType.Password,
        keyboardActions = keyboardActions ?: KeyboardActions(
            onDone = {
                keyboardController?.hide()
                onDone?.invoke()
            }
        ),
        leadingIcon = leadingIcon,
        trailingIcon = {
            IconButton(
                enabled = enabled,
                onClick = {
                    passwordVisible = !passwordVisible
                }
            ) {
                Icon(
                    painter = if (passwordVisible) visibleIcon else hiddenIcon,
                    contentDescription = label,
                    tint = iconTint
                )
            }
        },
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        maxLength = maxLength
    )
}