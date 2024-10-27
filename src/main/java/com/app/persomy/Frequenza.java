package com.app.persomy;

public class Frequenza {
    private String descrizioneFrequenza;

    public Frequenza(int idFrequenza, String descrizioneFrequenza, int giorni)
    {
    	this.descrizioneFrequenza = descrizioneFrequenza;
    }

    public String getDescrizioneFrequenza() 
    {
        return descrizioneFrequenza;
    }
}