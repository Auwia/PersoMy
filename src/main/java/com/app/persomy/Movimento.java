package com.app.persomy.v4;


public class Movimento {
    private String automaticaFrequenza, automaticaStartDate, automaticaVoce;    
    private double automaticaImporto;
    private Boolean uscita, automaticaSalvata, automaticaFlaggata;

    public  Movimento(String automaticaFrequenza, String automaticaStartDate, String automaticaVoce, double prezzo, Boolean uscita, Boolean salvata, Boolean flaggata)
    {
    	this.automaticaFrequenza = automaticaFrequenza;
    	this.automaticaStartDate = automaticaStartDate;
    	this.automaticaVoce = automaticaVoce;
    	this.uscita = uscita;
    	automaticaImporto = prezzo;
    	automaticaSalvata = salvata;
    	automaticaFlaggata = flaggata;
    }

    public String getAutomaticaFrequenza() {
        return automaticaFrequenza;
    }
    
    public String getAutomaticaStartDate() {
        return automaticaStartDate;
    }

    public String getAutomaticaVoce() {
        return automaticaVoce;
    }
    
    public double getAutomaticaImporto() {
        return automaticaImporto;
    }
    
    public Boolean getAutomaticaUscita() {
        return uscita;
    }
    
    public Boolean getAutomaticaSalvata() {
        return automaticaSalvata;
    }
    
    public Boolean getAutomaticaFlaggata() {
        return automaticaFlaggata;
    }
}