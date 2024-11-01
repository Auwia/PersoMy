package com.app.persomy.v4;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.DisplayMetrics;

public class Verify 
{
	public Verify(){}
	
	public boolean isTablet(Context context) 
	{		
	    try { 
	        DisplayMetrics dm = context.getResources().getDisplayMetrics(); 
	        float screenWidth  = dm.widthPixels / dm.xdpi; 
	        float screenHeight = dm.heightPixels / dm.ydpi; 
	        double size = Math.sqrt(Math.pow(screenWidth, 2) + Math.pow(screenHeight, 2)); 
	        return size >= 6; 
	    } catch(Throwable t) { 
	        return false; 
	    } 
	}
	
	boolean isTherePassword(SQLiteDatabase database) {
    	Cursor cur = database.query("OPZIONI", new String[]{"ATTIVO"}, "OPZ='PASSWORD'", null, null, null, null, null);
	    cur.moveToFirst();
	    boolean ritorno;

        ritorno = cur.getCount() > 0 && cur.getInt(0) == 1;

	    cur.close();
	    return ritorno;
	}
	
	public boolean IsSdPresent() 
	{
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
	
	boolean isThereUpdate(SQLiteDatabase database) 
	{
    	Cursor cur = database.query("OPZIONI", new String[]{"ATTIVO"}, "OPZ='UPDATE'" , null, null, null, null, null);
	    cur.moveToFirst();
	    boolean ritorno;
        ritorno = cur.getCount() > 0 && cur.getInt(0) == 1;
	    cur.close();
	    return ritorno;
	}

	public boolean isOnline(Context context) 
	{
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
	
	public boolean isThereJobAutomatico(SQLiteDatabase database)
	{
		Cursor cur = database.query("JOB_AUTOMATICI", new String[]{"COUNT(*)"}, "DATA_START < DATETIME('NOW')" , null, null, null, null, null);
		cur.moveToFirst();
	    return (cur.getCount() > 0 && cur.getInt(0) > 0 );
	}
}
