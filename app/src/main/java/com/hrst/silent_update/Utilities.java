package com.hrst.silent_update;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public class Utilities {
    private static final String TAG = "MainActivity";

    /**
     * Check that valid permissions are set
     * @param context Context
     * @return Boolean value
     */
    public static boolean validPermissions(Context context) {
        if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ((Activity)context).requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            return false;
        }
        return true;
    }

    /**
     * Install package and kill app after installation
     * @param context Context
     * @param packageUri Package URI
     * @throws IOException
     *
     * References
     *   https://www.sisik.eu/blog/android/dev-admin/update-app
     *   https://stackoverflow.com/a/37153867
     */
    public static void installPackage(Context context, URI packageUri) throws IOException {
        // Check that file exists
        File file = new File(packageUri);
        if (!file.exists() || !file.isFile()) {
            Log.w(TAG, "File does not exist: " + packageUri);
            return;
        }

//        // Check that package is more recent than the current version
//        // https://stackoverflow.com/a/17118923
//        PackageInfo newPackageInfo = context.getPackageManager().getPackageArchiveInfo(packagePath, 0);
//        PackageInfo oldPackageInfo = null;
//        try {
//            oldPackageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
//            Log.i("Utilities", "This package's version code: " + BuildConfig.VERSION_CODE);
//            Log.i("Utilities", "New package's version code: " + newPackageInfo.versionCode());
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }

        // PackageManager provides an instance of PackageInstaller
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();

        // Prepare params for installing one APK file with MODE_FULL_INSTALL
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(context.getPackageName());

        // Get a PackageInstaller.Session for performing the actual update
        int sessionId = packageInstaller.createSession(params);
        PackageInstaller.Session session = packageInstaller.openSession(sessionId);

        // Copy APK file bytes into OutputStream provided by install Session
        InputStream is = new FileInputStream(file);
        OutputStream os = session.openWrite(context.getPackageName(), 0, -1);
        byte[] buffer = new byte[65536];
        int length;
        while ((length = is.read(buffer)) != -1) {
            os.write(buffer, 0, length);
        }
        session.fsync(os);
        is.close();
        os.close();
        Log.i(TAG, "Installation complete");

        // The app gets killed after installation session commit
        Log.i(TAG, "Killing app");
        session.commit(PendingIntent.getBroadcast(context, sessionId, new Intent("android.intent.action.MAIN"), 0).getIntentSender());
    }
}
