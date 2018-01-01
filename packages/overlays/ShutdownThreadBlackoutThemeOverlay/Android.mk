LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_RRO_THEME := ShutdownThreadBlackoutTheme
LOCAL_CERTIFICATE := platform

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_PACKAGE_NAME := ShutdownThreadBlackoutThemeOverlay

include $(BUILD_RRO_PACKAGE)
