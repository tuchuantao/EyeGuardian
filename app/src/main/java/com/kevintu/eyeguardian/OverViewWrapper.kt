package com.kevintu.eyeguardian

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager

/**
 * Created by tuchuantao on 2020/10/27
 */
class OverViewWrapper {

  private var mWindowManager: WindowManager
  private var mContext: Context
  private var mView: View? = null
  private var mAdded: Boolean = false

  constructor(context: Context) {
    mContext = context
    mWindowManager =
      context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
  }

  fun getLayoutResId(): Int {
    return R.layout.over_layout
  }

  private fun initViewIfNeed() {
    if (mView == null) {
      mView = LayoutInflater.from(mContext).inflate(getLayoutResId(), null, false)
    }
  }

  fun show() {
    initViewIfNeed()

    if (mView == null) {
      throw IllegalStateException("Over view is null !!")
    }
    if (mAdded) {
      mView!!.visibility = View.VISIBLE
      return
    }

    var layoutParams = createLayoutParams()

    try {
      mWindowManager.addView(mView, layoutParams)
      mAdded = true
    } catch (e: Exception) {
      Log.e("kevin", "Add over view fail!! Exception=" + e.message)
    }
  }

  fun dismiss() {
    mView?.let {
      it.visibility = View.GONE
    }
  }

  fun remove() {

  }

  fun changeOverViewBgColor(bgColor: Int) {
    mView?.setBackgroundColor(bgColor)
  }

  private fun createLayoutParams(): WindowManager.LayoutParams {
    val layoutParams = WindowManager.LayoutParams()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
      //刘海屏延伸到刘海里面
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        layoutParams.layoutInDisplayCutoutMode =
          WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
      }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
      && Build.VERSION.SDK_INT < Build.VERSION_CODES.M
    ) {
      layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST
    } else {
      layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
    }

    layoutParams.packageName = mContext.packageName

    layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

    // 不响应touch事件
    layoutParams.flags = layoutParams.flags or (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

    // 占满屏幕
    layoutParams.flags = layoutParams.flags or (WindowManager.LayoutParams.FLAG_FULLSCREEN
        or WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
        or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
    layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
    layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
    layoutParams.x = 0
    layoutParams.y = 0

    layoutParams.format = PixelFormat.TRANSPARENT

    return layoutParams
  }
}