package com.app.persomy.v4;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

public class MovimentoListViewAdapter extends ArrayAdapter<Movimento> {

    private final ArrayList<Movimento> myMovimento;
    private final int resLayout;
    private final LayoutInflater inflater;

    public MovimentoListViewAdapter(Context context, ArrayList<Movimento> myMovimento) {
        super(context, R.layout.custom_automatic_view, myMovimento);
        this.myMovimento = myMovimento;
        this.resLayout = R.layout.custom_automatic_view;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(resLayout, parent, false);
            holder = new ViewHolder();
            holder.myMovimentoFrequenza = convertView.findViewById(R.id.frequenzaAutomatica);
            holder.myMovimentoStartData = convertView.findViewById(R.id.startDateAutomatica);
            holder.myMovimentoVoce = convertView.findViewById(R.id.voceAutomatica);
            holder.myMovimentoImporto = convertView.findViewById(R.id.importoAutomatica);
            holder.myMovimentoFlaggata = convertView.findViewById(R.id.flaggataAutomatica);
            holder.mySimboloEuro = convertView.findViewById(R.id.simboloEuro);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Movimento item = myMovimento.get(position);

        if (item != null) {
            Locale loc = new Locale("it", "IT");
            if (holder.mySimboloEuro != null) {
                holder.mySimboloEuro.setText(Currency.getInstance(loc).getSymbol());
                holder.mySimboloEuro.setTextColor(Color.BLACK);
            }

            if (holder.myMovimentoFrequenza != null) {
                holder.myMovimentoFrequenza.setText(item.getAutomaticaFrequenza());
                holder.myMovimentoFrequenza.setTextColor(Color.BLACK);
            }

            if (holder.myMovimentoStartData != null) {
                holder.myMovimentoStartData.setText(item.getAutomaticaStartDate());
                holder.myMovimentoStartData.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            }

            if (holder.myMovimentoVoce != null) {
                holder.myMovimentoVoce.setText(item.getAutomaticaVoce());
                holder.myMovimentoVoce.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            }

            if (holder.myMovimentoImporto != null) {
                DecimalFormat df = new DecimalFormat("###,##0.00");
                holder.myMovimentoImporto.setText(df.format(item.getAutomaticaImporto()));
                holder.myMovimentoImporto.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            }

            if (holder.myMovimentoFlaggata != null) {

                String voce = item.getAutomaticaVoce();
                boolean isChecked = item.getAutomaticaFlaggata();
                String contentDescription = getContext().getString(
                        R.string.checkbox_select_expense,
                        voce + " (row " + position + ")"
                );

                holder.myMovimentoFlaggata.setContentDescription(contentDescription);

                holder.myMovimentoFlaggata.setChecked(item.getAutomaticaFlaggata());

                holder.myMovimentoFlaggata.setOnClickListener(v -> myMovimento.set(
                        position,
                        new Movimento(
                                item.getAutomaticaFrequenza(),
                                item.getAutomaticaStartDate(),
                                item.getAutomaticaVoce(),
                                item.getAutomaticaImporto(),
                                item.getAutomaticaUscita(),
                                item.getAutomaticaSalvata(),
                                ((CheckBox) v).isChecked()
                        )));
            }
        }

        return convertView;
    }

    static class ViewHolder {
        TextView myMovimentoFrequenza;
        TextView myMovimentoStartData;
        TextView myMovimentoVoce;
        TextView myMovimentoImporto;
        CheckBox myMovimentoFlaggata;
        TextView mySimboloEuro;
    }
}
