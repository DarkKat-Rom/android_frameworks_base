/*
* Copyright (C) 2016 DarkKat
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

public class ColorConstants {

    // Colors

    // Theme general
    public static final int WHITE = 0xffffffff;
    public static final int BLACK = 0xff000000;

    // Theme Holo
    public static final int HOLO_BLUE_LIGHT = 0xff33b5e5;

    // Theme Material
    public static final int MATERIAL_BLUE_GREY_900 = 0xff263238;
    public static final int MATERIAL_BLUE_GREY_800 = 0xff37474F;
    public static final int MATERIAL_DEEP_TEAL_500 = 0xff009688;
    public static final int MATERIAL_BLUE_700      = 0xff1976d2;
    public static final int MATERIAL_BLUE_500      = 0xff2196f3;
    public static final int MATERIAL_GREY_50       = 0xfffafafa;

    // Theme DarkKat
    public static final int DARKKAT_BLUE_GREY      = 0xff1b1f23;
    public static final int DARKKAT_BLUE_BLUE_GREY = 0xff1f2429;

    // Theme Blackout
    public static final int BLACKOUT_PRIMARY_COLOR = 0xff101010;

    // SystemUI

    // Status bar
    public static final int LIGHT_MODE_COLOR_SINGLE_TONE = WHITE;
    public static final int DARK_MODE_COLOR_SINGLE_TONE  = 0x99000000;

    // Status bar expanded (All themes)
    public static final int SYSTEMUI_SWITCH_THUMB_NORMAL = 0xffbdbdbd;

    // Status bar expanded (Day theme)
    public static final int SYSTEMUI_PRIMARY_DAY   = MATERIAL_BLUE_GREY_900;
    public static final int SYSTEMUI_SECONDARY_DAY = MATERIAL_BLUE_GREY_800;
    public static final int SYSTEMUI_ACCENT_DAY    = MATERIAL_DEEP_TEAL_500;

    // Status bar expanded (DarkKat theme)
    public static final int SYSTEMUI_PRIMARY_DARKKAT   = DARKKAT_BLUE_GREY;
    public static final int SYSTEMUI_SECONDARY_DARKKAT = DARKKAT_BLUE_BLUE_GREY;
    public static final int SYSTEMUI_ACCENT_DARKKAT    = MATERIAL_DEEP_TEAL_500;

    // Status bar expanded (Blackout theme)
    public static final int SYSTEMUI_PRIMARY_BLACKOUT   = BLACK;
    public static final int SYSTEMUI_SECONDARY_BLACKOUT = BLACK;
    public static final int SYSTEMUI_ACCENT_BLACKOUT    = MATERIAL_BLUE_700;

    // Notifications

    // Notifications (Day theme)
    public static final int NOTIFICATION_BG_PRIMARY_DAY    = WHITE;
    public static final int NOTIFICATION_BG_LOW_DAY        = 0xfff5f5f5;
    public static final int NOTIFICATION_BG_SECONDARY_DAY  = 0xffeeeeee;
    public static final int NOTIFICATION_BG_EMPHASIZED_DAY = 0xffe0e0e0;
    public static final int NOTIFICATION_ACCENT_DAY        = MATERIAL_DEEP_TEAL_500;

    // Notifications (DarkKat theme)
    public static final int NOTIFICATION_BG_PRIMARY_DARKKAT    = DARKKAT_BLUE_GREY;
    public static final int NOTIFICATION_BG_LOW_DARKKAT        = DARKKAT_BLUE_BLUE_GREY;
    public static final int NOTIFICATION_BG_SECONDARY_DARKKAT  = DARKKAT_BLUE_BLUE_GREY;
    public static final int NOTIFICATION_BG_EMPHASIZED_DARKKAT = 0xff21262c;
    public static final int NOTIFICATION_ACCENT_DARKKAT        = MATERIAL_DEEP_TEAL_500;

    // Notifications (Blackout theme)
    public static final int NOTIFICATION_BG_PRIMARY_BLACKOUT    = BLACK;
    public static final int NOTIFICATION_BG_LOW_BLACKOUT        = BLACK;
    public static final int NOTIFICATION_BG_SECONDARY_BLACKOUT  = BLACKOUT_PRIMARY_COLOR;
    public static final int NOTIFICATION_BG_EMPHASIZED_BLACKOUT = 0xff131313;
    public static final int NOTIFICATION_ACCENT_BLACKOUT        = MATERIAL_BLUE_700;

    // Alpha values

    // General
    public static final int FULLY_OPAQUE_ALPHA       = 255;
    public static final int HIGHTLIGHT_ALPHA_COLORED = 66;
    public static final int RIPPLE_ALPHA_COLORED     = HIGHTLIGHT_ALPHA_COLORED;

    // General (Night theme)
    public static final int TEXT_PRIMARY_ALPHA_NIGHT   = FULLY_OPAQUE_ALPHA;
    public static final int TEXT_SECONDARY_ALPHA_NIGHT = 179;
    public static final int ICON_NORMAL_ALPHA_NIGHT    = TEXT_SECONDARY_ALPHA_NIGHT;
    public static final int HIGHTLIGHT_ALPHA_NIGHT     = 51;
    public static final int RIPPLE_ALPHA_NIGHT         = HIGHTLIGHT_ALPHA_NIGHT;
    public static final int DIVIDER_ALPHA_NIGHT        = HIGHTLIGHT_ALPHA_NIGHT;

    // General (Day theme)
    public static final int TEXT_PRIMARY_ALPHA_DAY   = 222;
    public static final int TEXT_SECONDARY_ALPHA_DAY = 138;
    public static final int ICON_NORMAL_ALPHA_DAY    = TEXT_SECONDARY_ALPHA_DAY;
    public static final int HIGHTLIGHT_ALPHA_DAY     = 31;
    public static final int RIPPLE_ALPHA_DAY         = HIGHTLIGHT_ALPHA_DAY;
    public static final int DIVIDER_ALPHA_DAY        = HIGHTLIGHT_ALPHA_DAY;

    // SystemUI

    // Status bar
    public static final int LIGHT_MODE_ALPHA_SINGLE_TONE = 255;
    public static final int DARK_MODE_ALPHA_SINGLE_TONE  = 153;

    // Status bar expanded (All themes)

    public static final int SBE_TEXT_SECONDARY_ALPHA = TEXT_SECONDARY_ALPHA_NIGHT;
    public static final int SBE_ICON_NORMAL_ALPHA    = ICON_NORMAL_ALPHA_NIGHT;
    public static final int SBE_RIPPLE_ALPHA_LIGHT   = RIPPLE_ALPHA_NIGHT;
    public static final int SBE_RIPPLE_ALPHA_DARK    = RIPPLE_ALPHA_DAY;
    public static final int SBE_RIPPLE_ALPHA_COLORED = RIPPLE_ALPHA_COLORED;
    public static final int SBE_ICON_TUNER_ALPHA     = 77;

    // Status bar expanded (Quick settings all themes)
    public static final int QS_TILE_DISABLED_ALPHA    = 97;
    public static final int QS_TILE_INACTIVE_ALPHA    = 74;
    public static final int QS_TILE_UNAVAILABLE_ALPHA = 64;

    // Notifications

    // Notifications (All themes)
    public static final int NOTIFICATION_BG_DIMMED_ALPHA = 204;
}
