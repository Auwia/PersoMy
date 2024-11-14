package com.app.persomy.v4;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    public static String getId(Context c) {

        String b = Settings.Secure.getString(c.getContentResolver(),
                Settings.Secure.ANDROID_ID) +
                Build.BRAND +
                Build.DEVICE +
                Build.VERSION.RELEASE +
                Build.BOARD +
                Build.ID;

        return md5sum(b);
    }

    public static String md5sum(String s) {
        try {
            MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            md.update(s.getBytes());
            byte[] resultSum = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : resultSum) {
                StringBuilder h = new StringBuilder(Integer.toHexString(0xFF & b));
                while (h.length() < 2)
                    h.insert(0, "0");
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e("md5sum", "Algorithm not found error occurred", e);
        }
        return null;
    }
}
