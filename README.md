# L Tweaks System

Due to not satisfied the performance and stability of Xposed, I created this project to implement functions in [L Tweaks](https://github.com/bluesky139/LTweaks).

This is an advanced implementation on [L Tweaks](https://github.com/bluesky139/LTweaks), you need compile with Android Open Source Project.

## Build steps

* Download AOSP from official or thirdparty, build out system image and flash it to your device, install Magisk and make sure it runs well.

* Open this project in Android Studio, copy and rename [Config.java.template](https://github.com/bluesky139/LTweaksSystem/blob/master/common/src/main/java/li/lingfeng/ltsystem/common/Config.java.template) to `Config.java`, configure Android system source path and device code name in it.

* Run `Patcher` project, it will patch AOSP. (You need revert all changes if you have run this patcher before, just revert `/framework/base`, `/libcore`, `/frameworks/opt/telephony`)

* Build AOSP again.

* Run `Magisk` project, it will build out magisk module for modified system frameworks, install it on your device.

* Use [keytool-importkeypair](https://github.com/getfatday/keytool-importkeypair) to convert your system signature to Android keystore format, create `system_keystore` folder in this project and put keystore into it, run `app` project, it will build out `L Tweaks System` apk, install it.

* Reboot into recovery, clear dalvik cache.

* Everything is ready, boot into system, find `L Tweaks System` app, set each preferences that what you want, reboot system/apps, enjoy.

## My working environment

* Android Studio 3.2.1

* Official AOSP, Android 9.0

* Device: Pixel
