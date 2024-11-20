package com.app.persomy.v4;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

public class Verify 
{
	public Verify(){}

	boolean isTherePassword(SQLiteDatabase database) {
    	Cursor cur = database.query("OPZIONI", new String[]{"ATTIVO"}, "OPZ='PASSWORD'", null, null, null, null, null);
	    cur.moveToFirst();
	    boolean ritorno = cur.getCount() > 0 && cur.getInt(0) == 1;
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
	    boolean ritorno = cur.getCount() > 0 && cur.getInt(0) == 1;
		cur.close();
	    return ritorno;
	}

	public boolean isThereJobAutomatico(SQLiteDatabase database)
	{
		Cursor cur = database.query("JOB_AUTOMATICI", new String[]{"COUNT(*)"}, "DATA_START < DATETIME('NOW')" , null, null, null, null, null);
		cur.moveToFirst();
	    return (cur.getCount() > 0 && cur.getInt(0) > 0 );
	}
}
