package com.approagency.base.paymnet

import com.approagency.base.model.session.Session

data class PaymentRequest(
    val productId: Int,
    val productUuid: String,
    val type: PaymentProductType = PaymentProductType.SUBSCRIPTION,
    val sessionId: String = Session.ID,
    val payload: String? = null
)
