package com.app.persomy.v4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

public class SpesaListViewAdapter extends ArrayAdapter<Spesa> {

    private final ArrayList<Spesa> mySpesa;
    private final int resLayout;
    private final LayoutInflater inflater;

    public SpesaListViewAdapter(Context context, ArrayList<Spesa> mySpesa) {
        super(context, R.layout.list_view_custom, mySpesa);
        this.mySpesa = mySpesa;
        this.resLayout = R.layout.list_view_custom;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(resLayout, parent, false);
            holder = new ViewHolder();
            holder.mySpesaDescription = convertView.findViewById(R.id.descrizioneSpesaCustom);
            holder.mySpesaSoldi = convertView.findViewById(R.id.soldiSpesaCustom);
            holder.mySpesaFlaggata = convertView.findViewById(R.id.flaggataSpesaCustom);
            holder.mySimboloEuro = convertView.findViewById(R.id.simboloEuro);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Spesa item = mySpesa.get(position);

        if (item != null) {
            // Set the Euro symbol based on the Italian locale
            Locale loc = new Locale("it", "IT");

            if (holder.mySimboloEuro != null) {
                holder.mySimboloEuro.setText(Currency.getInstance(loc).getSymbol());
                holder.mySimboloEuro.setContentDescription(
                        inflater.getContext().getString(R.string.currency_symbol_description)
                );
            }

            // Set description text
            if (holder.mySpesaDescription != null) {
                holder.mySpesaDescription.setText(item.getSpesaName());
                holder.mySpesaDescription.setContentDescription(
                        inflater.getContext().getString(R.string.spesa_description, item.getSpesaName())
                );
            }

            // Set amount text with proper formatting
            if (holder.mySpesaSoldi != null) {
                DecimalFormat df = new DecimalFormat("###,##0.00");
                String formattedPrice = df.format(item.getSpesaPrezzo());
                holder.mySpesaSoldi.setText(formattedPrice);
                holder.mySpesaSoldi.setContentDescription(
                        inflater.getContext().getString(R.string.spesa_amount_description, formattedPrice)
                );
            }

            // Set and handle checkbox state
            if (holder.mySpesaFlaggata != null) {
                holder.mySpesaFlaggata.setChecked(item.getSpesaFlaggata());
                holder.mySpesaFlaggata.setOnClickListener(v -> mySpesa.set(
                        position,
                        new Spesa(
                                item.getSpesaName(),
                                item.getSpesaPrezzo(),
                                item.getSpesaSalvata(),
                                ((CheckBox) v).isChecked()
                        )
                ));
                holder.mySpesaFlaggata.setContentDescription(
                        inflater.getContext().getString(
                                R.string.spesa_flag_description,
                                item.getSpesaName(),
                                item.getSpesaFlaggata() ? "selezionata" : "non selezionata"
                        )
                );
            }
        }

        return convertView;
    }

    static class ViewHolder {
        TextView mySpesaDescription;
        TextView mySpesaSoldi;
        CheckBox mySpesaFlaggata;
        TextView mySimboloEuro;
    }
}
