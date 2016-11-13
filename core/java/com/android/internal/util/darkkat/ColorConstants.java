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

    // Theme DarkKat
    public static final int DARKKAT_BLUE_GREY      = 0xff1b1f23;
    public static final int DARKKAT_BLUE_BLUE_GREY = 0xff1f2429;

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

    // Alpha values

    // SystemUI

    // Status bar
    public static final int LIGHT_MODE_ALPHA_SINGLE_TONE = 255;
    public static final int DARK_MODE_ALPHA_SINGLE_TONE  = 153;

    // Status bar expanded (All themes)
    public static final int FULLY_OPAQUE_ALPHA        = 255;
    public static final int TEXT_SECONDARY_ALPHA      = 179;
    public static final int ICON_NORMAL_ALPHA         = 179;
    public static final int ICON_TUNER_ALPHA          = 77;
    public static final int QS_TILE_DISABLED_ALPHA    = 97;
    public static final int QS_TILE_INACTIVE_ALPHA    = 74;
    public static final int QS_TILE_UNAVAILABLE_ALPHA = 64;
    public static final int RIPPLE_ALPHA_COLORED      = 66;
    public static final int RIPPLE_ALPHA_LIGHT        = 51;
    public static final int RIPPLE_ALPHA_DARK         = 31;
}
