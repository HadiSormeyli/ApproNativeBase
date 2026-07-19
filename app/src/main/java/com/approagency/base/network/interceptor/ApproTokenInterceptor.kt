package com.approagency.base.network.interceptor

import com.approagency.base.session.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class ApproTokenInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(
        chain: Interceptor.Chain
    ): Response {

        val token = runBlocking {
            sessionManager.getSession()?.approToken
        }
        val request = chain.request()
            .newBuilder()
            .apply {
                addHeader("Accept", "application/json")
                if (!token.isNullOrBlank()) {
                    addHeader(
                        "Authorization",
                        "Bearer $token"
                    )
                }
            }
            .build()

        return chain.proceed(request)
    }
}