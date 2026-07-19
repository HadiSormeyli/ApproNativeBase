package com.approagency.base.di

import android.app.Application
import com.approagency.base.config.ApproConfig
import com.approagency.base.local.preference.PreferencesHelper
import com.approagency.base.model.ui.deepLink.DeepLinkParser
import com.approagency.base.utils.DeepLinkManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

object Appro {
    fun initialize(
        application: Application,
        config: ApproConfig,
        deepLinkParser: DeepLinkParser? = null,
        appModules: List<Module> = emptyList()
    ): KoinApplication {
        val hasDeepLinkScheme =
            config.deepLinks.isNotEmpty() && config.deepLink.isNotEmpty()
        val hasDeepLinkParser = deepLinkParser != null

        require(hasDeepLinkScheme == hasDeepLinkParser) {
            when {
                hasDeepLinkScheme -> "DeepLinkParser is required when deepLink is specified"
                else -> "deepLink is required when DeepLinkParser is specified"
            }
        }

        return startKoin {
            PreferencesHelper.init(
                context = application,
                applicationPackageName = config.packageName
            )

            androidContext(application)

            modules(
                listOf(
                    approConfigModule(config),
                    localModule,
                    sessionModule,
                    uiModule,
                    networkModule,
                    paymentModule,
                ) + appModules
            )
        }.also { koinApplication ->
            deepLinkParser?.let {
                koinApplication.koin
                    .get<DeepLinkManager>()
                    .initialize(it)
            }
        }
    }
}