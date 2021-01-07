package com.hrst.silent_update;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utilities {
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
     * @param packageName Package name
     * @param packagePath Package path
     * @throws IOException
     * References
     *   https://www.sisik.eu/blog/android/dev-admin/update-app
     *   https://stackoverflow.com/a/37153867
     */
    public static void installPackage(Context context, String packageName, String packagePath) throws IOException {
        // Check that file exists
        File file = new File(packagePath);
        if (!file.exists() || !file.isFile()) {
            Log.w("installPackage", "File does not exist: " + packagePath);
            return;
        }

        // PackageManager provides an instance of PackageInstaller
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();

        // Prepare params for installing one APK file with MODE_FULL_INSTALL
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(packageName);

        // Get a PackageInstaller.Session for performing the actual update
        int sessionId = packageInstaller.createSession(params);
        PackageInstaller.Session session = packageInstaller.openSession(sessionId);

        // Copy APK file bytes into OutputStream provided by install Session
        InputStream is = new FileInputStream(packagePath);
        OutputStream os = session.openWrite(packageName, 0, -1);
        byte[] buffer = new byte[65536];
        int length;
        while ((length = is.read(buffer)) != -1) {
            os.write(buffer, 0, length);
        }
        session.fsync(os);
        is.close();
        os.close();
        Log.i("Utilities", "Installation complete");

        // The app gets killed after installation session commit
        Log.i("Utilities", "Killing app");
        session.commit(PendingIntent.getBroadcast(context, sessionId, new Intent("android.intent.action.MAIN"), 0).getIntentSender());
    }
}
