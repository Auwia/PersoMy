package com.app.persomy.v4;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

public class SpesaListViewAdapter extends ArrayAdapter<Spesa> {
	ArrayList<Spesa> mySpesa;
	int resLayout;
	Context context;

	public View row;

	public SpesaListViewAdapter(Context context, ArrayList<Spesa> mySpesa) {
		super(context, R.layout.list_view_custom, mySpesa);
		this.mySpesa = mySpesa;
		resLayout = R.layout.list_view_custom;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		row = convertView;
		if (row == null) { // inflate our custom layout. resLayout ==
							// R.layout.row_team_layout.xml
			LayoutInflater ll = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = ll.inflate(resLayout, parent, false);
		}

		Spesa item = mySpesa.get(position);
		final int pos = position;

		if (item != null) {
			TextView mySpesaDescription = (TextView) (row != null ? row
					.findViewById(R.id.descrizioneSpesaCustom) : null);
			TextView mySpesaSoldi = (TextView) row
					.findViewById(R.id.soldiSpesaCustom);
			CheckBox mySpesaFlaggata = (CheckBox) row
					.findViewById(R.id.flaggataSpesaCustom);
			TextView mySimboloEuro = (TextView) row
					.findViewById(R.id.simboloEuro);

			if (mySimboloEuro != null) {

				Locale loc = new Locale("it", "IT");
				mySimboloEuro.setText(Currency.getInstance(loc).getSymbol());

			}

			if (mySpesaDescription != null)
				mySpesaDescription.setText(item.getSpesaName());

			if (mySpesaSoldi != null) {
				DecimalFormat df = new DecimalFormat("###,##0.00");
				mySpesaSoldi.setText(String.valueOf(df.format(item
						.getSpesaPrezzo())));
			}

			if (mySpesaFlaggata != null)
				mySpesaFlaggata.setChecked(item.getSpesaFlaggata());

			if (mySpesaFlaggata != null) {
				mySpesaFlaggata.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						mySpesa.set(pos, new Spesa(mySpesa.get(pos)
								.getSpesaName(), mySpesa.get(pos)
								.getSpesaPrezzo(), mySpesa.get(pos)
								.getSpesaSalvata(), ((CheckBox) v).isChecked()));
					}
				});
			}
		}

		return row;
	}
}
