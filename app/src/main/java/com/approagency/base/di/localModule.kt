package com.approagency.base.di

import androidx.room.Room
import com.approagency.base.config.ApproConfig
import com.approagency.base.local.room.ApproDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val localModule = module {
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