package com.approagency.base.paymnet

import androidx.activity.ComponentActivity
import com.approagency.base.config.ApproConfig
import com.approagency.base.local.room.dao.SessionDao
import com.approagency.base.model.network.Failure
import com.approagency.base.model.network.Resource
import com.approagency.base.network.service.ApproPrivateService
import kotlinx.coroutines.flow.Flow

class GooglePlayPaymentService(
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