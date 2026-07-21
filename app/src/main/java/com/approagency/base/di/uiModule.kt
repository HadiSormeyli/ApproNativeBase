package com.approagency.base.di

import com.approagency.base.config.ApproConfig
import com.approagency.base.presentation.ApproViewModel
import com.approagency.base.theme.ThemeManager
import com.approagency.base.utils.DeepLinkManager
import com.approagency.base.utils.NotificationManager
import com.approagency.base.utils.OtpAutoFillBus
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val uiModule = module {
    single {
        OtpAutoFillBus()
    }

    single {
        ThemeManager(
            defaultMode = get<ApproConfig>().defaultThemeMode
        )
    }

    single {
        NotificationManager(
            context = androidContext()
        )
    }

    single {
        DeepLinkManager(
            config = get()
        )
    }

    viewModelOf(::ApproViewModel)
}