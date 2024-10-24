package main.java.com.app.persomy;


public class ListaReportAnno {
	private String mese;
    private double entrata;
    private double uscita;

    public  ListaReportAnno(String mese, double entrata, double uscita)
    {
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