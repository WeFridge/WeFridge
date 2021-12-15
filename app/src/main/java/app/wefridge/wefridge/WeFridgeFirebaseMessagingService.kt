package app.wefridge.wefridge


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage



class WeFridgeFirebaseMessagingService : FirebaseMessagingService() {
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     * It will check if a message contains a payload, if it does, the information will be used in
     * the function generateNotification.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.notification !=null) {
            Log.d("FCMService", "Message data payload: ${remoteMessage.data}")
            generateNotification(remoteMessage.notification!!.title!!,remoteMessage.notification!!.body!!)
        }
    }

    /**
     * Calls the FCM registration token at the beginning of the installation.
     * FCM registration token is initially generated so this is where you would retrieve the token.
    }*/
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMService", "Refreshed token: $token")
    }

    /**
     *@param title, message are taken from the payload and displayed in a created channel.
     * it will use the function getRemoteView to create the notification layout
     * it is an edit version from firebase, the changes are the chanel and the call of the funktion
     * getRemoteView
     */
    private fun generateNotification(title:String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.show_fridge_notification)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setColor(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) getColor(R.color.fern_green) else resources.getColor(
                        R.color.fern_green
                    )
                )
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel(this)
        notificationManager.notify(channelId, 0, builder.build())

    }

    companion object {
        const val channelId = "notification_channel"

        fun createNotificationChannel(ctx: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(
                    channelId,
                    ctx.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                )

                val notificationManager =
                    ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
    }

}
