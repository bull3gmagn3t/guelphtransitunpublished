package velocityraptor.guelphtransit.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Display;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import java.util.ArrayList;

import velocityraptor.guelphtransit.R;

/**
 * File/Class: BotBarAdapter.java
 * Project: GuelphTransit
 * Created by Aidan Maher on 09/10/17.
 * Array Adapter for putting items in the bottom selectable stop list
 * Arranging favourites on a drag event is also defined here
 */

public class BotBarAdapter extends ArrayAdapter<Item> {
    private String layout;
    private int fragMode=-1;
    private Activity main;

    public BotBarAdapter(Context context, String layout, ArrayList<Item> stopList, int fragMode, Activity main) {
        super(context, 0, stopList);
        this.fragMode=fragMode;
        this.layout = layout;
        this.main=main;
    }

    public Point getTouchPositionFromDragEvent(View item, DragEvent event) {
        Rect rItem = new Rect();
        if(item!=null) {
            item.getGlobalVisibleRect(rItem);
            return new Point(rItem.left + Math.round(event.getX()), rItem.top + Math.round(event.getY()));
        }else{
            return new Point();
        }
    }
    /**
     * Get a View that displays the data at the specified position in the data set.
     * @param position the data item position in Array<Stop>
     * @param convertView From android doc 28/09/2017 View: The old view to reuse, if possible.
     *                    Note: You should check that this view is non-null and of an appropriate
     *                    type before using.
     *                    If it is not possible to convert this view to display the correct data,
     *                    this method can create a new view. Heterogeneous lists can specify their
     *                    number of view types, so that this View is always of the right type (see
     *                    getViewTypeCount() and getItemViewType(int)).
     * @param parent Main activity
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        final Item stop = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        TextView tvName=null;
        TextView tvHome=null;
        //final View convertVie=convertView;

        if (convertView == null) {
            //Call respective layout based on string input
            switch (layout) {
                case "list_schedule_stop":
                    convertView = LayoutInflater.from(getContext()).inflate(
                            R.layout.icon_list_stop, parent, false);
                    assert convertView != null;
                    tvName = (TextView) convertView.findViewById(R.id.bus_stop_id);
                    tvHome = (TextView) convertView.findViewById(R.id.bus_stop_name);
                    break;
                case "botbar_stop_icon":
                    convertView = LayoutInflater.from(getContext()).inflate(
                            R.layout.icon_botbar_stop, parent, false);
                    assert convertView != null;
                    tvName = (TextView) convertView.findViewById(R.id.bus_stop_id);
                    tvHome = (TextView) convertView.findViewById(R.id.bus_stop_name);
                    if(fragMode==1){
                        convertView.setOnDragListener(new View.OnDragListener() {
                            public boolean setBlack(View v){
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    v.setBackground(new ColorDrawable(getContext().
                                            getColor(R.color.black)));
                                } else {
                                    v.setBackground(new ColorDrawable(getContext().
                                            getResources().
                                            getColor(R.color.black)));
                                }
                                return true;
                            }
                        @Override
                        public boolean onDrag(View v, DragEvent event) {
                            int action = event.getAction();
                            boolean isSelected;
                            isSelected = !MainActivity.selected.first().equals(stop.first());

                            //get position of cursor
                            Point p = getTouchPositionFromDragEvent(v, event);
                            //Log.d("D","pos :"+p.toString());

                            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                            Display display = wm.getDefaultDisplay();
                            Point size = new Point();
                            display.getSize(size);
                            for (int x = 0; x < 2; x++) {
                                //Log.d("D",new Integer(p.x).toString()+" "+new Integer(screenHeight-p.y).toString());
                                if (MainActivity.scrollAreas[x].contains(p.x, p.y)) {
                                    if (x == 0) {
                                        ((HorizontalScrollView) main.findViewById
                                                (R.id.horizontalScrollView)).smoothScrollBy(-20, 0);
                                    } else {
                                        ((HorizontalScrollView) main.findViewById
                                                (R.id.horizontalScrollView)).smoothScrollBy(20, 0);
                                    }
                                }

                                switch (action) {
                                    case DragEvent.ACTION_DRAG_STARTED:
                                        // do nothing
//                                    if (MainActivity.selected.first().equals(stop.first())) {
//                                        Log.d("D", stop.first()+ " started");
//                                    } else {
//                                        Log.d("D", stop.first() + " awake");
//                                    }
                                        break;
                                    case DragEvent.ACTION_DRAG_ENTERED:
                                        //setBackgroundDrawable() deprecated
//                                    HorizontalScrollView h = (HorizontalScrollView)v.findViewById(R.id.horizontalScrollView);
//                                    if(h!=null) {
//                                        h.scrollBy(10, 10);
//                                    }
//                                    if (MainActivity.selected.first().equals(stop.first())) {
//                                        Log.d("D", stop.first() + " entered");
//                                    } else {
//                                        Log.d("D", stop.first() + " entered by " + MainActivity.selected.first());
//                                    }
                                        v.setBackgroundResource(R.drawable.shape_droptarget);
                                        break;
                                    case DragEvent.ACTION_DRAG_EXITED:
//                                    if (MainActivity.selected.first().equals(stop.first())) {
//                                        Log.d("D", stop.first() + " exited");
//                                    } else {
//                                        Log.d("D", stop.first() + " exited by " + MainActivity.selected.first());
//                                    }
                                        //setBackgroundDrawable() deprecated
                                        v.setBackgroundResource(R.drawable.shape);
                                        if (isSelected) {
                                            setBlack(v);
                                        }
                                        break;
                                    case DragEvent.ACTION_DROP:
                                        setBlack(v);
                                        //Reload the adapterList
                                        MainActivity.swapFavList(MainActivity.selected.position, stop.position);
                                        View va = main.findViewById(R.id.container);
                                        if (va != null) {
                                            View innerView = va.findViewById(R.id.button_favorites);
                                            //findViewById(R.id.button_favorites);
                                            if (innerView != null) {
                                                innerView.performClick();
                                            }
                                        }

                                        break;
                                    case DragEvent.ACTION_DRAG_ENDED:
//                                    if (MainActivity.selected.first().equals(stop.first())) {
//                                        Log.d("D", "ended");
//                                    }
                                        //Reset the color
                                        setBlack(v);
                                    default:
                                        break;
                                }
                            }
                            return true;
                        }
                    });
                    }

                    break;
                case "botbar_route_icon":
                    convertView = LayoutInflater.from(getContext()).inflate(
                            R.layout.icon_botbar_bus, parent, false);
                    assert convertView != null;
                    tvName = (TextView) convertView.findViewById(R.id.bus_id);
                    tvHome = (TextView) convertView.findViewById(R.id.bus_route);
                    break;
                case "list_bus_route":
                    convertView = LayoutInflater.from(getContext()).inflate(
                            R.layout.icon_list_bus, parent, false);
                    assert convertView != null;
                    tvName = (TextView) convertView.findViewById(R.id.bus_id);
                    tvHome = (TextView) convertView.findViewById(R.id.bus_route);
                    break;
                default:
                    //Log.d("D","DEFAULT");
                    break;
            }
            tvName.setText(stop.first());
            tvHome.setText(stop.second());
        }
        // Populate the data into the template view using the data object


        // Return the completed view to render on screen
        return convertView;
    }
}

