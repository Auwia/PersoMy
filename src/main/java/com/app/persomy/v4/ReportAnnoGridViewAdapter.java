package com.app.persomy.v4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ReportAnnoGridViewAdapter extends ArrayAdapter<ListaReportAnno>
{
	private ArrayList<ListaReportAnno> myLista;
	private int resLayout;
	private Context context;
	public View row;
	private DecimalFormat df = new DecimalFormat("###,##0.00");
     
     public ReportAnnoGridViewAdapter(Context context, ArrayList<ListaReportAnno> myLista) {
         super(context, R.layout.grid_view_custom_anno, myLista);
         this.myLista = myLista;
         resLayout = R.layout.grid_view_custom_anno;
         this.context = context;
     }

     @NonNull
     @Override
     public View getView(int position, View convertView, @NonNull ViewGroup parent)
     {
         row = convertView;
         if(row == null)
         {
             LayoutInflater ll = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             row = ll.inflate(resLayout, parent, false);
         }

         ListaReportAnno item = myLista.get(position);

         if(item != null)
         {
             assert row != null;
        	 TextView myListaMese = row.findViewById(R.id.meseReportAnno);
             TextView myListaEntrata = row.findViewById(R.id.entrateReportAnno);
             TextView myListaUscita = row.findViewById(R.id.usciteReportAnno);
             
             if(myListaMese != null)
             {
            	 myListaMese.setText(item.getListaMese());
            	 myListaMese.setTextSize(12);
             }
            	 
             if(myListaEntrata != null)
             {
            	 myListaEntrata.setText(String.valueOf(df.format(item.getListaEntrata())));
            	 myListaEntrata.setTextSize(12);
             }
                 
             if(myListaUscita != null)
             {
            	 myListaUscita.setText(String.valueOf(df.format(item.getListaUscita())));
            	 myListaUscita.setTextSize(12);
             }
         }
     
     return row;
     }
}
