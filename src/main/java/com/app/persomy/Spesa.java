package com.app.persomy;


public class Spesa {
    private String spesaName;
    private double spesaPrezzo;
    private Boolean spesaSalvata;
    private Boolean spesaFlaggata;

    public  Spesa(String nome, double prezzo, Boolean salvata, Boolean flaggata)
    {
        spesaName = nome;
        spesaPrezzo = prezzo;
        spesaSalvata = salvata;
        spesaFlaggata = flaggata;
    }

    public String getSpesaName() {
        return spesaName;
    }

    public double getSpesaPrezzo() {
        return spesaPrezzo;
    }
    public Boolean getSpesaSalvata() {
        return spesaSalvata;
    }
    
    public Boolean getSpesaFlaggata() {
        return spesaFlaggata;
    }
}