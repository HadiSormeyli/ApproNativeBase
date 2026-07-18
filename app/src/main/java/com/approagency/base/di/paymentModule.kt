package com.approagency.base.di

import com.approagency.base.config.ApproConfig
import com.approagency.base.config.Flavor
import com.approagency.base.paymnet.BazaarPaymentService
import com.approagency.base.paymnet.GooglePlayPaymentService
import com.approagency.base.paymnet.MyketPaymentService
import com.approagency.base.paymnet.PaymentService
import org.koin.dsl.module

val paymentModule = module {
    single<PaymentService> {
        val config = get<ApproConfig>()

        when (config.flavor) {
            Flavor.BAZAAR -> BazaarPaymentService(
                config = config, sessionDao = get(), approService = get()
            )

            Flavor.MYKET -> MyketPaymentService(
                config = config, sessionDao = get(), approService = get()
            )

            Flavor.GOOGLE_PLAY -> GooglePlayPaymentService(
                config = config, sessionDao = get(), approService = get()
            )
        }
    }
}