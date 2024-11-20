package com.app.persomy.v4;


public class Spesa {
    private final String spesaName;
    private final double spesaPrezzo;
    private final Boolean spesaSalvata;
    private final Boolean spesaFlaggata;

    public Spesa(String nome, double prezzo, Boolean salvata, Boolean flaggata) {
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