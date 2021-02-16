# Silent Update

This sample shows how to update Android app silently without user confirmation using a device owner app.

## Usage
1. Build app and install on device
```shell
adb install -r <apk-filename>
```

2. Set app as device owner
```shell
 adb shell dpm set-device-owner com.hrst.silent_update/.DeviceAdmin
```

3. Increment versionCode (in app's `build.gradle`) of the app and rebuild again.

4. Push the new APK with updated versionCode to the device
```shell
adb push app-update.apk /sdcard/Download/.
```

5. Start the app (old version) and select `UPDATE APP`. This should install the new version of the app from the specified location. The app should then automatically restart and show the new version.


## Notes
### Enable Device Owner via ADB
Remove all accounts from device `Settings > Accounts`. Enable developer mode and then USB debugging. Then enable device owner via ADB:
```
adb shell dpm set-device-owner com.hrst.update_app/.DeviceAdmin
```

You should get the following message:
```
Success: Device owner set to package ComponentInfo{com.hrst.update_app/com.hrst.update_app.DeviceAdmin}
Active admin set to component {com.hrst.update_app/com.hrst.update_app.DeviceAdmin}
```

Reference:
- https://stackoverflow.com/a/44194210

### Removing Device Owner
In temi (Android 6), if the app has device admin privileges, disabling this privilege must come from within the app. Otherwise it will become impossible to uninstall the app without a hard factory reset.

Since Android 7, the `dpm` command can be used to remove device owner:
```
adb shell dpm remove-active-admin com.hrst.update_app/.DeviceAdmin
```

You should get the following message:
```
adb shell dpm remove-active-admin com.hrst.update_app/.DeviceAdmin
```

Alternatively, go to `Settings > Security > Device Administrators` and disable the app.
 
 
## References:
 - https://www.sisik.eu/blog/android/dev-admin/set-dev-owner
 - https://developer.android.com/guide/topics/admin/device-admin
