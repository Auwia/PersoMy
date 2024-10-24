package main.java.com.app.persomy;


public class Lista {
	private String dataMovimento;
    private String ListaName;
    private double ListaPrezzo;
    private Boolean ListaUscita;

    public  Lista(String data, String nome, double prezzo, Boolean uscita)
    {
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
        if (ListaUscita) return "-"; else return "+";  
    }
}