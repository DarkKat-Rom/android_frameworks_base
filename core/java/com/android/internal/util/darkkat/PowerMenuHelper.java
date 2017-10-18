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

import android.os.PowerManager;
import android.content.Context;
import android.provider.Settings;

public class PowerMenuHelper {

    private static final int ADVANCED_RESTART_MODE_ALWAYS                     = 0;
    private static final int ADVANCED_RESTART_MODE_HIDE_ON_SECURE_LOCK_SCREEN = 2;

    public static final int REBOOT_MODE_NOT_IN_USE               = 0;
    public static final int REBOOT_MODE_RESTART_TO_SYSTEM        = 1;
    public static final int REBOOT_MODE_RESTART_TO_QUICK_RESTART = 2;
    public static final int REBOOT_MODE_REBOOT_TO_RECOVERY       = 3;
    public static final int REBOOT_MODE_REBOOT_TO_BOOTLOADER     = 4;

    public static String resolveReason(int rebootMode) {
        String reason = null;

        if (rebootMode == REBOOT_MODE_REBOOT_TO_RECOVERY) {
            reason = PowerManager.REBOOT_RECOVERY;
        } else if (rebootMode == REBOOT_MODE_REBOOT_TO_BOOTLOADER) {
            reason = PowerManager.REBOOT_BOOTLOADER;
        }

        return reason;
    }

    public static boolean advancedRestartEnabled(Context context, boolean isPrimary,
            boolean isKeyguardLocked) {
        final int mode = Settings.System.getInt(context.getContentResolver(),
                Settings.System.POWER_MENU_ADVANCED_RESTART_MODE,
                ADVANCED_RESTART_MODE_HIDE_ON_SECURE_LOCK_SCREEN);

        if (mode == ADVANCED_RESTART_MODE_HIDE_ON_SECURE_LOCK_SCREEN) {
            if (isPrimary) {
                return !isKeyguardLocked;
            } else {
                return false;
            }
        }

        return mode == ADVANCED_RESTART_MODE_ALWAYS && isPrimary;
    }

    public static boolean confirmRestart(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.POWER_MENU_CONFIRM_RESTART, 0) == 1;
    }

    public static boolean confirmPowerOff(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.System.POWER_MENU_CONFIRM_POWER_OFF, 0) == 1;
    }
}
