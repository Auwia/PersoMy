package com.app.persomy.v4;


public class Lista {
    private final String dataMovimento;
    private final String ListaName;
    private final double ListaPrezzo;
    private final Boolean ListaUscita;

    public Lista(String data, String nome, double prezzo, Boolean uscita) {
        dataMovimento = data;
        ListaName = nome;
        ListaPrezzo = prezzo;
        ListaUscita = uscita;
    }

    public String getListaName() {
        return ListaName;
    }

    public double getListaPrezzo() {
        return ListaPrezzo;
    }

    public String getDataMovimento() {
        return dataMovimento;
    }

    public String getUscitaMovimento() {
        if (ListaUscita) return "-";
        else return "+";
    }
}