package com.kevintu.eyeguardian

import android.app.Application

/**
 * Created by tuchuantao on 2020/10/27
 */
class EyeGuardianApplication: Application() {

  companion object {
    var mAppContext: EyeGuardianApplication? = null
  }

  override fun onCreate() {
    super.onCreate()
    mAppContext = this
  }
}