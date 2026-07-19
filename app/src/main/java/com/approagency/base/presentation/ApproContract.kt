package com.approagency.base.presentation

import androidx.activity.ComponentActivity
import com.approagency.base.model.ui.UiState
import com.approagency.base.model.session.Session
import com.approagency.base.model.ui.AuthStep
import com.approagency.base.model.user.Product
import com.approagency.base.model.user.UserStatus
import com.approagency.base.paymnet.PaymentRequest

class ApproContract {

    sealed class Event : ViewEvent {
        data class Login(val phoneNumber: String) : Event()
        data object CheckStatus : Event()
        data class CheckOtp(
            val phoneNumber: String,
            val otp: String,
            val sessionId: String = Session.ID
        ) : Event()

        data class OnOtpChanged(val otp: String) : Event()
        data class OnPhoneNumberChanged(val phoneNumber: String) : Event()
        data object GetProducts : Event()
        data class Purchase(
            val activity: ComponentActivity,
            val paymentRequest: PaymentRequest,
        ) : Event()

        data object ResetLoginState : Event()
        data object EditPhoneNumber : Event()
        data object ResetPurchaseState : Event()
        data class SendFCMToken(val token: String) : Event()
    }

    data class State(
        val step: AuthStep = AuthStep.Phone,
        val phoneNumber: String = "",
        val otp: String = "",
        val loginState: UiState<Unit> = UiState.Idle(),
        val otpState: UiState<Session> = UiState.Idle(),
        val statusState: UiState<UserStatus> = UiState.Loading(),
        val productsState: UiState<List<Product>> = UiState.Loading(),
        val purchaseState: UiState<String> = UiState.Idle()
    ) : ViewState

    sealed class SideEffect : ViewSideEffect
}