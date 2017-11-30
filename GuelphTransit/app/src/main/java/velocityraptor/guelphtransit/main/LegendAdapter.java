package velocityraptor.guelphtransit.main;

/**
 * LegendAdapter.java
 * Created by Aidan Maher on 30/10/17.
 */

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import velocityraptor.guelphtransit.R;

public class LegendAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] text;
    private final Integer[] imageId;

    public LegendAdapter(Activity context, String[] text) {
        super(context, R.layout.legend_item, text);
        this.text = text;
        this.context = context;
        this.imageId = new Integer[]{
                R.drawable.green_dot, R.drawable.yellow_dot, R.drawable.red_dot
        };
    }

    public LegendAdapter(Activity context,
                         String[] web, Integer[] imageId) {
        super(context, R.layout.legend_item, web);
        this.context = context;
        this.text = web;
        this.imageId = imageId;

    }

    @Override
    public View getView(int position, final View view, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.legend_item, parent, false);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);

        txtTitle.setText(text[position]);
        imageView.setImageResource(imageId[position]);
        return rowView;
    }
}
