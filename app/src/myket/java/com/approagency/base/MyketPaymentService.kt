package com.approagency.base.paymnet

import android.app.Activity
import androidx.activity.ComponentActivity
import com.approagency.base.config.ApproConfig
import com.approagency.base.local.room.dao.SessionDao
import com.approagency.base.model.network.Failure
import com.approagency.base.model.network.Resource
import com.approagency.base.network.networkCall
import com.approagency.base.network.service.ApproPrivateService
import com.approagency.base.utils.isPackageInstalled
import ir.myket.billingclient.IabHelper
import ir.myket.billingclient.util.IabResult
import ir.myket.billingclient.util.Purchase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MarketPaymentService(
    private val config: ApproConfig,
    private val sessionDao: SessionDao,
    private val service: ApproPrivateService,
    private val marketPackageName: String = "ir.mservices.market"
) : PaymentService {
    override fun purchase(
        activity: ComponentActivity,
        request: PaymentRequest
    ): Flow<Resource<String>> {
        return networkCall {
            if (!config.isPaymentAvailable) {
                throw Failure.StoreUnavailable
            }

            if (!activity.isPackageInstalled(marketPackageName)) {
                throw Failure.InstallMyketApplication
            }

            val session = sessionDao.get(request.sessionId)
                ?: throw Failure.Unauthorized

            if (session.isPremium) throw Failure.HaveSubscription

            val approToken = session.approToken
            val phoneNumber = session.phoneNumber
            val versionCode = config.versionCode

            val payload = request.payload ?: "$phoneNumber|$versionCode"

            val helper = withContext(Dispatchers.Main.immediate) {
                IabHelper(activity, config.paymentRsaKey).apply {
                    enableDebugLogging(config.debug)
                }
            }

            try {
                withContext(Dispatchers.Main.immediate) {
                    helper.awaitSetup()
                }

                if (!helper.subscriptionsSupported()) {
                    throw Failure.PurchaseFailed
                }

                val purchase = withContext(Dispatchers.Main.immediate) {
                    helper.awaitPurchase(
                        activity = activity,
                        sku = request.productUuid,
                        itemType = IabHelper.ITEM_TYPE_SUBS,
                        payload = payload
                    )
                }

                if (purchase.developerPayload != payload) {
                    throw Failure.PurchaseFailed
                }

                val token = purchase.token.orEmpty()

                if (token.isBlank()) {
                    throw Failure.PurchaseCancelled
                }

                val response = service.subscribeProduct(
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
                withContext(Dispatchers.Main.immediate) {
                    helper.dispose()
                }
            }
        }
    }

    private suspend fun IabHelper.awaitSetup() {
        suspendCancellableCoroutine { continuation ->
            startSetup { result ->
                if (!continuation.isActive) return@startSetup

                if (result.isSuccess) {
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(result.toFailure())
                }
            }
        }
    }

    private suspend fun IabHelper.awaitPurchase(
        activity: Activity,
        sku: String,
        itemType: String,
        payload: String
    ): Purchase {
        return suspendCancellableCoroutine { continuation ->
            launchPurchaseFlow(
                activity,
                sku,
                itemType,
                { result, purchase ->
                    if (!continuation.isActive) return@launchPurchaseFlow

                    when {
                        result.isFailure -> {
                            continuation.resumeWithException(result.toFailure())
                        }

                        purchase == null -> {
                            continuation.resumeWithException(Failure.PurchaseCancelled)
                        }

                        else -> {
                            continuation.resume(purchase)
                        }
                    }
                },
                payload
            )
        }
    }

    private fun IabResult.toFailure(): Failure {
        return when (response) {
            IabHelper.BILLING_RESPONSE_RESULT_USER_CANCELED,
            IabHelper.IABHELPER_USER_CANCELLED -> Failure.PurchaseCancelled

            IabHelper.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE -> Failure.InstallMyketApplication

            IabHelper.BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE -> Failure.NotFound

            else -> Failure.PurchaseFailed
        }
    }
}