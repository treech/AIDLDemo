package com.treech.notificationdemo

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*

class NotificationService : Service() {

    private var notifyId: Int = 100
    private var queue: Deque<Int> = LinkedList()

    override fun onCreate() {
        super.onCreate()
        Log.d("ygq","NotificationService onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ygq","NotificationService onDestroy")
    }

    fun showKeyTravelerAlarmNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, SecondActivity::class.java)
        //dealComment dealPerson dealPersonTel idCard 推送收到的报警无此字段
        intent.putExtra("test", "aaa")
        val pendingIntent = PendingIntent.getActivity(this, SystemClock.uptimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        //分组与取消分组 参考 https://github.com/toctocbin/SampleProject.git
        //注:通知的notificationBuilder修改需要app卸载重装
        val bundle_notification_id = "bundle_notification_" + 100
        //We need to update the bundle notification every time a new notification comes up.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.notificationChannels.size < 2) {
                val groupChannel = NotificationChannel("bundle_channel_id", "bundle_channel_name", NotificationManager.IMPORTANCE_LOW)
                notificationManager.createNotificationChannel(groupChannel)
                val channel = NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT)
                //锁屏显示通知
                channel.lockscreenVisibility = Notification.VISIBILITY_SECRET
                //桌面launcher的消息角标
                channel.canShowBadge()
                //是否允许震动
                channel.enableVibration(true)
                //设置震动模式
                channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 100)
                notificationManager.createNotificationChannel(channel)
            }
        }
        val summaryNotificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, "bundle_channel_id")
            .setGroup(bundle_notification_id)
            .setContentTitle("This is content summary") //文本内容
            .setContentText("this is title summary") //通知的时间
            .setGroupSummary(true)
            .setSmallIcon(R.mipmap.ic_logo)
        val notificationBuilder: NotificationCompat.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = NotificationCompat.Builder(this, "channel_id")
        } else {
            notificationBuilder = NotificationCompat.Builder(applicationContext)
            notificationBuilder.priority = Notification.PRIORITY_DEFAULT
        }
        if (notifyId == Integer.MAX_VALUE - 1) {
            notifyId = 100 + 1
        } else {
            notifyId++
        }
        //小图标
        notificationBuilder.setSmallIcon(R.mipmap.ic_logo) //标题
            .setContentTitle("This is content notification") //文本内容
            .setContentText("this is title notification:$notifyId") //通知的时间
            .setWhen(System.currentTimeMillis()) //设置点击信息后的跳转（意图）
            .setContentIntent(pendingIntent) //设置点击信息后自动清除通知
            .setAutoCancel(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationBuilder
                .setGroup(bundle_notification_id)
                .setGroupSummary(false)
        }
        queue.add(notifyId)
        notificationManager.notify(notifyId, notificationBuilder.build())
        notificationManager.notify(100, summaryNotificationBuilder.build())
    }


    private val serviceBinder = object : INotificationInterface.Stub() {
        override fun addNotify() {
            Log.d("ygq","addNotify")
            showKeyTravelerAlarmNotification()
            Log.d("ygq","addNotify queue size:${queue.size}")
        }

        override fun consumeNotify(notifyId: Int) {
            Log.d("ygq","consumeNotify：$notifyId")
            queue.remove(notifyId)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notifyId)
            Log.d("ygq","consumeNotify queue size:${queue.size}")
        }
    }


    override fun onBind(intent: Intent?): IBinder {
        return serviceBinder
    }
}