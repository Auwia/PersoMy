package main.java.com.app.persomy;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListaMovimentiGridViewAdapter extends ArrayAdapter<Lista>
{
     ArrayList<Lista> myLista;
     int resLayout;
     Context context;

    public View row;
     
     public ListaMovimentiGridViewAdapter(Context context, ArrayList<Lista> myLista) {
         super(context, R.layout.custom_grid_view, myLista);
         this.myLista = myLista;
         resLayout = R.layout.custom_grid_view;
         this.context = context;
     }

     @Override
     public View getView(int position, View convertView, ViewGroup parent)
     {
         row = convertView;
         if(row == null)
         {
             LayoutInflater ll = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             row = ll.inflate(resLayout, parent, false);
         }

         Lista item = myLista.get(position);

         if(item != null)
         {   
        	 TextView myListaData = (TextView) (row != null ? row.findViewById(R.id.dataListaCustom) : null);
        	 TextView myListaDescription = (TextView) row.findViewById(R.id.descrizioneListaCustom);
             TextView myListaSoldi = (TextView) row.findViewById(R.id.soldiListaCustom);
             TextView myListaUscita = (TextView) row.findViewById(R.id.simbolo);
             TextView mySimboloEuro = (TextView) row.findViewById(R.id.simboloEuro);
             
			if (mySimboloEuro != null) {
				Locale loc = new Locale("it", "IT");
				mySimboloEuro.setText(Currency.getInstance(loc).getSymbol());
			}

             if(myListaData != null)
             {
            	 myListaData.setText(item.getDataMovimento());
            	 myListaData.setTextSize(12);
             }
            	 
             if(myListaDescription != null)
             {
            	 myListaDescription.setText(item.getListaName());
            	 myListaDescription.setTextSize(12);
             }
                 
             
             if(myListaSoldi != null)
             {
            	 DecimalFormat df = new DecimalFormat("###,##0.00");
            	 myListaSoldi.setText(String.valueOf(df.format(item.getListaPrezzo())));
            	 myListaSoldi.setTextSize(12);
             }
             
             if(myListaUscita != null)
             {
            	 myListaUscita.setText(item.getUscitaMovimento());
            	 myListaUscita.setTextSize(12);
             }
         }
     
     return row;
     }
}
