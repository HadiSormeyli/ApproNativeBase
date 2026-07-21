package com.approagency.base.di

import com.approagency.base.MarketPaymentService
import com.approagency.base.paymnet.PaymentService
import org.koin.dsl.module

val paymentModule = module {
    single<PaymentService> {
        MarketPaymentService(
            config = get(),
            sessionDao = get(),
            service = get()
        )
    }
}