package com.app.persomy;

class Automatica {
    private String automaticaFrequenza, automaticaStartDate, automaticaVoce;    
    private double automaticaImporto;
    private Boolean automaticaUscita;

    public Automatica(String frequenza, String startDate, String voce, double importo, Boolean uscita)
    {
    	automaticaFrequenza = frequenza;
    	automaticaStartDate = startDate;
    	automaticaVoce = voce;
    	automaticaImporto = importo;
    	automaticaUscita = uscita;
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
        return automaticaUscita;
    }
}