package com.approagency.base.di

import androidx.room.Room
import com.approagency.base.config.ApproConfig
import com.approagency.base.local.preference.PreferencesHelper
import com.approagency.base.local.room.ApproDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val localModule = module {
    single {
        PreferencesHelper.init(
            context = androidContext(),
            applicationPackageName = get<ApproConfig>().packageName
        )

        PreferencesHelper
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            ApproDatabase::class.java,
            "${get<ApproConfig>().packageName}.base.db}"
        )
    }

    single {
        get<ApproDatabase>().sessionDao()
    }
}