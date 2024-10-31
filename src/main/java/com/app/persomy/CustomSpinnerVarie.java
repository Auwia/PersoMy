package com.app.persomy.v4;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class CustomSpinnerVarie extends ArrayAdapter<String>
{
	private String[] objects;
	private Activity context;
	
	public CustomSpinnerVarie(Activity context, String[] objects) {
        super(context, R.layout.spinner_custom_varie, objects);
        
        this.objects = objects;
        this.context = context;
    }
	
	@Override
    public String getItem(int pos) {
        // TODO Auto-generated method stub
        return objects[pos];
    }
	
    @Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {
        return getCustomView(position, parent);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, parent);
    }

    View getCustomView(int position, ViewGroup parent)
    {    	
    	LayoutInflater inflater = context.getLayoutInflater();
        final View row=inflater.inflate(R.layout.spinner_custom_varie, parent, false);
        
        TextView label=(TextView) (row != null ? row.findViewById(R.id.descrizioneSpesa) : null);
        label.setText(objects[position]);
                
        /*
         * 
         * 
        label.setOnClickListener(new View.OnClickListener() 
        {
        	public void onClick(View v) 
        	{
        		MainActivity.MODIFY_SPESA = false;
        		System.out.println("Posizione: " + posizione + " stringa: " );
        		
            }
        });
        
        label.setOnClickListener(new View.OnClickListener() 
        {
        	public void onClick(View v) 
        	{
        		MainActivity.MODIFY_SPESA = false;
        		System.out.println("Posizione: " + posizione + " stringa: " );
            }
        });
    
        row.setOnClickListener(new OnClickListener() 
        {
        	public void onClick(View v)
        	{
        		System.out.println("Allah");
        	}    
        });
        
        ImageView bottoneModifica = (ImageView) row.findViewById(R.id.modificaDescrizione);
        bottoneModifica.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {
            	MainActivity.MODIFY_SPESA = true;
            	getCustomView(pos, parente);
            }
        });
      
      ImageView bottoneModifica = (ImageView) row.findViewById(R.id.modificaDescrizione);
				        bottoneModifica.setOnClickListener(new View.OnClickListener() 
				        {
				            public void onClick(View v) 
				            {
				            	MainActivity.MODIFY_SPESA = true;
				            }
				        });
            */

        return row;
    }
}