package com.approagency.base

import android.app.Activity
import androidx.activity.ComponentActivity
import com.approagency.base.config.ApproConfig
import com.approagency.base.local.room.dao.SessionDao
import com.approagency.base.model.network.Failure
import com.approagency.base.model.network.Resource
import com.approagency.base.network.networkCall
import com.approagency.base.network.service.ApproPrivateService
import com.approagency.base.paymnet.PaymentRequest
import com.approagency.base.paymnet.PaymentService
import com.approagency.base.utils.Logger
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

            Logger.info(
                TAG,
                "================ MYKET PURCHASE START ================"
            )

            Logger.info(
                TAG,
                buildString {
                    append("Config ")
                    append("flavor=${config.flavor}, ")
                    append("gateway=${config.flavor.gateway}, ")
                    append("package=${config.packageName}, ")
                    append("versionCode=${config.versionCode}, ")
                    append("debug=${config.debug}, ")
                    append("paymentAvailable=${config.isPaymentAvailable}, ")
                    append("rsaLength=${config.paymentRsaKey.length}")
                }
            )

            Logger.info(
                TAG,
                buildString {
                    append("Request ")
                    append("productId=${request.productId}, ")
                    append("productUuid=${request.productUuid}, ")
                    append("sessionId=${request.sessionId}, ")
                    append("customPayload=${request.payload != null}")
                }
            )

            try {
                /*
                 * STEP 1
                 */
                Logger.info(
                    TAG,
                    "STEP 1: Checking payment availability"
                )

                if (!config.isPaymentAvailable) {
                    Logger.error(
                        TAG,
                        "FAILED STEP 1: Payment is not available"
                    )

                    throw Failure.StoreUnavailable
                }

                Logger.info(
                    TAG,
                    "STEP 1: PASS"
                )

                /*
                 * STEP 2
                 */
                Logger.info(
                    TAG,
                    "STEP 2: Checking Myket installation package=$marketPackageName"
                )

                val myketInstalled =
                    activity.isPackageInstalled(marketPackageName)

                Logger.info(
                    TAG,
                    "STEP 2: Myket installed=$myketInstalled"
                )

                if (!myketInstalled) {
                    Logger.error(
                        TAG,
                        "FAILED STEP 2: Myket application is not installed"
                    )

                    throw Failure.InstallMyketApplication
                }

                /*
                 * STEP 3
                 */
                Logger.info(
                    TAG,
                    "STEP 3: Loading session id=${request.sessionId}"
                )

                val session = sessionDao.get(request.sessionId)

                if (session == null) {
                    Logger.error(
                        TAG,
                        "FAILED STEP 3: Session was not found"
                    )

                    throw Failure.Unauthorized
                }

                Logger.info(
                    TAG,
                    buildString {
                        append("STEP 3: Session loaded ")
                        append("phone=${maskPhone(session.phoneNumber)}, ")
                        append("premium=${session.isPremium}, ")
                        append(
                            "approToken=${
                                Logger.maskToken(session.approToken)
                            }"
                        )
                    }
                )

                /*
                 * STEP 4
                 */
                if (session.isPremium) {
                    Logger.error(
                        TAG,
                        "FAILED STEP 4: User already has subscription"
                    )

                    throw Failure.HaveSubscription
                }

                Logger.info(
                    TAG,
                    "STEP 4: Premium check PASS"
                )

                val phoneNumber = session.phoneNumber
                val versionCode = config.versionCode

                val payload =
                    request.payload ?: "$phoneNumber|$versionCode"

                Logger.info(
                    TAG,
                    buildString {
                        append("STEP 5: Payload prepared ")
                        append("length=${payload.length}, ")
                        append("custom=${request.payload != null}")
                    }
                )

                /*
                 * STEP 6
                 */
                Logger.info(
                    TAG,
                    "STEP 6: Creating IabHelper"
                )

                val helper = withContext(
                    Dispatchers.Main.immediate
                ) {
                    Logger.debug(
                        TAG,
                        "Creating IabHelper on thread=${Thread.currentThread().name}"
                    )

                    IabHelper(
                        activity,
                        config.paymentRsaKey
                    ).apply {
                        enableDebugLogging(config.debug)
                    }
                }

                Logger.info(
                    TAG,
                    "STEP 6: IabHelper created"
                )

                try {
                    /*
                     * STEP 7
                     */
                    Logger.info(
                        TAG,
                        "STEP 7: Starting Myket billing setup"
                    )

                    withContext(
                        Dispatchers.Main.immediate
                    ) {
                        helper.awaitSetup()
                    }

                    Logger.info(
                        TAG,
                        "STEP 7: Myket billing setup SUCCESS"
                    )

                    /*
                     * STEP 8
                     */
                    Logger.info(
                        TAG,
                        "STEP 8: Checking subscriptionsSupported"
                    )

                    /*
                     * STEP 9
                     */
                    Logger.info(
                        TAG,
                        buildString {
                            append("STEP 9: Launching Myket purchase flow ")
                            append("sku=${request.productUuid}, ")
                            append("type=${IabHelper.ITEM_TYPE_SUBS}")
                        }
                    )

                    val purchase = withContext(
                        Dispatchers.Main.immediate
                    ) {
                        helper.awaitPurchase(
                            activity = activity,
                            sku = request.productUuid,
                            itemType = IabHelper.ITEM_TYPE_SUBS,
                            payload = payload
                        )
                    }

                    Logger.info(
                        TAG,
                        buildString {
                            append("STEP 9: Purchase callback SUCCESS ")
                            append("sku=${purchase.sku}, ")
                            append(
                                "token=${
                                    Logger.maskToken(
                                        purchase.token
                                    )
                                }, "
                            )
                            append(
                                "payloadLength=${
                                    purchase.developerPayload?.length ?: 0
                                }"
                            )
                        }
                    )

                    /*
                     * STEP 10
                     */
                    val payloadMatches =
                        purchase.developerPayload == payload

                    Logger.info(
                        TAG,
                        buildString {
                            append("STEP 10: Developer payload ${payloadMatches} ${purchase.developerPayload} ${payload}")
                            append("expectedLength=${payload.length}, ")
                            append(
                                "actualLength=${
                                    purchase.developerPayload?.length ?: 0
                                }"
                            )
                        }
                    )

                    /*
                     * STEP 11
                     */
                    val token = purchase.token.orEmpty()

                    Logger.info(
                        TAG,
                        buildString {
                            append("STEP 11: Purchase token ")
                            append("blank=${token.isBlank()}, ")
                            append("length=${token.length}, ")
                            append("masked=${Logger.maskToken(token)}")
                        }
                    )

                    if (token.isBlank()) {
                        Logger.error(
                            TAG,
                            "FAILED STEP 11: Purchase token is blank"
                        )

                        throw Failure.PurchaseCancelled
                    }

                    /*
                     * STEP 12
                     */
                    Logger.info(
                        TAG,
                        buildString {
                            append("STEP 12: Sending purchase to backend ")
                            append("package=${config.packageName}, ")
                            append("productId=${request.productId}, ")
                            append("gateway=${config.flavor.gateway}")
                        }
                    )

                    val response = service.subscribeProduct(
                        packageName = config.packageName,
                        productId = request.productId,
                        body = mapOf(
                            "purchase_token" to token,
                            "gateway" to config.flavor.gateway
                        )
                    )

                    Logger.info(
                        TAG,
                        buildString {
                            append("STEP 12: Backend response ")
                            append("code=${response.code()}, ")
                            append("success=${response.isSuccessful}")
                        }
                    )

                    if (response.isSuccessful) {
                        Logger.info(
                            TAG,
                            "================ MYKET PURCHASE SUCCESS ================"
                        )

                        "خرید با موفقیت انجام شد"
                    } else {
                        val errorBody = runCatching {
                            response.errorBody()?.string()
                        }.getOrNull()

                        Logger.error(
                            TAG,
                            buildString {
                                append("FAILED STEP 12: Backend rejected purchase ")
                                append("code=${response.code()}, ")
                                append("message=${response.message()}, ")
                                append("errorBody=$errorBody")
                            }
                        )

                        throw Failure.PurchaseFailed
                    }
                } finally {
                    /*
                     * Always dispose helper.
                     */
                    Logger.info(
                        TAG,
                        "Disposing IabHelper"
                    )

                    runCatching {
                        withContext(
                            Dispatchers.Main.immediate
                        ) {
                            helper.dispose()
                        }
                    }.onSuccess {
                        Logger.info(
                            TAG,
                            "IabHelper disposed successfully"
                        )
                    }.onFailure { throwable ->
                        Logger.error(
                            tag = TAG,
                            message = "IabHelper dispose FAILED",
                            throwable = throwable
                        )
                    }
                }
            } catch (throwable: Throwable) {

                Logger.error(
                    tag = TAG,
                    message = buildString {
                        append("================ MYKET PURCHASE FAILED ================ ")
                        append("type=${throwable::class.java.simpleName}, ")
                        append("message=${throwable.message}")
                    },
                    throwable = throwable
                )

                throw throwable
            }
        }
    }

    private suspend fun IabHelper.awaitSetup() {
        suspendCancellableCoroutine { continuation ->

            Logger.info(
                TAG,
                "awaitSetup(): calling IabHelper.startSetup()"
            )

            try {
                startSetup { result ->

                    Logger.info(
                        TAG,
                        buildString {
                            append("awaitSetup(): callback ")
                            append("response=${result.response}, ")
                            append("success=${result.isSuccess}, ")
                            append("failure=${result.isFailure}, ")
                            append("message=${result.message}")
                        }
                    )

                    if (!continuation.isActive) {
                        Logger.warning(
                            TAG,
                            "awaitSetup(): continuation is no longer active"
                        )

                        return@startSetup
                    }

                    if (result.isSuccess) {
                        Logger.info(
                            TAG,
                            "awaitSetup(): SUCCESS"
                        )

                        continuation.resume(Unit)
                    } else {
                        val failure = result.toFailure()

                        Logger.error(
                            TAG,
                            buildString {
                                append("awaitSetup(): FAILED ")
                                append("response=${result.response}, ")
                                append("message=${result.message}, ")
                                append(
                                    "mappedFailure=${
                                        failure::class.java.simpleName
                                    }"
                                )
                            }
                        )

                        continuation.resumeWithException(
                            failure
                        )
                    }
                }
            } catch (throwable: Throwable) {
                Logger.error(
                    tag = TAG,
                    message = "awaitSetup(): startSetup() THREW exception",
                    throwable = throwable
                )

                if (continuation.isActive) {
                    continuation.resumeWithException(
                        throwable
                    )
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

            Logger.info(
                TAG,
                buildString {
                    append("awaitPurchase(): BEFORE launchPurchaseFlow ")
                    append("sku=$sku, ")
                    append("itemType=$itemType, ")
                    append("payloadLength=${payload.length}, ")
                    append("activity=${activity::class.java.simpleName}")
                }
            )

            try {
                launchPurchaseFlow(
                    activity,
                    sku,
                    itemType,
                    { result, purchase ->

                        Logger.info(
                            TAG,
                            buildString {
                                append("awaitPurchase(): CALLBACK ")
                                append("response=${result.response}, ")
                                append("success=${result.isSuccess}, ")
                                append("failure=${result.isFailure}, ")
                                append("message=${result.message}, ")
                                append("purchaseNull=${purchase == null}")
                            }
                        )

                        if (!continuation.isActive) {
                            Logger.warning(
                                TAG,
                                "awaitPurchase(): continuation is no longer active"
                            )

                            return@launchPurchaseFlow
                        }

                        when {
                            result.isFailure -> {
                                val failure =
                                    result.toFailure()

                                Logger.error(
                                    TAG,
                                    buildString {
                                        append("awaitPurchase(): RESULT FAILURE ")
                                        append("response=${result.response}, ")
                                        append("message=${result.message}, ")
                                        append(
                                            "mappedFailure=${
                                                failure::class.java.simpleName
                                            }"
                                        )
                                    }
                                )

                                continuation.resumeWithException(
                                    failure
                                )
                            }

                            purchase == null -> {
                                Logger.error(
                                    TAG,
                                    "awaitPurchase(): purchase is NULL"
                                )

                                continuation.resumeWithException(
                                    Failure.PurchaseCancelled
                                )
                            }

                            else -> {
                                Logger.info(
                                    TAG,
                                    buildString {
                                        append("awaitPurchase(): SUCCESS ")
                                        append("sku=${purchase.sku}, ")
                                        append(
                                            "token=${
                                                Logger.maskToken(
                                                    purchase.token
                                                )
                                            }"
                                        )
                                    }
                                )

                                continuation.resume(
                                    purchase
                                )
                            }
                        }
                    },
                    payload
                )

                /*
                 * VERY IMPORTANT:
                 * If you see this but Myket UI does not appear,
                 * launchPurchaseFlow() returned without throwing.
                 */
                Logger.info(
                    TAG,
                    "awaitPurchase(): launchPurchaseFlow() call returned"
                )
            } catch (throwable: Throwable) {
                /*
                 * This is especially important.
                 * launchPurchaseFlow can potentially throw before
                 * Myket's UI appears.
                 */
                Logger.error(
                    tag = TAG,
                    message = buildString {
                        append("awaitPurchase(): launchPurchaseFlow() THREW ")
                        append("type=${throwable::class.java.simpleName}, ")
                        append("message=${throwable.message}")
                    },
                    throwable = throwable
                )

                if (continuation.isActive) {
                    continuation.resumeWithException(
                        throwable
                    )
                }
            }
        }
    }

    private fun IabResult.toFailure(): Failure {
        Logger.error(
            TAG,
            buildString {
                append("Mapping IabResult ")
                append("response=$response, ")
                append("message=$message")
            }
        )

        val failure = when (response) {
            IabHelper.BILLING_RESPONSE_RESULT_USER_CANCELED,
            IabHelper.IABHELPER_USER_CANCELLED -> {
                Failure.PurchaseCancelled
            }

            IabHelper.BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE -> {
                Failure.InstallMyketApplication
            }

            IabHelper.BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE -> {
                Failure.NotFound
            }

            else -> {
                Failure.PurchaseFailed
            }
        }

        Logger.error(
            TAG,
            "IabResult mapped response=$response -> ${failure::class.java.simpleName}"
        )

        return failure
    }

    private fun maskPhone(
        phoneNumber: String?
    ): String {
        if (phoneNumber.isNullOrBlank()) {
            return "null"
        }

        if (phoneNumber.length <= 4) {
            return "***"
        }

        return "***${phoneNumber.takeLast(4)}"
    }

    companion object {
        private const val TAG = "MyketPayment"
    }
}