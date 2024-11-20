package com.app.persomy.v4;


public class ListaReportAnno {
    private final String mese;
    private final double entrata;
    private final double uscita;

    public ListaReportAnno(String mese, double entrata, double uscita) {
        this.mese = mese;
        this.entrata = entrata;
        this.uscita = uscita;
    }

    public String getListaMese() {
        return mese;
    }

    public double getListaEntrata() {
        return entrata;
    }

    public double getListaUscita() {
        return uscita;
    }
}