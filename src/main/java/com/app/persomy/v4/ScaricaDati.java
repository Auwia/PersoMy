package com.app.persomy.v4;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ScaricaDati {

	private Cursor cur;
	private SQLiteDatabase database;
	private SimpleDateFormat dateFormat;
	private Date dateObj;
	
	public ScaricaDati(SQLiteDatabase database)
	{
		this.database = database;
	}
	
	public String getSpesaDescrizione(String codice) 
	{
		String ritorno;
		Cursor cur = database.query("VARIE", new String[]{"descrizione", "count(*)"} , "cont" + "=?", new String[]{codice} , null, null, null);
	    cur.moveToFirst();
	    if (cur.getCount() > 0 && cur.getInt(1) > 0 )
	    	ritorno = cur.getString(0);
	    else
	    	ritorno = null;

	    cur.close();
	    return ritorno;
	}
	
	public int getSpesaCodiceDescrizione(String descr) 
	{
		int ritorno;
		Cursor cur = database.query("VARIE", new String[]{"cont", "count(*)"} , "descrizione" + "=?", new String[]{descr} , null, null, null);
		cur.moveToFirst();
		if (cur.getCount() > 0 && cur.getInt(0) > 0 ) {
			ritorno = Integer.parseInt(cur.getString(0));
		} else {
			ritorno = -1;
		}

	   cur.close();
	   return ritorno;
	}
	
	public String getIDMovimento(int frequenzaDescription, String dataStart, int voceDescription, double prezzo, boolean uscita ) 
	{
		String ritorno = null;
		
		int a = (uscita) ? 1:0;
		
		try
		{
			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);		
			dateObj = dateFormat.parse(dataStart + ":00");			
		} catch (Exception ignored) {  }
		
		cur = database.query("MOVIMENTI_AUTOMATICI", new String[]{"ID"} , "id_frequenza=? and data_start=? and id_voce=? and importo=? and uscita=" + a, new String[]{String.valueOf(frequenzaDescription), dateFormat.format(dateObj), String.valueOf(voceDescription), String.valueOf(prezzo)}, null, null, null);
	    
		if (cur.moveToFirst()) {
	    	ritorno = cur.getString(0);
		}
	    
	    cur.close();
	    return ritorno;
	}
	
	public String[] caricaSpinnerCompleto(String uscita) 
    {
		if (uscita.equals("null")) {
			cur = database.query("VARIE", new String[]{"descrizione"}, "cont in (select distinct descrizione from money)", null, null, null, "descrizione");
		} else {
			cur = database.query("VARIE", new String[]{"descrizione"}, null, null, null, null, "descrizione");
		}
		
        cur.moveToFirst();
        int i=0;
        String array_spinner[] = new String[cur.getCount()];
        
        while (!cur.isAfterLast()) 
        {
        	array_spinner[i]=  cur.getString(0);
            i+=1;
           	cur.moveToNext();
        }
        
        cur.close();
        
        return array_spinner;
    }
	
	public String getMonth(int month) {
	    return new DateFormatSymbols().getMonths()[month-1];
	}
	
	public String meseToNumber(String mese)
    {
		try
		{
			dateObj = new SimpleDateFormat("MMMM", Locale.getDefault()).parse(mese);
		    Calendar cal = Calendar.getInstance();
		    cal.setTime(dateObj);
		    int meseAppoggio = cal.get(Calendar.MONTH) +1;
		    if (meseAppoggio < 10)
		    	return "0" + meseAppoggio;
		    else
		    	return String.valueOf(meseAppoggio);
			
		} catch (Exception e) { 
			e.printStackTrace(); 
		}
		
    	return null;
    }
	
	public int frequenzaToNumber(String frequenza)
    {
    	if (frequenza.equals("Giornaliero") || frequenza.equals("Daily")) return 1;
    	if (frequenza.equals("Settimanale") || frequenza.equals("Weekly")) return 2;
    	if (frequenza.equals("Decadale") || frequenza.equals("Decadal")) return 3;
    	if (frequenza.equals("Mensile") || frequenza.equals("Monthly")) return 4;
    	if (frequenza.equals("Semestrale") || frequenza.equals("Half")) return 5;
    	if (frequenza.equals("Annuale") || frequenza.equals("Annual")) return 6;
    	if (frequenza.equals("Primo giorno del mese") || frequenza.equals("First day of the month")) return 7;
    	if (frequenza.equals("Ultimo giorno del mese") || frequenza.equals("Last day of the month")) return 8;
    	
    	return -1;
    }
	
	public int getGiorniFrequenza(String frequenza)
    {
    	if (frequenza.equals("Giornaliero") || frequenza.equals("Daily")) return 1;
    	if (frequenza.equals("Settimanale") || frequenza.equals("Weekly")) return 7;
    	if (frequenza.equals("Decadale") || frequenza.equals("Decadal")) return 0;
    	if (frequenza.equals("Mensile") || frequenza.equals("Monthly")) return 30;
    	if (frequenza.equals("Semestrale") || frequenza.equals("Half")) return 180;
    	if (frequenza.equals("Annuale") || frequenza.equals("Annual")) return 365;
    	if (frequenza.equals("Primo giorno del mese") || frequenza.equals("First day of the month")) return 0;
    	if (frequenza.equals("Ultimo giorno del mese") || frequenza.equals("Last day of the month")) return 0;
    	
    	return -1;
    }
	
	public String[] caricaSpinnerMese() 
    {
		String array_spinner[] = new String[12];
        
        for (int i = 0; i < 12; i++) 
        	array_spinner[i] = new DateFormatSymbols().getMonths()[i];
                
        return array_spinner;
    }
	
}
