package com.approagency.base.network.repository

import com.approagency.base.config.ApproConfig
import com.approagency.base.model.Promotion
import com.approagency.base.model.network.Resource
import com.approagency.base.model.user.Product
import com.approagency.base.model.user.UserResponse
import com.approagency.base.model.user.UserStatus
import com.approagency.base.network.networkCall
import com.approagency.base.network.service.ApproService
import kotlinx.coroutines.flow.Flow

class ApproRepository(
    private val config: ApproConfig,
    private val service: ApproService
) {
    fun getPromotions(): Flow<Resource<List<Promotion>>> {
        return networkCall {
            service.getPromotions()
        }
    }

    fun login(
        mobile: String,
        packageName: String = config.packageName
    ): Flow<Resource<Unit>> {
        return networkCall {
            service.login(
                mobile,
                packageName
            )
        }
    }

    fun checkOtp(
        mobile: String,
        token: String
    ): Flow<Resource<UserResponse>> {
        return networkCall {
            service.checkOtp(
                mobile,
                token
            )
        }
    }

    fun getStatus(
        packageName: String = config.packageName
    ): Flow<Resource<UserStatus>> {
        return networkCall {
            service.getStatus(
                packageName
            )
        }
    }

    fun getProducts(
        packageName: String = config.packageName
    ): Flow<Resource<List<Product>>> {
        return networkCall {
            service.getProducts(
                packageName
            )
        }
    }

    fun subscribeProduct(
        packageName: String = config.packageName,
        productId: Int,
        body: Map<String, String>
    ): Flow<Resource<Boolean>> {
        return networkCall {
            service.subscribeProduct(
                packageName,
                productId,
                body
            ).isSuccessful
        }
    }

    fun sendFCMToken(
        token: String,
        packageName: String = config.packageName
    ): Flow<Resource<Unit>> {
        return networkCall {
            service.sendFCMToken(
                token,
                packageName
            )
            Unit
        }
    }
}