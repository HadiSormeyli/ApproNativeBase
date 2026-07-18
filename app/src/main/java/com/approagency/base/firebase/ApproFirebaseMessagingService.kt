package com.approagency.base.firebase


import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject

class ApproFirebaseMessagingService :
    FirebaseMessagingService() {

    private val firebaseManager: FirebaseManager by inject()

    override fun onNewToken(token: String) {
        firebaseManager.handleNewToken(token)
    }

    override fun onMessageReceived(
        message: RemoteMessage
    ) {
        firebaseManager.handleMessage(message)
    }
}