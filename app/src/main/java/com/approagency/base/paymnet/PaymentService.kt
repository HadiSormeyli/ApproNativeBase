package com.approagency.base.paymnet

import androidx.activity.ComponentActivity
import com.approagency.base.model.network.Resource
import kotlinx.coroutines.flow.Flow

interface PaymentService {
    fun purchase(
        activity: ComponentActivity,
        request: PaymentRequest
    ): Flow<Resource<String>>
}