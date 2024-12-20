package com.app.persomy.v4;

import android.content.Context;
import android.database.SQLException;

public class PersoMyDataSource {

    private final PersoMyDB dbHelper;

    public PersoMyDataSource(Context context) {
        dbHelper = new PersoMyDB(context);
    }

    public void open() throws SQLException {
        dbHelper.getWritableDatabase();
    }
}
