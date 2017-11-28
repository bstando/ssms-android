package lab.android.bartosz.ssms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class SensorListAdapter extends ArrayAdapter<MDNSDevice> {
    private final Context context;
    private List<MDNSDevice> values;

    public SensorListAdapter(Context context, List<MDNSDevice> objects) {
        super(context, R.layout.layout_list_item, objects);
        this.context = context;
        this.values = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.layout_list_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.list_item_label);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.list_item_logo);
        textView.setText(values.get(position).toString());

        // Change icon based on name
        MDNSDevice s = values.get(position);

        if (s.getIsSensor()) {
            imageView.setImageResource(R.drawable.sensor);
        } else {
            imageView.setImageResource(R.drawable.collector);
        }


        return rowView;
    }
}
