// Copyright (c) 2009, Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package net.tawacentral.roger.secrets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * This class wraps OS-specific APIs behind Java's reflection API so that the
 * application can still run on OSs that don't support the given API.  This
 * class is used as a backward compatibility layer so that I don't need to
 * build different versions of the application for different OSs. 
 * 
 * The root of the problem is the dalvik class verifier, which rejects classes
 * that depend on system classes or interfaces that are not present on the
 * device, causing the application to force exit.  Instead of statically
 * depending on OS-specific classes, this code uses reflection to dynamically
 * discover if a class is available or not, in order to get around the verifier.
 * This is the "correct" way to support different OS versions.
 *  
 * @author rogerta
 *
 */
public class OS {
  /** Tag for logging purposes. */
  public static final String LOG_TAG = "Secrets";

  private static int sdkVersion; 
  static {
    try {
      sdkVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
    } catch (Exception ex) {
    }
  }

  /** Does the device support the froyo (Android 2.2) APIs? */
  public static boolean isAndroid22() {
    return sdkVersion >= 8;
  }
  
  /** Hide the soft keyboard if visible. */
  public static void hideSoftKeyboard(Context ctx, View view) {
    InputMethodManager manager = (InputMethodManager)
        ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
    if (null != manager)
      manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }
  
  /**
   * Creates a backup manager for handling app backup in Android 2.2 and later.
   * Returns an Object so that that this function can be called from any
   * version of Android. 
   * 
   * @param context Application context for the backup manager.
   * @return A backup manager on Android 2.2 and later, null otherwise. 
   */
  public static Object createBackupManager(Context context) {
    Object bm = null;
    
    if (isAndroid22()) {
      try {
        Class<?> clazz = Class.forName("android.app.backup.BackupManager");
        Constructor<?> c = clazz.getConstructor(Context.class);
        bm = c.newInstance(context);
        Log.d(LOG_TAG, "createBackupManager");
      } catch (Exception ex) {
        Log.e(LOG_TAG, "Should have found backup manager", ex);
      }
    }
    
    return bm;
  }
  
  /**
   * Calls dataChanged() on the backup manager on Android 2.2 and later, or
   * does nothing on previous versions of Android.
   * 
   * @param bm Backup manager.  Can be null.
   */
  public static void backupManagerDataChanged(Object bm) {
    if (isAndroid22() && bm != null) {
      try {
        Class<?> clazz = Class.forName("android.app.backup.BackupManager");
        Method m = clazz.getMethod("dataChanged");
        m.invoke(bm);
        Log.d(LOG_TAG, "backupManagerDataChanged");
      } catch (Exception ex) {
        Log.e(LOG_TAG, "backupManagerDataChanged", ex);
      }
    }
  }
}
