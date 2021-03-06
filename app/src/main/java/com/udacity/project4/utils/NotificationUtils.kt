package com.udacity.project4.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.BuildConfig
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

fun sendNotification(context: Context, reminderDataItem: ReminderDataItem) {
    val notificationManager = context
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // We need to create a NotificationChannel associated with our CHANNEL_ID before sending a notification.
    if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
        val name = context.getString(R.string.app_name)
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT).apply {
            setShowBadge(true)
            enableLights(true)
            lightColor = Color.CYAN
            enableVibration(true)
            description = context.getString(R.string.geofence_entered)
        }

        notificationManager.createNotificationChannel(channel)
    }

    val intent = ReminderDescriptionActivity.newIntent(context.applicationContext, reminderDataItem, true)

    //create a pending intent that opens ReminderDescriptionActivity when the user clicks on the notification
    val stackBuilder = TaskStackBuilder.create(context)
        .addParentStack(ReminderDescriptionActivity::class.java)
        .addNextIntent(intent)
    val notificationPendingIntent = stackBuilder
        .getPendingIntent(getUniqueId(), PendingIntent.FLAG_UPDATE_CURRENT)

    //create big picture style
    val mapImage = BitmapFactory.decodeFile(reminderDataItem.snapshot)
    val bigPictureStyle = NotificationCompat.BigPictureStyle().bigPicture(mapImage).bigLargeIcon(null)

    //build the notification object with the data to be shown
    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(reminderDataItem.title)
        .setContentText(reminderDataItem.location)
        .setContentIntent(notificationPendingIntent)
        .setAutoCancel(true)
        .setStyle(bigPictureStyle)
        .setLargeIcon(mapImage)
        .build()

    notificationManager.notify(getUniqueId(), notification)

    // Todo: Remove geofencing here (but how? how to get the PendingIntent)
    //val geofencingClient = LocationServices.getGeofencingClient(context)
    //geofencingClient.removeGeofences(`miss Pending Intent`)
}

private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())