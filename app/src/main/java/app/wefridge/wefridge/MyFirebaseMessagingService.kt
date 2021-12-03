package app.wefridge.wefridge


import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpResponse
import com.android.volley.toolbox.JsonObjectRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.IOException

const val channelId="notification_channel"
const val channelName="app.wefridge.wefridge"
class MyFirebaseMessagingService : FirebaseMessagingService() {
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.notification !=null) {
            Log.d("FCMService", "Message data payload: ${remoteMessage.data}")
            generateNotification(remoteMessage.notification!!.title!!,remoteMessage.notification!!.body!!)
        }
    }
    // [END receive_message]

    // [START on_new_token]
    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.

    @SuppressLint("StringFormatInvalid")
    fun onNewToken() {

        val refreshedToken: Task<String> = FirebaseMessaging.getInstance().token
        Log.d(TAG, "Refreshed token: $refreshedToken")

        val tokenMessage = getString(R.string.msg_token_fmt, refreshedToken)

        sendRegistrationToServer(tokenMessage)
    }*/
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMService", "Refreshed token: $token")

        //sendRegistrationToServer(token)

    }


    // [END on_new_token]
    /**
     *

    private fun sendRegistrationToServer(token: String) {
        val httpClient: HttpClient = DefaultHttpClient()
        val httpPost = HttpPost("https://yourbackend.example.com/tokensignin")

        try {
            val nameValuePairs: MutableList<NameValuePair> = ArrayList<NameValuePair>(1)
            nameValuePairs.add(BasicNameValuePair("idToken", idToken))
            httpPost.setEntity(UrlEncodedFormEntity(nameValuePairs))
            val response: HttpResponse = httpClient.execute(httpPost)
            val statusCode: Int = response.getStatusLine().getStatusCode()
            val responseBody: String = EntityUtils.toString(response.getEntity())
            Log.i(R.string.TAG_Main.toString(), "Signed in as: $responseBody")
        } catch (e: ClientProtocolException) {
            Log.e(R.string.TAG_Messservice.toString(), "Error sending ID token to backend.", e)
        } catch (e: IOException) {
            Log.e(R.string.TAG_Messservice.toString(), "Error sending ID token to backend.", e)
        }
        //TODO: das ist eine möglichkeit
        val url = "http://my-json-feed"
        //TODO code to send token to json server
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                textView.text = "Response: %s".format(response.toString())
            },
            Response.ErrorListener { error ->
                // TODO: Handle error
            }
        )

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)

        Log.d("FCMService", "sendRegistrationTokenToServer($token)")
    }*/
    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param title,message FCM message body received.
     */
    @SuppressLint("RemoteViewLayout")
    fun getRemoteView(title: String, message: String): RemoteViews {

        val remoteView= RemoteViews("app.wefridge.wefridge",R.layout.notification)
        remoteView.setTextViewText(R.id.title_logo,title)
        remoteView.setTextViewText(R.id.message,message)
        remoteView.setImageViewResource(R.id.app_logo,R.drawable.ic_launcher_foreground)
        return remoteView

    }
    @SuppressLint("UnspecifiedImmutableFlag")
    private fun generateNotification(title:String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 , intent,
            PendingIntent.FLAG_ONE_SHOT)

        var builder: NotificationCompat.Builder= NotificationCompat.Builder(applicationContext,
            channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
        builder = builder.setContent(getRemoteView(title,message))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationManager.notify(0 , builder.build())

    }


}