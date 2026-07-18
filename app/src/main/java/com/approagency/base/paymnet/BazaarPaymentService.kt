package com.approagency.base.paymnet

import androidx.activity.ComponentActivity
import com.approagency.base.config.ApproConfig
import com.approagency.base.local.room.dao.SessionDao
import com.approagency.base.model.network.Failure
import com.approagency.base.model.network.Resource
import com.approagency.base.network.networkCall
import com.approagency.base.network.service.ApproService
import com.approagency.base.utils.isPackageInstalled
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import ir.cafebazaar.poolakey.entity.PurchaseInfo
import ir.cafebazaar.poolakey.request.PurchaseRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

class BazaarPaymentService(
    private val config: ApproConfig,
    private val sessionDao: SessionDao,
    private val approService: ApproService,
    private val marketPackageName: String = "com.farsitel.bazaar"
) : PaymentService {

    override fun purchase(
        activity: ComponentActivity,
        request: PaymentRequest
    ): Flow<Resource<String>> {
        return networkCall {
            if (config.isPaymentAvailable) {
                throw Failure.StoreUnavailable
            }

            if (!activity.isPackageInstalled(marketPackageName)) {
                throw Failure.InstallBazarApplication
            }

            val session = sessionDao.get(request.sessionId)
                ?: throw Failure.Unauthorized

            val approToken = session.approToken
            val phoneNumber = session.phoneNumber
            val versionCode = config.versionCode
            val payload = request.payload ?: "$phoneNumber|$versionCode"

            val payment = Payment(
                context = activity,
                config = PaymentConfiguration(
                    localSecurityCheck = SecurityCheck.Enable(
                        rsaPublicKey = config.paymentRsaKey
                    ),
                    shouldSupportSubscription = true
                )
            )

            val connection = payment.awaitConnection()

            try {
                val purchaseInfo = payment.awaitSubscribe(
                    activity = activity,
                    productUuid = request.productUuid,
                    payload = payload
                )

                if (purchaseInfo.payload != payload) {
                    throw Failure.PurchaseFailed
                }

                val token = purchaseInfo.purchaseToken

                if (token.isBlank()) {
                    throw Failure.PurchaseCancelled
                }

                val response = approService.subscribeProduct(
                    packageName = config.packageName,
                    productId = request.productId,
                    body = mapOf(
                        "purchase_token" to token,
                        "gateway" to config.flavor.gateway
                    )
                )

                if (response.isSuccessful) {
                    "خرید با موفقیت انجام شد"
                } else {
                    throw Failure.PurchaseFailed
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    private suspend fun Payment.awaitConnection(): Connection {
        return suspendCancellableCoroutine { continuation ->
            lateinit var connection: Connection

            connection = connect {
                connectionSucceed {
                    if (continuation.isActive) {
                        continuation.resume(connection) { _, _, _ -> }
                    }
                }

                connectionFailed { throwable ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(
                            Failure.PurchaseFailed
                        )
                    }
                }

                disconnected {
                    // Do not fail here automatically.
                    // We disconnect manually in finally after purchase.
                }
            }

            continuation.invokeOnCancellation {
                connection.disconnect()
            }
        }
    }

    private suspend fun Payment.awaitSubscribe(
        activity: ComponentActivity,
        productUuid: String,
        payload: String?
    ): PurchaseInfo {
        return suspendCancellableCoroutine { continuation ->
            subscribeProduct(
                registry = activity.activityResultRegistry,
                request = PurchaseRequest(
                    productId = productUuid,
                    payload = payload
                )
            ) {
                purchaseFlowBegan {
                }

                failedToBeginFlow { throwable ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(
                            Failure.PurchaseFailed
                        )
                    }
                }

                purchaseSucceed { purchaseInfo ->
                    if (continuation.isActive) {
                        continuation.resume(purchaseInfo) { _, _, _ -> }
                    }
                }

                purchaseCanceled {
                    if (continuation.isActive) {
                        continuation.resumeWithException(
                            Failure.PurchaseCancelled
                        )
                    }
                }

                purchaseFailed { throwable ->
                    if (continuation.isActive) {
                        continuation.resumeWithException(
                            Failure.PurchaseFailed
                        )
                    }
                }
            }
        }
    }
}
