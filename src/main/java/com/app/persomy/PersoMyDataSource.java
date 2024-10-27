package com.app.persomy;

import android.content.Context;
import android.database.SQLException;

public class PersoMyDataSource {

	private PersoMyDB dbHelper;
	
	public PersoMyDataSource(Context context) 
	{
	    dbHelper = new PersoMyDB(context);
	}

	public void open() throws SQLException 
	{
		dbHelper.getWritableDatabase();
	}

	public void close() 
	{
		dbHelper.close();
	}
}
