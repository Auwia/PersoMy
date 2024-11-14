package com.app.persomy.v4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

public class MovimentoListViewAdapter extends ArrayAdapter<Movimento> {
    public View row;
    ArrayList<Movimento> myMovimento;
    int resLayout;
    Context context;

    public MovimentoListViewAdapter(Context context,
                                    ArrayList<Movimento> myMovimento) {
        super(context, R.layout.custom_automatic_view, myMovimento);
        this.myMovimento = myMovimento;
        resLayout = R.layout.custom_automatic_view;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        row = convertView;
        if (row == null) {
            LayoutInflater ll = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = ll.inflate(resLayout, parent, false);
        }

        Movimento item = myMovimento.get(position);
        final int pos = position;

        if (item != null) {
            TextView myMovimentoFrequenza = row != null ? row.findViewById(R.id.frequenzaAutomatica) : null;
            assert row != null;
            TextView myMovimentoStartData = row.findViewById(R.id.startDateAutomatica);
            TextView myMovimentoVoce = row.findViewById(R.id.voceAutomatica);
            TextView myMovimentoImporto = row.findViewById(R.id.importoAutomatica);
            CheckBox myMovimentoFlaggata = row.findViewById(R.id.flaggataAutomatica);
            TextView mySimboloEuro = row.findViewById(R.id.simboloEuro);

            if (mySimboloEuro != null) {

                Locale loc = new Locale("it", "IT");
                mySimboloEuro.setText(Currency.getInstance(loc).getSymbol());

            }

            if (myMovimentoFrequenza != null)
                myMovimentoFrequenza.setText(item.getAutomaticaFrequenza());

            if (myMovimentoStartData != null)
                myMovimentoStartData.setText(item.getAutomaticaStartDate());

            if (myMovimentoVoce != null)
                myMovimentoVoce.setText(item.getAutomaticaVoce());

            if (myMovimentoImporto != null) {
                DecimalFormat df = new DecimalFormat("###,##0.00");
                myMovimentoImporto.setText(String.valueOf(df.format(item
                        .getAutomaticaImporto())));
            }

            if (myMovimentoFlaggata != null)
                myMovimentoFlaggata.setChecked(item.getAutomaticaFlaggata());

            assert myMovimentoFlaggata != null;
            myMovimentoFlaggata.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    myMovimento.set(
                            pos,
                            new Movimento(
                                    myMovimento.get(pos)
                                            .getAutomaticaFrequenza(),
                                    myMovimento.get(pos)
                                            .getAutomaticaStartDate(),
                                    myMovimento.get(pos).getAutomaticaVoce(),
                                    myMovimento.get(pos).getAutomaticaImporto(),
                                    myMovimento.get(pos).getAutomaticaUscita(),
                                    myMovimento.get(pos).getAutomaticaSalvata(),
                                    ((CheckBox) v).isChecked()));
                }
            });
        }

        return row;
    }
}
