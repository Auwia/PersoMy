package com.app.persomy.v4;

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