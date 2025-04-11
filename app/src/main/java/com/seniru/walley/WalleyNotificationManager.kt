import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.seniru.walley.R
import com.seniru.walley.persistence.SharedMemory

class WalleyNotificationManager {
    companion object {
        var notificationId = 0

        fun createNotificationChannel(context: Context) {
            Log.i("WalleyNotificationManager", "createNotificationChannel")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "walley"
                val descriptionText = "Walley notifications"
                val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel("walley", name, importance).apply {
                    description = descriptionText
                }
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun checkPermissions(context: Context): Boolean {
            val hasPermissions = (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED)
            Log.i("WalleyNotificationManager", "hasPermissions = $hasPermissions")
            return hasPermissions
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun requestPermissions(activity: Activity) {
            Log.i("WalleyNotificationManager", "requestPermissions")
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun createNotification(context: Context, intent: Intent, title: String, content: String) {
            val preferences = SharedMemory.getInstance(context)
            if (!preferences.getIsAllowingPushNotifications()) return
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val builder = NotificationCompat.Builder(context, "walley")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // requestPermissions(context as Activity)
                    return@with
                }
                notify(notificationId++, builder.build())
            }
        }
    }
}
