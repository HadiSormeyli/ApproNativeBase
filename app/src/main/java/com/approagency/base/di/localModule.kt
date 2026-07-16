package com.approagency.base.di

import androidx.room.Room
import com.approagency.base.config.BaseConfig
import com.approagency.base.local.preference.PreferencesHelper
import com.approagency.base.local.room.ApproDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val localModule = module {
    single {
        PreferencesHelper.init(
            context = androidContext(),
            applicationPackageName = get<BaseConfig>().applicationPackage
        )

        PreferencesHelper
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            ApproDatabase::class.java,
            "${get<BaseConfig>().applicationPackage}.base.db}"
        )
    }

    single {
        get<ApproDatabase>().sessionDao()
    }
}