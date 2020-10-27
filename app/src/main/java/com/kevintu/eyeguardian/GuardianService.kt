package com.kevintu.eyeguardian

import android.app.*
import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.*
import androidx.core.app.NotificationCompat

/**
 * Created by tuchuantao on 2020/10/26
 */
class GuardianService : Service() {

  companion object {
    private const val SERVICE_ACTION = "kevintu.intent.action.GUARDIAN_SERVICE"
    private const val NOTIFICATION_CHANNEL_ID = "GuardianServiceChannelId";
    private const val MANAGER_NOTIFICATION_ID = 2001

    const val WHAT_CHANGE_OVER_BG = 1001
    const val WHAT_SHOW_OVER_VIEW = 1002
    const val WHAT_DISMISS_OVER_VIEW = 1003

    fun createServiceIntent(): Intent {
      var intent = Intent(SERVICE_ACTION)
      EyeGuardianApplication.mAppContext?.let {
        intent.component = ComponentName(it.packageName, GuardianService::class.java.name)
      }
      return intent
    }

    fun createChangeBgMsg(bgColor: Int) : Message {
      var msg = Message.obtain()
      msg.what = WHAT_CHANGE_OVER_BG
      msg.arg1 = bgColor
      return msg
    }

    fun createShowOverViewMsg() : Message {
      var msg = Message.obtain()
      msg.what = WHAT_SHOW_OVER_VIEW
      return msg
    }

    fun createDismissOverViewMsg(): Message {
      var msg = Message.obtain()
      msg.what = WHAT_DISMISS_OVER_VIEW
      return msg
    }
  }

  private var mBgColor = Color.YELLOW
  private val mOverViewWrapper: OverViewWrapper by lazy {
    OverViewWrapper(this)
  }

  private var mMessenger = Messenger(object : Handler() {
    override fun handleMessage(msg: Message) {
      when (msg.what) {
        WHAT_CHANGE_OVER_BG -> { // 更改遮罩颜色
          mOverViewWrapper.changeOverViewBgColor(msg.arg1)
        }
        WHAT_SHOW_OVER_VIEW -> {
          mOverViewWrapper.show()
        }
        WHAT_DISMISS_OVER_VIEW -> {
          mOverViewWrapper.dismiss()
        }
      }
    }
  })

  override fun onCreate() {
    super.onCreate()
    showForegroundNotification()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    return START_STICKY
  }

  override fun onBind(intent: Intent): IBinder {
    mOverViewWrapper.show()
    return mMessenger.binder
  }

  private fun showForegroundNotification() {
    createNotificationChannelIfNeed()

    val mBuilder: NotificationCompat.Builder =
      NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
        .setContentTitle(applicationContext.getString(R.string.app_name))
        .setContentText("护眼通知内容")
        .setWhen(System.currentTimeMillis())
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    val pendingIntent = PendingIntent.getActivity(
      applicationContext, 0,
      getStartAppIntent(), PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notification: Notification = mBuilder.setContentIntent(pendingIntent)
      .setAutoCancel(false).build()

    startForeground(MANAGER_NOTIFICATION_ID, notification)
  }

  private fun createNotificationChannelIfNeed() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // https://www.jianshu.com/p/99bc32cd8ad6
      val importance = NotificationManager.IMPORTANCE_DEFAULT
      val channel = NotificationChannel(
        NOTIFICATION_CHANNEL_ID,
        GuardianService::class.java.simpleName,
        importance
      )
      channel.description = "Notification Channel description"
      channel.setShowBadge(false)
      val notificationManager = getSystemService(
        NotificationManager::class.java
      )
      notificationManager?.createNotificationChannel(channel)
    }
  }

  private fun getStartAppIntent(): Intent {
    var intent =
      applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
    if (intent == null) {
      intent = Intent(applicationContext, MainActivity::class.java)
    }
    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    return intent
  }

}