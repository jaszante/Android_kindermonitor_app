package nl.jastrix_en_coeninblix.kindermonitor_app.FirebaseNotifications

import android.content.Context.MODE_PRIVATE
import com.google.firebase.messaging.RemoteMessage
import android.R.id.edit
import android.content.Context
import android.provider.Settings.Global.getString
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService


class MyFirebaseMessagingService : FirebaseMessagingService() {

//    companion object {
////        var hasToken: Boolean = false
//    }

    override fun onNewToken(fireBaseToken: String?) {
        super.onNewToken(fireBaseToken)
        Log.e("newToken", fireBaseToken)
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("firebasetoken", fireBaseToken).apply()
//        hasToken = true
    }

    fun getFirebaseToken(context: Context): String {
        val token = context.getSharedPreferences("_", MODE_PRIVATE).getString("firebasetoken", "empty")!!
        if (token == "empty"){

            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        return@OnCompleteListener
                    }

                    val lateNewToken = task.result!!.token
                    context.getSharedPreferences("_", MODE_PRIVATE).edit().putString("firebasetoken", lateNewToken).apply()
//                    hasToken = true
                })

            return context.getSharedPreferences("_", MODE_PRIVATE).getString("firebasetoken", "empty")!!
        }
        else {
//            hasToken = true
            return token
        }
    }

}