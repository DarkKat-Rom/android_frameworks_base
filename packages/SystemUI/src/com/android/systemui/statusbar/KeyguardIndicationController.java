/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.systemui.statusbar;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.BatteryManager;
import android.os.BatteryStats;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;

import com.android.internal.app.IBatteryStats;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.KeyguardIndicationTextView;
import com.android.systemui.statusbar.phone.LockIcon;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;

import java.text.DecimalFormat;

/**
 * Controls the indications and error messages shown on the Keyguard
 */
public class KeyguardIndicationController {

    private static final String TAG = "KeyguardIndication";
    private static final boolean DEBUG_CHARGING_SPEED = false;

    private static final int MSG_HIDE_TRANSIENT = 1;
    private static final int MSG_CLEAR_FP_MSG = 2;
    private static final long TRANSIENT_FP_ERROR_TIMEOUT = 1300;

    private static final DecimalFormat ONE_DIGITS_FORMAT = new DecimalFormat("##0.#");

    private final Context mContext;
    private final ContentResolver mResolver;
    private final KeyguardIndicationTextView mTextView;
    private final UserManager mUserManager;
    private final IBatteryStats mBatteryInfo;

    private final int mSlowThreshold;
    private final int mFastThreshold;
    private final LockIcon mLockIcon;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;

    private String mRestingIndication;
    private String mTransientIndication;
    private int mTransientTextColor;
    private boolean mVisible;

    private boolean mPowerPluggedIn;
    private boolean mPowerCharged;
    private int mPlugged;
    private int mCurrentTemperature;
    private int mCurrentMicroAmp;
    private int mCurrentMicroVolt;
    private int mCurrentMicroAmpHours;
    private int mCurrentMicroWatt;
    private int mMaxChargingMicroAmp;
    private int mMaxChargingMicroVolt;
    private int mMaxChargingMicroAmpHours;
    private int mMaxChargingMicroWatt;
    private int mChargingSpeed;
    private String mMessageToShowOnScreenOn;

    public KeyguardIndicationController(Context context, KeyguardIndicationTextView textView,
                                        LockIcon lockIcon) {
        mContext = context;
        mResolver = mContext.getContentResolver();
        mTextView = textView;
        mLockIcon = lockIcon;

        Resources res = context.getResources();
        mSlowThreshold = res.getInteger(R.integer.config_chargingSlowlyThreshold);
        mFastThreshold = res.getInteger(R.integer.config_chargingFastThreshold);

        mUserManager = context.getSystemService(UserManager.class);
        mBatteryInfo = IBatteryStats.Stub.asInterface(
                ServiceManager.getService(BatteryStats.SERVICE_NAME));

        KeyguardUpdateMonitor.getInstance(context).registerCallback(mUpdateMonitor);
        context.registerReceiverAsUser(mTickReceiver, UserHandle.SYSTEM,
                new IntentFilter(Intent.ACTION_TIME_TICK), null, null);
    }

    public void setVisible(boolean visible) {
        mVisible = visible;
        mTextView.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            hideTransientIndication();
            updateIndication();
        }
    }

    /**
     * Sets the indication that is shown if nothing else is showing.
     */
    public void setRestingIndication(String restingIndication) {
        mRestingIndication = restingIndication;
        updateIndication();
    }

    /**
     * Hides transient indication in {@param delayMs}.
     */
    public void hideTransientIndicationDelayed(long delayMs) {
        mHandler.sendMessageDelayed(
                mHandler.obtainMessage(MSG_HIDE_TRANSIENT), delayMs);
    }

    /**
     * Shows {@param transientIndication} until it is hidden by {@link #hideTransientIndication}.
     */
    public void showTransientIndication(int transientIndication) {
        showTransientIndication(mContext.getResources().getString(transientIndication));
    }

    /**
     * Shows {@param transientIndication} until it is hidden by {@link #hideTransientIndication}.
     */
    public void showTransientIndication(String transientIndication) {
        showTransientIndication(transientIndication, Color.WHITE);
    }

    /**
     * Shows {@param transientIndication} until it is hidden by {@link #hideTransientIndication}.
     */
    public void showTransientIndication(String transientIndication, int textColor) {
        mTransientIndication = transientIndication;
        mTransientTextColor = textColor;
        mHandler.removeMessages(MSG_HIDE_TRANSIENT);
        updateIndication();
    }

    /**
     * Hides transient indication.
     */
    public void hideTransientIndication() {
        if (mTransientIndication != null) {
            mTransientIndication = null;
            mHandler.removeMessages(MSG_HIDE_TRANSIENT);
            updateIndication();
        }
    }

    private void updateIndication() {
        if (mVisible) {
            // Walk down a precedence-ordered list of what should indication
            // should be shown based on user or device state
            boolean showBatteryInfo = Settings.System.getInt(mResolver,
                    Settings.System.LOCK_SCREEN_SHOW_BATTERY_INFO, 0) == 1;
            boolean showBatteryChargingInfo = Settings.System.getInt(mResolver,
                    Settings.System.LOCK_SCREEN_SHOW_BATTERY_CHARGING_INFO, 1) == 1;
            boolean showAdvancedBatteryChargingInfo = Settings.System.getInt(mResolver,
                    Settings.System.LOCK_SCREEN_SHOW_ADVANCED_BATTERY_CHARGING_INFO, 0) == 1;

            if (!mUserManager.isUserUnlocked(ActivityManager.getCurrentUser())) {
                mTextView.switchIndication(com.android.internal.R.string.lockscreen_storage_locked);
                mTextView.setTextColor(Color.WHITE);
            } else if (!TextUtils.isEmpty(mTransientIndication)) {
                mTextView.switchIndication(mTransientIndication);
                mTextView.setTextColor(mTransientTextColor);
            } else if (mPowerPluggedIn) {
                if (showBatteryChargingInfo) {
                    String indication = computeChargingPowerIndication();
                    if (DEBUG_CHARGING_SPEED && !showAdvancedBatteryChargingInfo) {
                        indication += ",  " + (mMaxChargingMicroWatt / 1000) + " mW";
                    }
                    mTextView.switchIndication(indication);
                    mTextView.setTextColor(Color.WHITE);
                } else {
                    mTextView.switchIndication(mRestingIndication);
                    mTextView.setTextColor(Color.WHITE);
                }
            } else if (showBatteryInfo) {
                String indication = computePowerIndication();
                mTextView.switchIndication(indication);
                mTextView.setTextColor(Color.WHITE);
            } else {
                mTextView.switchIndication(mRestingIndication);
                mTextView.setTextColor(Color.WHITE);
            }
        }
    }

    private String computeChargingPowerIndication() {
        if (mPowerCharged) {
            return mContext.getResources().getString(R.string.keyguard_charged);
        }

        // Try fetching charging time from battery stats.
        long chargingTimeRemaining = 0;
        try {
            chargingTimeRemaining = mBatteryInfo.computeChargeTimeRemaining();

        } catch (RemoteException e) {
            Log.e(TAG, "Error calling IBatteryStats: ", e);
        }

        boolean showAdvancedBatteryChargingInfo = Settings.System.getInt(mResolver,
                Settings.System.LOCK_SCREEN_SHOW_ADVANCED_BATTERY_CHARGING_INFO, 0) == 1;
        int chargingTypeId = 0;
        int pluggedTypeId = R.string.keyguard_indication_charging;
        final boolean hasChargingTime = chargingTimeRemaining > 0;
        String chargingType = "";
        String pluggedType = "";
        String chargingTime = "";
        String chargingInfo = "";

        switch (mChargingSpeed) {
            case KeyguardUpdateMonitor.BatteryStatus.CHARGING_FAST:
                chargingTypeId = R.string.keyguard_indication_charging_part_fast;
                break;
            case KeyguardUpdateMonitor.BatteryStatus.CHARGING_SLOWLY:
                chargingTypeId = R.string.keyguard_indication_charging_part_slowly;
                break;
            default:
                break;
        }
        switch (mPlugged) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                pluggedTypeId = R.string.keyguard_indication_charging_ac;
                break;
            case BatteryManager.BATTERY_PLUGGED_USB:
                pluggedTypeId = R.string.keyguard_indication_charging_usb;
                break;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                pluggedTypeId = R.string.keyguard_indication_charging_wireless;
                break;
            default:
                break;
        }

        if (chargingTypeId > 0) {
            chargingType = mContext.getResources().getString(chargingTypeId);
        }
        if (hasChargingTime) {
            String chargingTimeFormatted = Formatter.formatShortElapsedTimeRoundingUpToMinutes(
                    mContext, chargingTimeRemaining);
            chargingTime = mContext.getResources().getString
                    (R.string.keyguard_indication_charging_part_time, chargingTimeFormatted);
        }
        chargingInfo = mContext.getResources().getString(pluggedTypeId, chargingType, chargingTime);

        boolean onFirstRow = true;
        int count = 0;
        if (showAdvancedBatteryChargingInfo) {
            if (mCurrentTemperature > 0) {
                chargingInfo += ", " + mCurrentTemperature / 10 + " °C";
            }
            if (mCurrentMicroAmp > 0 || mMaxChargingMicroAmp > 0) {
                chargingInfo += "\n";
                chargingInfo += (mCurrentMicroAmp > 0
                        ? (mCurrentMicroAmp / 1000) + " mA (" : "N/A (");
                chargingInfo += (mMaxChargingMicroAmp > 0
                        ? (mMaxChargingMicroAmp / 1000) + " mA)" : "N/A)");
                onFirstRow = false;
                count += 1;
            }
            if (mCurrentMicroVolt > 0 || mMaxChargingMicroVolt > 0) {
                if (onFirstRow) {
                    chargingInfo += "\n";
                    onFirstRow = false;
                } else {
                    chargingInfo += count == 0 ? "" : ", ";
                }
                chargingInfo += (mCurrentMicroVolt > 0
                        ? ONE_DIGITS_FORMAT.format(mCurrentMicroVolt / 1000f) + " V (" : "N/A (");
                chargingInfo += (mMaxChargingMicroVolt > 0
                        ? ONE_DIGITS_FORMAT.format(mMaxChargingMicroVolt / 1000000f) + " V)" : "N/A)");
                count += 1;
            }
            if (mCurrentMicroAmpHours > 0 || mMaxChargingMicroAmpHours > 0) {
                if (onFirstRow || count == 2) {
                    chargingInfo += "\n";
                    if (onFirstRow) {
                        onFirstRow = false;
                    } else {
                        count = 0;
                    }
                } else {
                    chargingInfo += count == 0 ? "" : ", ";
                }
                chargingInfo += (mCurrentMicroAmpHours > 0
                        ? (mCurrentMicroAmpHours / 1000) + " mAh (" : "N/A (");
                chargingInfo += (mMaxChargingMicroAmpHours > 0
                        ? (mMaxChargingMicroAmpHours / 1000) + " mAh)" : "N/A)");
                count += 1;
            }
            if (mCurrentMicroWatt > 0 || mMaxChargingMicroWatt > 0) {
                if (onFirstRow || count == 2) {
                    chargingInfo += "\n";
                } else {
                    chargingInfo += count == 0 ? "" : ", ";
                }
                chargingInfo += (mCurrentMicroWatt > 0
                        ? ONE_DIGITS_FORMAT.format(mCurrentMicroWatt / 1000000f) + " W (" : "N/A (");
                chargingInfo += (mMaxChargingMicroWatt > 0
                        ? ONE_DIGITS_FORMAT.format(mMaxChargingMicroWatt/ 1000000f) + " W)" : "N/A)");
            }
        }
        return chargingInfo;
    }

    private String computePowerIndication() {
        // Try fetching battery remaining time from battery stats.
        long batteryTimeRemaining = 0;
        try {
            batteryTimeRemaining = mBatteryInfo.computeBatteryTimeRemaining();

        } catch (RemoteException e) {
            Log.e(TAG, "Error calling IBatteryStats: ", e);
        }

        final boolean hasBatteryTime = batteryTimeRemaining > 0;
        boolean showBatteryTemp = Settings.System.getInt(mResolver,
                Settings.System.LOCK_SCREEN_SHOW_BATTERY_TEMP, 0) == 1;
        int batteryId = R.string.keyguard_indication_discharging;
        String batteryTime = "";
        String batteryInfo = "";

        if (hasBatteryTime) {
            String batteryTimeFormatted = Formatter.formatShortElapsedTimeRoundingUpToMinutes(
                    mContext, batteryTimeRemaining);
            batteryTime = mContext.getResources().getString(
                    R.string.keyguard_indication_discharging_part_time, batteryTimeFormatted);
        }
        batteryInfo = mContext.getResources().getString(batteryId, batteryTime);

        if (showBatteryTemp) {
            if (mCurrentTemperature > 0) {
                batteryInfo += (batteryInfo.isEmpty() ? "" : ", ") + mCurrentTemperature / 10 + " °C";
            }
        }
        return batteryInfo;
    }

    KeyguardUpdateMonitorCallback mUpdateMonitor = new KeyguardUpdateMonitorCallback() {
        public int mLastSuccessiveErrorMessage = -1;

        @Override
        public void onRefreshBatteryInfo(KeyguardUpdateMonitor.BatteryStatus status) {
            BatteryManager batteryManager =
                    (BatteryManager) mContext.getSystemService(Context.BATTERY_SERVICE);

            boolean isChargingOrFull = status.status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status.status == BatteryManager.BATTERY_STATUS_FULL;
            mPowerPluggedIn = status.isPluggedIn() && isChargingOrFull;
            mPowerCharged = status.isCharged();
            mPlugged = status.plugged;
            mCurrentTemperature = status.currentTemperature;
            mCurrentMicroAmp = status.currentMicroAmp;
            mCurrentMicroVolt = status.currentMicroVolt;
            mCurrentMicroAmpHours = status.currentMicroAmpHours;
            mCurrentMicroWatt = status.currentMicroWatt;
            mMaxChargingMicroAmp = status.maxChargingMicroAmp;
            mMaxChargingMicroVolt = status.maxChargingMicroVolt;
            mMaxChargingMicroAmpHours = status.maxChargingMicroAmpHours;
            mMaxChargingMicroWatt = status.maxChargingMicroWatt;
            mChargingSpeed = status.getChargingSpeed(mSlowThreshold, mFastThreshold);
            updateIndication();
        }

        @Override
        public void onFingerprintHelp(int msgId, String helpString) {
            KeyguardUpdateMonitor updateMonitor = KeyguardUpdateMonitor.getInstance(mContext);
            if (!updateMonitor.isUnlockingWithFingerprintAllowed()) {
                return;
            }
            int errorColor = mContext.getResources().getColor(R.color.system_warning_color, null);
            if (mStatusBarKeyguardViewManager.isBouncerShowing()) {
                mStatusBarKeyguardViewManager.showBouncerMessage(helpString, errorColor);
            } else if (updateMonitor.isDeviceInteractive()) {
                mLockIcon.setTransientFpError(true);
                showTransientIndication(helpString, errorColor);
                mHandler.removeMessages(MSG_CLEAR_FP_MSG);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_CLEAR_FP_MSG),
                        TRANSIENT_FP_ERROR_TIMEOUT);
            }
            // Help messages indicate that there was actually a try since the last error, so those
            // are not two successive error messages anymore.
            mLastSuccessiveErrorMessage = -1;
        }

        @Override
        public void onFingerprintError(int msgId, String errString) {
            KeyguardUpdateMonitor updateMonitor = KeyguardUpdateMonitor.getInstance(mContext);
            if (!updateMonitor.isUnlockingWithFingerprintAllowed()
                    || msgId == FingerprintManager.FINGERPRINT_ERROR_CANCELED) {
                return;
            }
            int errorColor = mContext.getResources().getColor(R.color.system_warning_color, null);
            if (mStatusBarKeyguardViewManager.isBouncerShowing()) {
                // When swiping up right after receiving a fingerprint error, the bouncer calls
                // authenticate leading to the same message being shown again on the bouncer.
                // We want to avoid this, as it may confuse the user when the message is too
                // generic.
                if (mLastSuccessiveErrorMessage != msgId) {
                    mStatusBarKeyguardViewManager.showBouncerMessage(errString, errorColor);
                }
            } else if (updateMonitor.isDeviceInteractive()) {
                showTransientIndication(errString, errorColor);
                // We want to keep this message around in case the screen was off
                mHandler.removeMessages(MSG_HIDE_TRANSIENT);
                hideTransientIndicationDelayed(5000);
            } else {
                mMessageToShowOnScreenOn = errString;
            }
            mLastSuccessiveErrorMessage = msgId;
        }

        @Override
        public void onScreenTurnedOn() {
            if (mMessageToShowOnScreenOn != null) {
                int errorColor = mContext.getResources().getColor(R.color.system_warning_color,
                        null);
                showTransientIndication(mMessageToShowOnScreenOn, errorColor);
                // We want to keep this message around in case the screen was off
                mHandler.removeMessages(MSG_HIDE_TRANSIENT);
                hideTransientIndicationDelayed(5000);
                mMessageToShowOnScreenOn = null;
            }
        }

        @Override
        public void onFingerprintRunningStateChanged(boolean running) {
            if (running) {
                mMessageToShowOnScreenOn = null;
            }
        }

        @Override
        public void onFingerprintAuthenticated(int userId) {
            super.onFingerprintAuthenticated(userId);
            mLastSuccessiveErrorMessage = -1;
        }

        @Override
        public void onFingerprintAuthFailed() {
            super.onFingerprintAuthFailed();
            mLastSuccessiveErrorMessage = -1;
        }

        @Override
        public void onUserUnlocked() {
            if (mVisible) {
                updateIndication();
            }
        }
    };

    BroadcastReceiver mTickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mVisible) {
                updateIndication();
            }
        }
    };


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_HIDE_TRANSIENT && mTransientIndication != null) {
                mTransientIndication = null;
                updateIndication();
            } else if (msg.what == MSG_CLEAR_FP_MSG) {
                mLockIcon.setTransientFpError(false);
                hideTransientIndication();
            }
        }
    };

    public void setStatusBarKeyguardViewManager(
            StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }
}
