package com.approagency.base

import androidx.activity.ComponentActivity
import com.approagency.base.config.ApproConfig
import com.approagency.base.local.room.dao.SessionDao
import com.approagency.base.model.network.Failure
import com.approagency.base.model.network.Resource
import com.approagency.base.network.service.ApproPrivateService
import com.approagency.base.paymnet.PaymentRequest
import com.approagency.base.paymnet.PaymentService
import kotlinx.coroutines.flow.Flow

class MarketPaymentService(
    private val config: ApproConfig,
    private val sessionDao: SessionDao,
    private val service: ApproPrivateService,
    private val marketPackageName: String = "com.android.vending"
) : PaymentService {

    override fun purchase(
        activity: ComponentActivity,
        request: PaymentRequest
    ): Flow<Resource<String>> {
        throw Failure.StoreUnavailable
    }
}