package com.approagency.base.di

import com.approagency.base.config.BaseConfig
import org.koin.dsl.module

fun baseConfigModule(config: BaseConfig) = module {
    single {
        config
    }
}