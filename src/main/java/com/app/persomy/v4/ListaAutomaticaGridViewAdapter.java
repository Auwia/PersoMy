package com.app.persomy.v4;

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

public class ListaAutomaticaGridViewAdapter extends ArrayAdapter<Automatica> {
	private ArrayList<Automatica> myAutomatica;
	int resLayout;
	Context context;

	public View row;

	public ListaAutomaticaGridViewAdapter(Context context,
			int textViewResourceId, ArrayList<Automatica> myAutomatica) {
		super(context, textViewResourceId, myAutomatica);
		this.myAutomatica = myAutomatica;
		resLayout = textViewResourceId;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		row = convertView;
		if (row == null) {
			LayoutInflater ll = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = ll.inflate(resLayout, parent, false);
		}

		Automatica item = myAutomatica.get(position);

		if (item != null) {
			TextView myAutomaticaFrequenza = (TextView) (row != null ? row
					.findViewById(R.id.frequenzaAutomatica) : null);
			TextView myAutomaticaStartDate = (TextView) row
					.findViewById(R.id.startDateAutomatica);
			TextView myAutomaticaVoce = (TextView) row
					.findViewById(R.id.voceAutomatica);
			TextView myAutomaticaImporto = (TextView) row
					.findViewById(R.id.importoAutomatica);
			TextView simboloEuro = (TextView) row
					.findViewById(R.id.simboloEuro);

			Locale loc = new Locale("it", "IT");
			simboloEuro.setText(Currency.getInstance(loc).getSymbol());

			if (myAutomaticaFrequenza != null) {
				myAutomaticaFrequenza.setText(item.getAutomaticaFrequenza());
				myAutomaticaFrequenza.setTextSize(12);
			}

			if (myAutomaticaStartDate != null) {
				myAutomaticaStartDate.setText(item.getAutomaticaStartDate());
				myAutomaticaStartDate.setTextSize(12);
			}

			if (myAutomaticaVoce != null) {
				myAutomaticaVoce.setText(item.getAutomaticaVoce());
				myAutomaticaVoce.setTextSize(12);
			}

			if (myAutomaticaImporto != null) {
				DecimalFormat df = new DecimalFormat("###,##0.00");
				myAutomaticaImporto.setText(String.valueOf(df.format(item
						.getAutomaticaImporto())));
				myAutomaticaImporto.setTextSize(12);
			}
		}

		return row;
	}
}
