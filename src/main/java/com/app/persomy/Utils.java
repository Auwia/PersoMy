package com.app.persomy.v4;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public class Utils {

	public static String getId(Context c) {

		StringBuffer b = new StringBuffer();

		b.append(Settings.Secure.getString(c.getContentResolver(),
				Settings.Secure.ANDROID_ID));
		b.append(Build.BRAND);
		b.append(Build.DEVICE);
		b.append(Build.VERSION.RELEASE);
		b.append(Build.BOARD);
		b.append(Build.ID);

		return md5sum(b.toString());
	}

	public static String md5sum(String s) {

		try {

			MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			md.update(s.getBytes());

			byte resultSum[] = md.digest();

			StringBuffer hexString = new StringBuffer();

			for (int i = 0; i < resultSum.length; i++) {
				String h = Integer.toHexString(0xFF & resultSum[i]);

				while (h.length() < 2)
					h = "0" + h;

				hexString.append(h);
			}

			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}
}
