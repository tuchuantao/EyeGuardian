package com.kevintu.eyeguardian.uitls;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;

import java.lang.reflect.Method;

/**
 * Created by tuchuantao on 2020/10/26
 */
public class PermissionUtils {

  public static boolean checkPermission(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return Settings.canDrawOverlays(context);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      return checkOps(context);
    }
    return true;
  }

  private static boolean checkOps(Context context) {
    try {
      Object object = context.getSystemService(Context.APP_OPS_SERVICE);
      if (object == null) {
        return false;
      }
      Class localClass = object.getClass();
      Class[] paramsTypes = new Class[3];
      paramsTypes[0] = Integer.TYPE;
      paramsTypes[1] = Integer.TYPE;
      paramsTypes[2] = String.class;

      Object[] values = new Object[3];
      values[0] = 24; // AppOpsManager.OP_SYSTEM_ALERT_WINDOW
      values[1] = Binder.getCallingUid();
      values[2] = context.getPackageName();

      Method method = localClass.getMethod("checkOp", paramsTypes);
      if (method == null) {
        return false;
      }
      int status = (Integer) method.invoke(object, values);
      return status == AppOpsManager.MODE_ALLOWED;
    } catch (Exception ignore) {
    }
    return false;
  }
}
