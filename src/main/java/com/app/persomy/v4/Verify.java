package com.app.persomy.v4;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Verify {
    public Verify() {
    }

    boolean isTherePassword(SQLiteDatabase database) {
        Cursor cur = database.query("OPZIONI", new String[]{"ATTIVO"}, "OPZ='PASSWORD'", null, null, null, null, null);
        cur.moveToFirst();
        boolean ritorno = cur.getCount() > 0 && cur.getInt(0) == 1;
        cur.close();
        return ritorno;
    }

    boolean isThereUpdate(SQLiteDatabase database) {
        Cursor cur = database.query("OPZIONI", new String[]{"ATTIVO"}, "OPZ='UPDATE'", null, null, null, null, null);
        cur.moveToFirst();
        boolean ritorno = cur.getCount() > 0 && cur.getInt(0) == 1;
        cur.close();
        return ritorno;
    }

    public boolean isThereJobAutomatico(SQLiteDatabase database) {
        Cursor cur = null;
        try {
            cur = database.query("JOB_AUTOMATICI",
                    new String[]{"COUNT(*)"},
                    "DATA_START < DATETIME('NOW')",
                    null, null, null, null, null);
            if (cur.moveToFirst()) {
                return cur.getInt(0) > 0;
            }
            return false;
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
    }
}
