package com.approagency.base.network.repository

import com.approagency.base.config.BaseConfig
import com.approagency.base.model.Promotion
import com.approagency.base.model.network.Resource
import com.approagency.base.model.user.Product
import com.approagency.base.model.user.UserResponse
import com.approagency.base.model.user.UserStatus
import com.approagency.base.network.networkCall
import com.approagency.base.network.service.ApproService
import kotlinx.coroutines.flow.Flow

class ApproRepository(
    private val config: BaseConfig,
    private val service: ApproService
) {
    fun getPromotions(): Flow<Resource<List<Promotion>>> {
        return networkCall {
            service.getPromotions()
        }
    }

    fun login(
        mobile: String,
        packageName: String = config.applicationPackage
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
        packageName: String = config.applicationPackage
    ): Flow<Resource<UserStatus>> {
        return networkCall {
            service.getStatus(
                packageName
            )
        }
    }

    fun getProducts(
        packageName: String = config.applicationPackage
    ): Flow<Resource<List<Product>>> {
        return networkCall {
            service.getProducts(
                packageName
            )
        }
    }

    fun subscribeProduct(
        packageName: String = config.applicationPackage,
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
}