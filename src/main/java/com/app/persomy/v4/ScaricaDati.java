package com.app.persomy.v4;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScaricaDati {

    private final SQLiteDatabase database;
    private Cursor cur;
    private SimpleDateFormat dateFormat;
    private Date dateObj;

    public ScaricaDati(SQLiteDatabase database) {
        this.database = database;
    }

    public String getSpesaDescrizione(String codice) {
        String ritorno;
        Cursor cur = database.query("VARIE", new String[]{"descrizione", "count(*)"}, "cont" + "=?", new String[]{codice}, null, null, null);
        cur.moveToFirst();
        if (cur.getCount() > 0 && cur.getInt(1) > 0)
            ritorno = cur.getString(0);
        else
            ritorno = null;

        cur.close();
        return ritorno;
    }

    public int getSpesaCodiceDescrizione(String descr) {
        int ritorno;
        Cursor cur = database.query("VARIE", new String[]{"cont", "count(*)"}, "descrizione" + "=?", new String[]{descr}, null, null, null);
        cur.moveToFirst();
        if (cur.getCount() > 0 && cur.getInt(0) > 0) {
            ritorno = Integer.parseInt(cur.getString(0));
        } else {
            ritorno = -1;
        }

        cur.close();
        return ritorno;
    }

    public String getIDMovimento(int frequenzaDescription, String dataStart, int voceDescription, double prezzo, boolean uscita) {
        String ritorno = null;

        int a = (uscita) ? 1 : 0;

        try {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
            dateObj = dateFormat.parse(dataStart + ":00");
        } catch (Exception ignored) {
        }

        cur = database.query("MOVIMENTI_AUTOMATICI", new String[]{"ID"}, "id_frequenza=? and data_start=? and id_voce=? and importo=? and uscita=" + a, new String[]{String.valueOf(frequenzaDescription), dateFormat.format(dateObj), String.valueOf(voceDescription), String.valueOf(prezzo)}, null, null, null);

        if (cur.moveToFirst()) {
            ritorno = cur.getString(0);
        }

        cur.close();
        return ritorno;
    }

    public List<String> caricaSpinnerCompleto(String uscita) {
        List<String> listaSpinner = new ArrayList<>();

        if (uscita.equals("null")) {
            cur = database.query("VARIE", new String[]{"descrizione"}, "cont in (select distinct descrizione from money)", null, null, null, "descrizione");
        } else {
            cur = database.query("VARIE", new String[]{"descrizione"}, null, null, null, null, "descrizione");
        }

        if (cur.moveToFirst()) {
            do {
                listaSpinner.add(cur.getString(0));
            } while (cur.moveToNext());

            cur.close();
        }

        return listaSpinner;
    }

    public String getMonth(int month) {
        return new DateFormatSymbols().getMonths()[month - 1];
    }

    public String meseToNumber(String mese) {
        try {
            dateObj = new SimpleDateFormat("MMMM", Locale.getDefault()).parse(mese);
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateObj);
            int meseAppoggio = cal.get(Calendar.MONTH) + 1;
            if (meseAppoggio < 10)
                return "0" + meseAppoggio;
            else
                return String.valueOf(meseAppoggio);

        } catch (Exception e) {
            Log.e("meseToNumber", "A generic error occurred", e);
        }

        return null;
    }

    public int frequenzaToNumber(String frequenza) {
        return switch (frequenza) {
            case "Giornaliero", "Daily" -> 1;
            case "Settimanale", "Weekly" -> 2;
            case "Decadale", "Decadal" -> 3;
            case "Mensile", "Monthly" -> 4;
            case "Semestrale", "Half" -> 5;
            case "Annuale", "Annual" -> 6;
            case "Primo giorno del mese", "First day of the month" -> 7;
            case "Ultimo giorno del mese", "Last day of the month" -> 8;
            default -> -1;
        };

    }

    public int getGiorniFrequenza(String frequenza) {
        return switch (frequenza) {
            case "Giornaliero", "Daily" -> 1;
            case "Settimanale", "Weekly" -> 7;
            case "Decadale", "Decadal" -> 0;
            case "Mensile", "Monthly" -> 30;
            case "Semestrale", "Half" -> 180;
            case "Annuale", "Annual" -> 365;
            case "Primo giorno del mese", "First day of the month" -> 0;
            case "Ultimo giorno del mese", "Last day of the month" -> 0;
            default -> -1;
        };

    }

    public String[] caricaSpinnerMese() {
        String[] array_spinner = new String[12];

        for (int i = 0; i < 12; i++)
            array_spinner[i] = new DateFormatSymbols().getMonths()[i];

        return array_spinner;
    }

}
