package com.approagency.base.di

import com.approagency.base.config.BaseConfig
import com.approagency.base.presentation.ApproViewModel
import com.approagency.base.theme.ThemeManager
import com.approagency.base.utils.OtpAutoFillBus
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val uiModule = module {
    single {
        OtpAutoFillBus()
    }

    single {
        ThemeManager(
            defaultMode = get<BaseConfig>().defaultThemeMode
        )
    }

    viewModelOf(::ApproViewModel)
}