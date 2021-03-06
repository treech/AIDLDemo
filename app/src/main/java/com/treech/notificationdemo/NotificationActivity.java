package com.treech.notificationdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.TextView;

public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = NotificationActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        TextView textView = findViewById(R.id.notification_send_text);
        TextView textView3 = findViewById(R.id.notification_send_single_text);
        textView.setOnClickListener(v -> setNotificationGroup());
        textView3.setOnClickListener(v -> setNotificationAnotherGroup());
    }


    int singleNotificationId = 100;

    private void setNotificationGroup() {

        String bundle_notification_id = "bundle_notification_" + 100;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //We need to update the bundle notification every time a new notification comes up.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannels().size() < 2) {
                NotificationChannel groupChannel = new NotificationChannel("bundle_channel_id", "bundle_channel_name", NotificationManager.IMPORTANCE_LOW);
                notificationManager.createNotificationChannel(groupChannel);
                NotificationChannel channel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
        }
        NotificationCompat.Builder summaryNotificationBuilder = new NotificationCompat.Builder(this, "bundle_channel_id")
                .setGroup(bundle_notification_id)
                .setGroupSummary(true)
                .setContentTitle("New Notification " + singleNotificationId)
                .setContentText("Content for the notificationContent for ")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher);


        if (singleNotificationId == 100)
            singleNotificationId = 100 + 1;
        else
            singleNotificationId++;

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "channel_id")
                .setGroup(bundle_notification_id)
                .setContentTitle("New Notification " + singleNotificationId)
                .setContentText("Content for the notificationContent for ")
//                .setStyle(new NotificationCompat.BigTextStyle().bigText("Content for the notificationContent for the notificationContent for the notificationContent for the notificationContent for the notification"))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setGroupSummary(false);

        notificationManager.notify(singleNotificationId, notification.build());
        notificationManager.notify(100, summaryNotificationBuilder.build());
    }

    public void setNotificationAnotherGroup(){

        String bundle_notification_id = "bundle_notification_" + 200;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //We need to update the bundle notification every time a new notification comes up.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannels().size() < 2) {
                NotificationChannel groupChannel = new NotificationChannel("bundle_channel_id", "bundle_channel_name", NotificationManager.IMPORTANCE_LOW);
                notificationManager.createNotificationChannel(groupChannel);
                NotificationChannel channel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
        }
        NotificationCompat.Builder summaryNotificationBuilder = new NotificationCompat.Builder(this, "bundle_channel_id")
                .setGroup(bundle_notification_id)
                .setGroupSummary(true)
                .setContentTitle("Bundled Notification " + 100)
                .setContentText("Content Text for group summary")
                .setSmallIcon(R.mipmap.ic_launcher);


        if (singleNotificationId == 200)
            singleNotificationId = 200 + 1;
        else
            singleNotificationId++;

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "channel_id")
                .setGroup(bundle_notification_id)
                .setContentTitle("New Notification " + singleNotificationId)
                .setContentText("Content for the notification")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setGroupSummary(false);

        notificationManager.notify(singleNotificationId, notification.build());
        notificationManager.notify(200, summaryNotificationBuilder.build());
    }

}
