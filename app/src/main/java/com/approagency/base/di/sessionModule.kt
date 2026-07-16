package com.approagency.base.di

import com.approagency.base.session.SessionManager
import org.koin.dsl.module

val sessionModule = module {
    single {
        SessionManager(
            dao = get()
        )
    }
}