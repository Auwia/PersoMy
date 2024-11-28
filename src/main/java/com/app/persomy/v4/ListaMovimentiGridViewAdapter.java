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
import java.util.Currency;
import java.util.Locale;

public class ListaMovimentiGridViewAdapter extends ArrayAdapter<Lista> {

    private final ArrayList<Lista> myLista;
    private final int resLayout;
    private final LayoutInflater inflater;

    public ListaMovimentiGridViewAdapter(Context context, ArrayList<Lista> myLista) {
        super(context, R.layout.custom_grid_view, myLista);
        this.myLista = myLista;
        this.resLayout = R.layout.custom_grid_view;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(resLayout, parent, false);
            holder = new ViewHolder();
            holder.myListaData = convertView.findViewById(R.id.dataListaCustom);
            holder.myListaData.setContentDescription(inflater.getContext().getString(R.string.display_selected_date1));
            holder.myListaDescription = convertView.findViewById(R.id.descrizioneListaCustom);
            holder.myListaDescription.setContentDescription(inflater.getContext().getString(R.string.choose_expense_description_spinner1));

            holder.myListaSoldi = convertView.findViewById(R.id.soldiListaCustom);
            holder.myListaSoldi.setContentDescription(inflater.getContext().getString(R.string.soldiSpesaDesc1));
            holder.myListaUscita = convertView.findViewById(R.id.simbolo);
            holder.mySimboloEuro = convertView.findViewById(R.id.simboloEuro);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Lista item = myLista.get(position);

        if (item != null) {
            Locale loc = new Locale("it", "IT");
            if (holder.mySimboloEuro != null) {
                holder.mySimboloEuro.setText(Currency.getInstance(loc).getSymbol());
            }

            if (holder.myListaData != null) {
                holder.myListaData.setText(item.getDataMovimento());
                holder.myListaData.setTextSize(12);
            }

            if (holder.myListaDescription != null) {
                holder.myListaDescription.setText(item.getListaName());
                holder.myListaDescription.setTextSize(12);
            }

            if (holder.myListaSoldi != null) {
                DecimalFormat df = new DecimalFormat("###,##0.00");
                holder.myListaSoldi.setText(df.format(item.getListaPrezzo()));
                holder.myListaSoldi.setTextSize(12);
            }

            if (holder.myListaUscita != null) {
                holder.myListaUscita.setText(item.getUscitaMovimento());
                holder.myListaUscita.setTextSize(12);
            }
        }

        return convertView;
    }

    static class ViewHolder {
        TextView myListaData;
        TextView myListaDescription;
        TextView myListaSoldi;
        TextView myListaUscita;
        TextView mySimboloEuro;
    }
}