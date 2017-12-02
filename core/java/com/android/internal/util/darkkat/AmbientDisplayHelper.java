/*
 * Copyright (C) 2017 DarkKat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.util.darkkat;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;

public class AmbientDisplayHelper {

    public static boolean deviceHasProximitySensor(Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY);
    }

    public static boolean pulseOnNotification(Context context) {
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

        final int pulse = Settings.System.getInt(context.getContentResolver(),
                Settings.System.AMBIENT_DISPLAY_PULSE_ON_NOTIFICATION, 2);
        final boolean locked = km.inKeyguardRestrictedInputMode()
                && km.isKeyguardSecure();

        if (pulse == 2 && !locked) {
            return false;
        } else {
            return true;
        }
    }
}
