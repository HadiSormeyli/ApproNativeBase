package com.approagency.base.di

import com.approagency.base.config.ApproConfig
import org.koin.dsl.module

fun approConfigModule(config: ApproConfig) = module {
    single {
        config
    }
}