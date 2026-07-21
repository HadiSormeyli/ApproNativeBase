package com.approagency.base.di

import com.approagency.base.firebase.FirebaseConfig
import com.approagency.base.firebase.FirebaseManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Example:
 *
 * val firebaseConfig = FirebaseConfig(
 *     smallIcon = R.drawable.ic_notification,
 *     defaultTitle = getString(R.string.app_name),
 *     channelGroup = NotificationChannelGroupConfig(
 *         id = "firebase",
 *         name = "Firebase notifications"
 *     ),
 *     channel = NotificationChannelConfig(
 *         id = "firebase_general",
 *         name = "Notifications",
 *         description = "Application notifications",
 *         importance = NotificationManager.IMPORTANCE_HIGH
 *     )
 * )
 *
 * Appro.initialize(
 *     application = this,
 *     config = approConfig,
 *     deepLinkParser = AppDeepLinkParser(),
 *     appModules = listOf(
 *         firebaseModule(firebaseConfig)
 *     )
 * )
 */
fun firebaseModule(
    config: FirebaseConfig
) = module {
    single {
        config
    }

    single(createdAtStart = true) {
        FirebaseManager(
            context = androidContext(),
            notificationManager = get(),
            approConfig = get(),
            repository = get(),
            config = get(),
            sessionManager = get()
        ).apply {
            check(initialize()) {
                "FirebaseManager initialization failed"
            }
        }
    }
}