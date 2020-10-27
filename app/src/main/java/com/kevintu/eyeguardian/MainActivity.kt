package com.kevintu.eyeguardian

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.kevintu.eyeguardian.base.BaseActivity
import com.kevintu.eyeguardian.databinding.ActivityMainBinding
import com.kevintu.eyeguardian.uitls.PermissionUtils


class MainActivity : BaseActivity<ActivityMainBinding>() {

  companion object {
    private const val BACK_MAX_INTERVAL_TIME: Long = 2000
    private const val REQUEST_CODE_FOR_PERMISSION = 100
  }

  private var mLastBackTime: Long = 0
  private var mServiceConnection : ServiceConnection? = null
  private var mServiceMessenger : Messenger? = null
  private var mOverStatus: Boolean = false

  override fun initBinding(): ActivityMainBinding {
    return DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_main, null, false)
  }

  override fun initView(savedInstanceState: Bundle?) {
    binding.btnStartOrStop.setOnClickListener {
      if (mServiceMessenger == null) {
        if (PermissionUtils.checkPermission(this)) {
          startGuardianService()
        } else {
          showOpenPermissionDialog()
        }
      } else {
        if (mOverStatus) {
          mServiceMessenger!!.send(GuardianService.createDismissOverViewMsg())
        } else {
          mServiceMessenger!!.send(GuardianService.createShowOverViewMsg())
        }
        mOverStatus = !mOverStatus
        updateBtnContent()
      }
    }

    binding.colorSelector.setOnColorChangedListener {
      mServiceMessenger?.send(GuardianService.createChangeBgMsg(it))
    }

    mServiceConnection = object : ServiceConnection {
      override fun onServiceDisconnected(componentName: ComponentName) {
        mServiceMessenger = null
        mOverStatus = false
        updateBtnContent()
      }

      override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
        mServiceMessenger = Messenger(binder)
        mOverStatus = true
        updateBtnContent()
      }
    }
  }

  private fun updateBtnContent() {
    if (!mOverStatus) {
      binding.btnStartOrStop.text = "开启"
    } else {
      binding.btnStartOrStop.text = "关闭"
    }
  }

  private fun showOpenPermissionDialog() {
    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
    builder.setTitle("权限请求说明")
    builder.setMessage("本功能需要悬浮窗权限，请打开浮窗权限 :)")
    builder.setPositiveButton(android.R.string.ok) { dialog, which ->
      dialog.dismiss()
      requestPermission()
    }
    builder.setNegativeButton(android.R.string.cancel) { dialog, which ->
      dialog.dismiss()
      Toast.makeText(this, "请打开浮窗权限！！", Toast.LENGTH_SHORT).show()
    }
    builder.show()
  }

  private fun requestPermission() {
    var intent =
      Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
    startActivityForResult(intent, REQUEST_CODE_FOR_PERMISSION)
  }

  private fun startGuardianService() {
    startService(GuardianService.createServiceIntent())

    mServiceConnection?.let {
      bindService(GuardianService.createServiceIntent(), it, Context.BIND_AUTO_CREATE)
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_CODE_FOR_PERMISSION && resultCode == Activity.RESULT_OK) {
      if (PermissionUtils.checkPermission(this)) {
        startGuardianService()
      } else {
        Toast.makeText(this, "请打开浮窗权限！！", Toast.LENGTH_SHORT).show()
      }
    }
  }

  override fun onBackPressed() {
    var curTime = System.currentTimeMillis()
    if (curTime - mLastBackTime > BACK_MAX_INTERVAL_TIME) {
      Toast.makeText(this, "再点击一次Back退出！！", Toast.LENGTH_SHORT).show()
    } else {
      super.onBackPressed()
    }
    mLastBackTime = curTime
  }

  override fun onDestroy() {
    super.onDestroy()
    stopService(GuardianService.createServiceIntent())
    mServiceConnection?.let {
      unbindService(it)
    }
    mServiceMessenger = null
  }
}