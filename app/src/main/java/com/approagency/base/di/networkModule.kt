package com.approagency.base.di

import com.approagency.base.config.ApproConstants
import com.approagency.base.network.createPrivateOkHttpClient
import com.approagency.base.network.createPublicOkHttpClient
import com.approagency.base.network.createRetrofit
import com.approagency.base.network.repository.ApproRepository
import com.approagency.base.network.service.ApproService
import com.approagency.base.utils.createWebService
import com.google.gson.Gson
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit

val networkModule = module {
    single {
        Gson()
    }

    single {
        HttpLoggingInterceptor()
            .apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
    }

    single(named(ApproConstants.APPRO_PUBLIC_OKHTTP)) {
        createPublicOkHttpClient()
    }

    single(named(ApproConstants.APPRO_PRIVATE_OKHTTP)) {
        createPrivateOkHttpClient()
    }

    single(named(ApproConstants.APPRO_PUBLIC_RETROFIT)) {
        createRetrofit(
            okHttpClient = get(named(ApproConstants.APPRO_PUBLIC_OKHTTP)),
            gsonBuilder = get()
        )
    }

    single(named(ApproConstants.APPRO_PRIVATE_RETROFIT)) {
        createRetrofit(
            okHttpClient = get(named(ApproConstants.APPRO_PRIVATE_OKHTTP)),
            gsonBuilder = get()
        )
    }

    single {
        get<Retrofit>(
            named(ApproConstants.APPRO_PUBLIC_RETROFIT)
        ).createWebService<ApproService>()
    }

    single {
        ApproRepository(
            config = get(),
            service = get()
        )
    }
}