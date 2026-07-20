package com.approagency.base.firebase

import com.approagency.base.utils.Logger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject

class ApproFirebaseMessagingService :
    FirebaseMessagingService() {

    private val firebaseManager: FirebaseManager by inject()

    override fun onCreate() {
        super.onCreate()

        Logger.info(
            TAG,
            "FirebaseMessagingService created"
        )
    }

    override fun onNewToken(
        token: String
    ) {
        super.onNewToken(token)

        Logger.info(
            TAG,
            "onNewToken token=${Logger.maskToken(token)}"
        )

        firebaseManager.handleNewToken(token)
    }

    override fun onMessageReceived(
        remoteMessage: RemoteMessage
    ) {
        super.onMessageReceived(remoteMessage)

        Logger.info(
            TAG,
            buildString {
                append("onMessageReceived")
                append(" id=")
                append(remoteMessage.messageId)
                append(" from=")
                append(remoteMessage.from)
                append(" data=")
                append(remoteMessage.data)
                append(" notificationPayload=")
                append(remoteMessage.notification != null)
            }
        )

        firebaseManager.handleMessage(remoteMessage)
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()

        Logger.warning(
            TAG,
            "Firebase deleted pending messages"
        )
    }

    companion object {
        private const val TAG = "ApproFirebaseService"
    }
}