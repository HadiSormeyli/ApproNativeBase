package com.approagency.base.di

import android.app.Application
import com.approagency.base.config.BaseConfig
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

object BaseInitializer {

    fun init(
        application: Application,
        config: BaseConfig,
        appModules: List<Module> = emptyList()
    ): KoinApplication {
        return startKoin {
            androidContext(application)

            modules(
                listOf(
                    baseConfigModule(config),
                    localModule,
                    sessionModule,
                    uiModule,
                    networkModule,
                    paymentModule,
                ) + appModules
            )
        }
    }
}