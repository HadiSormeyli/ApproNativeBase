package com.approagency.base.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.approagency.base.model.ui.UiState
import com.approagency.base.model.session.Session
import com.approagency.base.model.ui.AuthStep
import com.approagency.base.model.ui.SubscriptionBottomSheetText
import com.approagency.base.model.user.Product
import com.approagency.base.model.user.toPaymentRequest
import com.approagency.base.presentation.ApproContract
import com.approagency.base.presentation.ApproViewModel
import com.approagency.base.session.SessionState
import com.approagency.base.theme.LocalBaseActivity
import com.approagency.base.utils.OtpAutoFillBus
import com.approagency.base.utils.OtpAutofillController
import org.koin.compose.koinInject
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionBottomSheet(
    approViewModel: ApproViewModel = LocalBaseActivity.current.approViewModel,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier,
    text: SubscriptionBottomSheetText = remember { SubscriptionBottomSheetText() },
    onRulesClick: () -> Unit,
    sessionId: String = Session.ID
) {
    val activity = LocalBaseActivity.current
    val viewState by approViewModel.state
    val sessionState by activity.sessionManager.state.collectAsStateWithLifecycle()

    val isLoggedIn = sessionState is SessionState.Login

    DisposableEffect(approViewModel) {
        onDispose {
            approViewModel.setEvent(
                ApproContract.Event.ResetLoginState
            )

            approViewModel.setEvent(
                ApproContract.Event.ResetPurchaseState
            )
        }
    }

    ApproModalBottomSheet(
        title = if (isLoggedIn) {
            text.subscriptionTitle
        } else {
            text.loginTitle
        },
        onDismiss = {
            activity.hideSubscriptionBottomSheet()
            onDismiss()
        }
    ) {
        when (sessionState) {
            SessionState.Loading -> Unit

            SessionState.Logout -> LoginSheetContent(
                phoneNumber = viewState.phoneNumber,
                otp = viewState.otp,
                step = viewState.step,
                loginState = viewState.loginState,
                otpState = viewState.otpState,
                text = text,
                onPhoneNumberChange = {
                    approViewModel.setEvent(
                        ApproContract.Event.OnPhoneNumberChanged(it)
                    )
                },
                onOtpChange = {
                    approViewModel.setEvent(
                        ApproContract.Event.OnOtpChanged(it)
                    )
                },
                onSendPhone = {
                    approViewModel.setEvent(
                        ApproContract.Event.Login(
                            viewState.phoneNumber.trim()
                        )
                    )
                },
                onCheckOtp = {
                    approViewModel.setEvent(
                        ApproContract.Event.CheckOtp(
                            phoneNumber = viewState.phoneNumber.trim(),
                            otp = it,
                            sessionId = sessionId
                        )
                    )
                },
                onEditPhone = {
                    approViewModel.setEvent(
                        ApproContract.Event.ResetLoginState
                    )
                },
                onRulesClick = onRulesClick
            )

            is SessionState.Login -> ProductsSheetContent(
                productsState = viewState.productsState,
                purchaseState = viewState.purchaseState,
                text = text,
                modifier = modifier,
                onRetry = {
                    approViewModel.setEvent(
                        ApproContract.Event.GetProducts
                    )
                },
                onPurchase = {
                    approViewModel.setEvent(
                        ApproContract.Event.Purchase(
                            activity = activity,
                            paymentRequest = it.toPaymentRequest()
                        )
                    )
                },
                onResetPurchase = {
                    approViewModel.setEvent(
                        ApproContract.Event.ResetPurchaseState
                    )
                },
                onPurchaseSuccess = onDismiss
            )
        }
    }
}

@Composable
fun LoginSheetContent(
    phoneNumber: String,
    otp: String,
    step: AuthStep,
    loginState: UiState<Unit>,
    otpState: UiState<Session>,
    text: SubscriptionBottomSheetText,
    onPhoneNumberChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onSendPhone: () -> Unit,
    onCheckOtp: (String) -> Unit,
    onEditPhone: () -> Unit,
    onRulesClick: () -> Unit
) {
    val activity = LocalBaseActivity.current
    val otpAutoFillBus: OtpAutoFillBus = koinInject()

    val isSendingPhone = loginState is UiState.Loading
    val isCheckingOtp = otpState is UiState.Loading
    val isOtpStep = step == AuthStep.Otp

    DisposableEffect(isOtpStep) {
        val controller = activity as? OtpAutofillController

        if (isOtpStep) {
            controller?.startOtpAutofill()
        }

        onDispose {
            controller?.stopOtpAutofill()
        }
    }

    LaunchedEffect(Unit) {
        otpAutoFillBus.codes.collect { code ->
            onOtpChange(code)

            if (code.isNotBlank()) {
                onCheckOtp(code)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        when (step) {
            AuthStep.Phone -> {
                Text(
                    text = text.enterPhone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val focusRequester = remember {
                    FocusRequester()
                }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                CustomOutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    value = phoneNumber,
                    onValueChange = onPhoneNumberChange,
                    onDone = {
                        if (
                            phoneNumber.trim().length >= 11 &&
                            !isSendingPhone
                        ) {
                            onSendPhone()
                        }
                    },
                    maxLength = 11,
                    label = text.phoneLabel,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        textDirection = TextDirection.Ltr
                    ),
                    keyboardType = KeyboardType.Phone,
                    enabled = !isSendingPhone
                )

                if (loginState is UiState.Error) {
                    Text(
                        text = loginState.error.text.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                LoadingButton(
                    text = text.sendCode,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = phoneNumber.trim().length >= 11,
                    isLoading = isSendingPhone,
                    onClick = onSendPhone
                )
            }

            AuthStep.Otp -> {
                Text(
                    text = buildAnnotatedString {
                        append(text.otpPrefix)

                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            append(phoneNumber)
                        }

                        append(text.otpSuffix)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OtpTextField(
                    otpText = otp,
                    onOtpTextChange = { value, _ ->
                        onOtpChange(value)
                    },
                    onComplete = {
                        onCheckOtp(otp.trim())
                    },
                    enabled = !isCheckingOtp
                )

                if (otpState is UiState.Error) {
                    Text(
                        text = otpState.error.text.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                LoadingButton(
                    text = text.login,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = otp.trim().isNotEmpty(),
                    isLoading = isCheckingOtp,
                    onClick = {
                        onCheckOtp(otp.trim())
                    }
                )

                SimpleTextButton(
                    text = text.editPhone,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCheckingOtp,
                    onClick = onEditPhone
                )
            }
        }

        SimpleTextButton(
            text = text.rules,
            modifier = Modifier.fillMaxWidth(),
            onClick = onRulesClick
        )
    }
}

@Composable
fun ProductsSheetContent(
    productsState: UiState<List<Product>>,
    purchaseState: UiState<String>,
    text: SubscriptionBottomSheetText,
    onRetry: () -> Unit,
    onPurchase: (Product) -> Unit,
    onResetPurchase: () -> Unit,
    onPurchaseSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activity = LocalBaseActivity.current

    LaunchedEffect(purchaseState) {
        when (purchaseState) {
            is UiState.Error -> {
                activity.showSnackBar(
                    purchaseState.error.text
                )

                onResetPurchase()
            }

            is UiState.Success -> {
                activity.showSnackBar(
                    purchaseState.data
                )

                onResetPurchase()
                onPurchaseSuccess()
            }

            else -> Unit
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (productsState) {
            is UiState.Idle,
            is UiState.Loading -> {
                repeat(3) {
                    ShimmerContainer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(MaterialTheme.shapes.large)
                    )
                }
            }

            is UiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = productsState.error.text.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    FilledTextButton(
                        text = text.retry,
                        onClick = onRetry
                    )
                }
            }

            is UiState.Success -> {
                val products = productsState.data

                var selectedProduct by remember(products) {
                    mutableStateOf(products.firstOrNull())
                }

                if (products.isEmpty()) {
                    Text(
                        text = text.emptyProducts,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(products) { product ->
                                ProductItem(
                                    title = product.title!!,
                                    price = product.price,
                                    isSelected = selectedProduct == product,
                                    selectedText = text.selected,
                                    currencyText = text.currency,
                                    onClick = {
                                        selectedProduct = product
                                    }
                                )
                            }
                        }

                        LoadingButton(
                            text = text.purchase,
                            modifier = Modifier.fillMaxWidth(),
                            isLoading = purchaseState is UiState.Loading,
                            enabled = selectedProduct != null,
                            onClick = {
                                selectedProduct?.let {
                                    onPurchase(it)
                                }
                            }
                        )
                    }
                }

                LoadingDialog(
                    isLoading = purchaseState is UiState.Loading
                )
            }
        }
    }
}

@Composable
fun ProductItem(
    title: String,
    price: Number?,
    isSelected: Boolean,
    selectedText: String,
    currencyText: String,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        label = "productBorderColor"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        label = "productContainerColor"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 6.dp else 2.dp,
        label = "productElevation"
    )

    val priceText = remember(price) {
        price?.let {
            NumberFormat
                .getNumberInstance(Locale.US)
                .format(it)
        }.orEmpty()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        tonalElevation = elevation,
        border = BorderStroke(
            width = if (isSelected) {
                1.5.dp
            } else {
                1.dp
            },
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (isSelected) {
                    Text(
                        text = selectedText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = priceText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = currencyText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}