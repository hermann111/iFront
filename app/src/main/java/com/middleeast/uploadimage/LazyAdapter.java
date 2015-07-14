package com.middleeast.uploadimage;

import java.text.Bidi;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LazyAdapter extends BaseAdapter {
    
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;

    public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
       // if(convertView==null) {

            Bidi bidi = new Bidi(data.get(position).get(CustomizedListView.KEY_NAME), Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
            if (bidi.baseIsLeftToRight())
                vi = inflater.inflate(R.layout.list_row, null);
            else
                vi = inflater.inflate(R.layout.list_row_rtl, null);

            //vi = inflater.inflate(R.layout.list_row, null);
       // }

        TextView name = (TextView)vi.findViewById(R.id.list_custom_view_name); // artist name
        TextView state = (TextView)vi.findViewById(R.id.list_custom_view_state); // duration
        TextView date = (TextView)vi.findViewById(R.id.list_custom_view_date); // artist name
        TextView license = (TextView)vi.findViewById(R.id.list_custom_view_license); // duration
        
        HashMap<String, String> violation = new HashMap<String, String>();
        violation = data.get(position);
        
        // Setting all values in listview
        name.setText(violation.get(CustomizedListView.KEY_NAME));
        state.setText(violation.get(CustomizedListView.KEY_STATE));
        date.setText(violation.get(CustomizedListView.KEY_DATE));
        license.setText(violation.get(CustomizedListView.KEY_LICENSE));

        return vi;
    }
}