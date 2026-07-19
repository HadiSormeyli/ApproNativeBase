package com.approagency.base.presentation

import androidx.activity.ComponentActivity
import androidx.lifecycle.viewModelScope
import com.approagency.base.config.ApproConfig
import com.approagency.base.model.network.Failure
import com.approagency.base.model.network.Resource
import com.approagency.base.model.session.Session
import com.approagency.base.model.ui.AuthStep
import com.approagency.base.model.ui.UiState
import com.approagency.base.network.repository.ApproRepository
import com.approagency.base.paymnet.PaymentRequest
import com.approagency.base.paymnet.PaymentService
import com.approagency.base.session.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ApproViewModel(
    private val repository: ApproRepository,
    private val sessionManager: SessionManager,
    private val config: ApproConfig,
    private val paymentService: PaymentService
) : BaseViewModel<ApproContract.Event, ApproContract.State, ApproContract.SideEffect>(
) {
    override fun setInitialState() = ApproContract.State()

    override fun onTriggerEvent(event: ApproContract.Event) {
        when (event) {
            is ApproContract.Event.Login -> loginUser(event.phoneNumber)
            ApproContract.Event.CheckStatus -> checkStatus()
            ApproContract.Event.GetProducts -> getProducts()
            is ApproContract.Event.Purchase -> purchase(
                activity = event.activity,
                paymentRequest = event.paymentRequest
            )

            ApproContract.Event.ResetLoginState -> resetAuthStates()
            ApproContract.Event.EditPhoneNumber -> {
                setState {
                    copy(
                        step = AuthStep.Phone,
                        loginState = UiState.Idle(),
                        otpState = UiState.Idle(),
                        otp = ""
                    )
                }
            }

            ApproContract.Event.ResetPurchaseState -> resetPurchaseState()
            is ApproContract.Event.CheckOtp -> checkOtp(
                event.phoneNumber,
                event.otp,
                event.sessionId
            )

            is ApproContract.Event.OnOtpChanged -> {
                setState {
                    copy(
                        otp = event.otp
                    )
                }
            }

            is ApproContract.Event.OnPhoneNumberChanged -> {
                setState {
                    copy(
                        phoneNumber = event.phoneNumber
                    )
                }
            }

            is ApproContract.Event.SendFCMToken -> sendFCMToken(event.token)
        }
    }


    private fun sendFCMToken(token: String) {
        viewModelScope.launch {
            repository.sendFCMToken(token, config.packageName).collectLatest { }
        }
    }

    private fun purchase(
        activity: ComponentActivity,
        paymentRequest: PaymentRequest
    ) {
        viewModelScope.launch {
            paymentService.purchase(
                activity = activity,
                request = paymentRequest
            ).collectLatest {
                setState {
                    copy(
                        purchaseState = it.toUiState()
                    )
                }

                if (it is Resource.Success) {
                    checkStatus()
                }
            }
        }
    }

    private fun resetPurchaseState() {
        setState {
            copy(
                purchaseState = UiState.Idle()
            )
        }
    }

    private fun loginUser(
        phoneNumber: String
    ) {
        viewModelScope.launch {
            repository.login(mobile = phoneNumber, packageName = config.packageName)
                .collectLatest {
                    setState {
                        copy(
                            loginState = it.toUiState(),
                            otp = "",
                            step = if (it is Resource.Success) AuthStep.Otp else step
                        )
                    }
                }
        }
    }

    private fun checkOtp(
        phoneNumber: String,
        otp: String,
        sessionId: String,
    ) {
        viewModelScope.launch {
            repository.checkOtp(
                mobile = phoneNumber,
                token = otp
            ).collectLatest {
                when (it) {
                    is Resource.Error -> {
                        setState {
                            copy(
                                otpState = UiState.Error(it.error)
                            )
                        }
                    }

                    is Resource.Loading -> {
                        setState {
                            copy(
                                otpState = UiState.Loading()
                            )
                        }
                    }

                    is Resource.Success -> {
                        val data = it.data
                        val session = Session(
                            id = sessionId,
                            phoneNumber = data.user.mobile ?: "",
                            firstName = data.user.firstName ?: "",
                            lastName = data.user.lastName ?: "",
                            accessToken = data.token,
                            approToken = data.token,
                            isPremium = false,
                        )

                        setState {
                            copy(
                                otpState = UiState.Success(
                                    session
                                )
                            )
                        }
                        sessionManager.login(session)
                        getProducts()
                        checkStatus()
                    }
                }
            }
        }
    }

    private fun resetAuthStates() {
        setState {
            copy(
                step = AuthStep.Phone,
                loginState = UiState.Idle(),
                otpState = UiState.Idle(),
                phoneNumber = "",
                otp = "",
            )
        }
    }

    private fun getProducts() {
        viewModelScope.launch {
            repository.getProducts(config.packageName).collectLatest {
                setState {
                    copy(
                        productsState = it.toUiState()
                    )
                }
            }
        }
    }

    private fun checkStatus() {
        viewModelScope.launch {
            val session = sessionManager.getSession()

            if (
                session != null
                && session.approToken?.isNotEmpty() == true
                && session.accessToken?.isNotEmpty() == true
            ) {
                repository.getStatus(config.packageName).collectLatest {
                    when (it) {
                        is Resource.Error -> {
                            if (it.error.code == Failure.Unauthorized.code || it.error.code == Failure.Forbidden.code) {
                                sessionManager.logout()

                                setState {
                                    copy(
                                        step = AuthStep.Phone,
                                        otp = "",
                                        phoneNumber = "",
                                        statusState = UiState.Error(it.error)
                                    )
                                }
                            } else {
                                setState {
                                    copy(
                                        statusState = UiState.Error(it.error)
                                    )
                                }
                            }
                        }

                        is Resource.Loading -> {
                            setState {
                                copy(
                                    statusState = UiState.Loading()
                                )
                            }
                        }

                        is Resource.Success -> {
                            val data = it.data

                            val session = session.copy(
                                isPremium = data.isSubscribed
                            )
                            setState {
                                copy(
                                    statusState = UiState.Success(
                                        data
                                    )
                                )
                            }
                            sessionManager.login(session)
                        }
                    }
                }

            } else {
                sessionManager.logout()
            }
        }
    }
}