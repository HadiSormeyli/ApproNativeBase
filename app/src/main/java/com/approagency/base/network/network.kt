package com.approagency.base.network

import android.os.Build
import com.approagency.base.config.ApproConstants
import com.approagency.base.config.ApproConstants.CONNECT_TIMEOUT
import com.approagency.base.config.ApproConstants.READ_TIMEOUT
import com.approagency.base.config.ApproConstants.WRITE_TIMEOUT
import com.approagency.base.config.ApproConfig
import com.approagency.base.model.network.Failure
import com.approagency.base.model.network.Resource
import com.approagency.base.network.dto.ErrorDto
import com.approagency.base.network.interceptor.ApproTokenInterceptor
import com.approagency.base.utils.addSLLFactory
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.context.GlobalContext.get
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

inline fun <RequestType> networkCall(
    shouldConvertError: Boolean = true,
    crossinline fetch: suspend () -> RequestType
) = flow {
    emit(Resource.Loading())

    try {
        emit(Resource.Success(fetch()))
    } catch (throwable: Throwable) {
        emit(
            Resource.Error(
                handleNetworkError(
                    throwable,
                    shouldConvertError,
                )
            )
        )
    }
}.flowOn(Dispatchers.IO)

fun handleNetworkError(
    e: Throwable,
    shouldConvertError: Boolean = false,
): Failure {
    return when (e) {

        is Failure -> e

        is HttpException -> {
            val code = e.code()

            if (shouldConvertError) {
                Failure.fromCode(code).apply {
                    serverMessage = e.convertErrorBody()
                }
            } else {
                Failure.fromCode(code)
            }
        }

        is IOException -> Failure.Connection

        else -> Failure.Unknown
    }
}

fun HttpException.convertErrorBody(): String? {
    return try {
        Gson().fromJson(
            response()?.errorBody()?.string(),
            ErrorDto::class.java
        ).message
    } catch (_: Exception) {
        message
    }
}

fun createRetrofit(
    okHttpClient: OkHttpClient = createPublicOkHttpClient(),
    gsonBuilder: Gson,
    url: String = ApproConstants.BASE_URL
): Retrofit {
    return Retrofit.Builder()
        .baseUrl(url)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gsonBuilder))
        .build()
}

fun createPublicOkHttpClient(
    config: ApproConfig = get().get(),
    httpLoggingInterceptor: HttpLoggingInterceptor = get().get(),
    interceptors: List<Interceptor> = emptyList()
): OkHttpClient {
    val client = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)

    if (config.debug) client.addInterceptor(httpLoggingInterceptor)

    interceptors.forEach {
        client.addInterceptor(it)
    }

    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) client.addSLLFactory()

    return client.build()
}

fun createPrivateOkHttpClient(
    config: ApproConfig = get().get(),
    approTokenInterceptor: ApproTokenInterceptor = get().get(),
    httpLoggingInterceptor: HttpLoggingInterceptor = get().get(),
    interceptors: List<Interceptor> = emptyList()
): OkHttpClient {
    val client = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)

    client.addInterceptor(approTokenInterceptor)

    if (config.debug) client.addInterceptor(httpLoggingInterceptor)

    interceptors.forEach {
        client.addInterceptor(it)
    }

    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) client.addSLLFactory()

    return client.build()
}