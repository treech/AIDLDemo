package com.treech.notificationdemo

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var serviceBinder: INotificationInterface? = null
    private var hasBound = false

    private val notificationServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d("ygq", "onServiceConnected")
            serviceBinder = INotificationInterface.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("ygq", "onServiceDisconnected")
            serviceBinder = null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.startNotificationService).setOnClickListener {
            Intent(this, NotificationService::class.java).apply {
                setPackage("com.treech.notificationdemo")
                action = "com.treech.notification.action"
                hasBound = bindService(this, notificationServiceConnection, BIND_AUTO_CREATE)
            }
        }

        findViewById<Button>(R.id.stopNotificationService).setOnClickListener {
            serviceBinder?.let {
                Log.d("ygq", "hasBound:$hasBound")
                if (hasBound) unbindService(notificationServiceConnection)
                hasBound = false
            }
        }

        findViewById<Button>(R.id.showNotification).setOnClickListener {
            serviceBinder?.addNotify()
        }

        findViewById<Button>(R.id.clearNotification).setOnClickListener {
            serviceBinder?.consumeNotify(101)
        }


        findViewById<Button>(R.id.showNotificationDemo).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
    }
}