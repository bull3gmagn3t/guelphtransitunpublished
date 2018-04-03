package velocityraptor.guelphtransit.main;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import velocityraptor.guelphtransit.R;
import velocityraptor.guelphtransit.main.stopPopup.StopPopup;

/**
 * This class defines the Fragment for the main bottom bar, filled with Bus Routes
 * Also defines behavior for changing pin colours based on scheduled times
 * Also defines behavior for drawing lines between stops
 *
 * @author: Nic Durish.
 * <p>
 * I have exclusive control over this submission via my password.
 * By including this statement in this header comment, I certify that:
 * 1) I have read and understood the University policy on academic integrity;
 * 2) I have completed the Computing with Integrity Tutorial on Moodle; and
 * I assert that this work is my own. I have appropriately acknowledged any and all material
 * (data, images, ideas or words) that I have used, whether directly quoted or paraphrased.
 * Furthermore, I certify that this assignment was prepared by me specifically for this course.
 */
public class FragmentBotBar extends Fragment {
    public FragmentBotBar newFragment;//the new fragment to switch between Fragment Modes
    private int fragMode;//1=home, 2=favourites, 3=location button presses
    private ArrayList<Stop> stopList=null;
    private LatLng myLocLatLng=null;
    public StopPopup stopPopUp;//The popup is made in this class
    private Marker stopMarker;
    private int show = 0;
    private final int BUTTONSIZE=100;
    //get "right" or "left", "width","height","top","bottom","left","right"
    public static HashMap<CharSequence,HashMap<CharSequence,Integer>> dragAreas;

    public void setLoc(LatLng loc){
        myLocLatLng=loc;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*****************************************************************************/
        /**Set width and length for for views which contain the stop or route buttons*/
        /*****************************************************************************/
        fragMode = this.getArguments().getInt("FRAG_MODE");
        int routePos=-1;
        routePos = this.getArguments().getInt("EXTRA_ROUTE_POS");
        show = this.getArguments().getInt("SHOW");
        View view = inflater.inflate(R.layout.fragment_busbar, container, false);
        switch (fragMode) {
            //Set the stopList for each fragmode [0..2]
            case 0://Routes
                if(MainActivity.routeList.size()>0) {
                    stopList = MainActivity.routeList.get(routePos).getStopList();
                    this.addAllPins();//adds all pins to map
                    this.addButtons(view,container);//add the bottom bar buttons
                }
                break;
            case 1://Favourites
                stopList = MainActivity.favoriteList;
                MainActivity.panelMode = 1;
                this.addAllPins();
                this.addButtons(view,container);//add the bottom bar buttons
                break;
            case 2://Location
                stopList = MainActivity.locationList;
                MainActivity.panelMode = 2;
                //Add stops from locationlist into bundle
//                Gson gson=new Gson();
//                String location = gson.toJson(locationList);
                this.addButtons(view,container);//add the bottom bar buttons
                break;
        }


//        FragmentTransaction transaction = getFragmentManager().beginTransaction();
//        transaction.replace(R.id.bot_bar_frag_view, newFragment);
//        transaction.addToBackStack(null);
//        transaction.commit();

        return view;

    }//end onCreateView
    /**
     * Add all the stopList pins to the map and set zoom
     */
    public void addAllPins(){

        //Jackson Keenan [Start]
        MainActivity.mainMap.clear();
        Float maxLat = (float) 0.00;
        Float minLat = (float) 100.00;
        Float maxLong = (float) -120.00;
        Float minLong = (float) 0.00;
        Polyline routeLine=null;

        if(fragMode==0) {
            routeLine = MainActivity.mainMap.addPolyline
                    (new PolylineOptions().width(5).color(0x7F0000FF));
        }
        List<LatLng> routePoints = new ArrayList<>();


        for (int i = 0; i < stopList.size(); i++) {
            Float stopLat = stopList.get(i).getLatitude();
            Float stopLong = stopList.get(i).getLongitude();
            if (stopLat < minLat) {
                minLat = stopLat;
            } else if (stopLat > maxLat) {
                maxLat = stopLat;
            }
            if (stopLong < minLong) {
                minLong = stopLong;
            } else if (stopLong > maxLong) {
                maxLong = stopLong;
            }

            LatLng stopLatLong = new LatLng(stopLat, stopLong);
            routePoints.add(i, stopLatLong);
        }
        ((MainActivity)getActivity()).placeColouredPins(stopList);

        //Calculate area of route for camera zoom
        if(fragMode==0) {
            routeLine.setPoints(routePoints);
            routeLine.setGeodesic(false);
            Float latCenter = maxLat - ((maxLat - minLat) / 2);
            Float longCenter = maxLong - ((maxLong - minLong) / 2);
            LatLng routeLatLong = new LatLng(latCenter, longCenter);
            //zoom the camera
            MainActivity.mapUpdate = CameraUpdateFactory.newLatLngZoom(routeLatLong, 12);
            MainActivity.mainMap.animateCamera(MainActivity.mapUpdate);
        }
    }

    /**
     *
     */
    public void addButtons(View view, ViewGroup container) {
        //Get a reference to the listView & set number of columns
        ArrayList<Item> stopListPairs = new ArrayList<>();

        final BotBarAdapter busListItemAdapter;
        if (fragMode == 0 && show == 0) {
            for (int x = 0; x < MainActivity.routeList.size(); x++) {
//                if(x==0){
//                    Log.d("D",fragMode+" "+show+" "+MainActivity.routeList.get(x).getRouteName());
//                }
                stopListPairs.add(new Item(MainActivity.routeList.get(x).getRouteName(),
                        MainActivity.routeList.get(x).getRouteNameString(),x));
            }
            busListItemAdapter = new BotBarAdapter(getActivity()
                    .getApplicationContext(), "botbar_route_icon", stopListPairs,fragMode,getActivity());
        } else {
            for (int x = 0; x < stopList.size(); x++) {
//                if(x==0){
//                    Log.d("D",fragMode+" "+show+" "+MainActivity.routeList.get(x).getRouteName());
//                }
                stopListPairs.add(new Item(stopList.get(x).getStopID(),
                        stopList.get(x).getStopName(),x));
            }
            busListItemAdapter = new BotBarAdapter(getActivity()
                    .getApplicationContext(), "botbar_stop_icon", stopListPairs,fragMode,getActivity());
        }

        //Adapter to create bus schedule list
        //Get a reference to the listView & set number of columns
        final GridView gridView = (GridView) view.findViewById(R.id.list_view_bus);
        float scale = getActivity().getResources().getDisplayMetrics().density;

        //Set width_param and convert to display independent pixels/buttons
        //100dp is the square length for each button
        LinearLayout.LayoutParams params;
        if(fragMode==0&&show==0) {
            gridView.setNumColumns(MainActivity.routeList.size());
            params = new LinearLayout.LayoutParams((int)
                    (MainActivity.routeList.size() * BUTTONSIZE * scale + 0.5f)
                    , (int) (BUTTONSIZE * scale + 0.5f));
        }else{
            gridView.setNumColumns(stopList.size());
            params = new LinearLayout.LayoutParams((int)
                    (stopList.size() * BUTTONSIZE * scale + 0.5f)
                    , (int) (BUTTONSIZE * scale + 0.5f));
        }

        //Inject the strings into the columns also insert the icon
        //Log.d("D","Setting adapter : "+busListItemAdapter.getItem(0).first());
        gridView.setAdapter(busListItemAdapter);
        gridView.setLayoutParams(params);
        /****************************Listeners*****************************/

        /******************************************************************/
        /***********Add Stops to Favourites********************************/
        /******************************************************************/
        if (fragMode != 1 && show == 1) {
            gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    boolean repeatFav = false;
                    Stop selectedStop = stopList.get(position);
                    for (int i = 0; i < MainActivity.favoriteList.size(); i++) {
                        if (MainActivity.favoriteList.get(i).getStopID().equals(selectedStop.getStopID())) {
                            Toast.makeText(getActivity().getApplicationContext(), "Stop " + selectedStop.getStopID()
                                    + " is already in Favorites.", Toast.LENGTH_SHORT).show();
                            repeatFav = true;
                        }
                    }
                    if (!repeatFav) {
                        MainActivity.favoriteList.add(0, selectedStop);
                        Toast.makeText(getActivity().getApplicationContext(), "Stop " + selectedStop.getStopID()
                                + " has been added to Favorites.", Toast.LENGTH_SHORT).show();
                    }

                    return true;
                }
            });
        } else if (fragMode == 1) {
            //gridView.setOnItemLongClickListener(new OnItemLongClickListenerBotBar(gridView));
            gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                //String st;
                //final class MyTouchListener implements AdapterView.OnItemLongClickListener {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                    // start dragging the item touched
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        v.startDragAndDrop(data, shadowBuilder, v, 0);
                    } else {
                        v.startDrag(data, shadowBuilder, v, 0);
                    }
                    //Stop s = null;
                    MainActivity.selected = busListItemAdapter.getItem(position);
                    final CharSequence st = busListItemAdapter.getItem(position).first();
                    //Log.d("D", "i am selected " + position + ": " + busListItemAdapter.getItem(position).first());
                    //MainActivity.st = busListItemAdapter.getItem(position).first();
                    return true;
                }
            });
            /******************************************************************/
            /***********Reorganizing favourites *******************************/
            /******************************************************************/
            /**Make Rects from areas of RelativeLayouts for detecting when user drags into it
             to scroll the HorizontalScrollView*/
            MainActivity.scrollAreas=new Rect[2];
            dragAreas=new HashMap<>();
            int index=0;
            //2 scroll areas
            for(int x = 0; x<2; ++x) {
                View scrollArea=null;
                int[] dragID = {R.id.dragAreaLeft, R.id.dragAreaRight};
                scrollArea = container.findViewById(R.id.fragment_busbar);
                if(scrollArea!=null){
                    scrollArea =  container.findViewById(R.id.fragment_busbar).findViewById(dragID[x]);
                    if(scrollArea!=null){
                        if (scrollArea instanceof RelativeLayout) {
                            int[] xy = new int[2];
                            scrollArea.getLocationOnScreen(xy);
                            if (xy.length > 0) {
                                MainActivity.scrollAreas[index] = new Rect(xy[0], xy[1],
                                        xy[0] + scrollArea.getWidth(),
                                        xy[1] + scrollArea.getHeight());
                            }
                            int values[]={MainActivity.scrollAreas[index].width(),
                                    MainActivity.scrollAreas[index].height(),
                                    MainActivity.scrollAreas[index].left,
                                    MainActivity.scrollAreas[index].right,
                                    MainActivity.scrollAreas[index].top,
                                    MainActivity.scrollAreas[index].bottom};

                            CharSequence keys[] = {"left", "right"};
                            //For coordinates location relative to the screen/display
                            dragAreas.put(keys[index],new HashMap<CharSequence, Integer>());

                            CharSequence otherKeys[]={"width",
                                    "height",
                                    "left",
                                    "right",
                                    "top",
                                    "bottom"};

                            for(int z=0;z<otherKeys.length;z++){
                                dragAreas.get(keys[index]).put(otherKeys[z],values[z]);
                            }
//                            Log.d("D", "\nRect " + index);
//                            Log.d("WIDTH        :", String.valueOf(MainActivity.scrollAreas[index].width()));
//                            Log.d("HEIGHT       :", String.valueOf(MainActivity.scrollAreas[index].height()));
//                            Log.d("left         :", String.valueOf(MainActivity.scrollAreas[index].left));
//                            Log.d("right        :", String.valueOf(MainActivity.scrollAreas[index].right));
//                            Log.d("top          :", String.valueOf(MainActivity.scrollAreas[index].top));
//                            Log.d("bottom       :", String.valueOf(MainActivity.scrollAreas[index].bottom));
                            index++;
                        }
                    }
                }

            }

            gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                //String st;
                //final class MyTouchListener implements AdapterView.OnItemLongClickListener {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                    // start fake dragging the item touched
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        v.startDragAndDrop(data, shadowBuilder, v, 0);
                    } else {
                        v.startDrag(data, shadowBuilder, v, 0);
                    }
                    //Stop s = null;
                    MainActivity.selected = busListItemAdapter.getItem(position);
                    final CharSequence st = busListItemAdapter.getItem(position).first();
                    //Log.d("D", "i am selected " + position + ": " + busListItemAdapter.getItem(position).first());
                    //MainActivity.st = busListItemAdapter.getItem(position).first();
                    return true;
                }
            });
        }
            /********************************************************************/
            /**Button Click Listener**********************************************/
            /********************************************************************/
            //Create Click Route Listener
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //showing routes
                    if (fragMode == 0 && show == 0) {
                        newFragment = new FragmentBotBar();
                        // Create fragment and give it an argument specifying the article it should show
                        stopList = MainActivity.routeList.get(position).getStopList();

                        Bundle args = new Bundle();
                        args.putInt("EXTRA_ROUTE_POS", position);
                        args.putInt("SHOW", 1);
                        args.putInt("FRAG_MODE", 0);
                        MainActivity.panelMode = 0;
                        newFragment.setArguments(args);
                        // Replace whatever is in the fragment_container view with this fragment,
                        // and add the transaction to the back stack so the user can navigate back
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.bot_bar_frag_view, newFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();

                    } else {
                        //Show a list of stops
                        MainActivity.panelMode = fragMode;
                        //Closing Previous popups
                        if (stopPopUp != null) {
                            stopPopUp.dismiss();
                        }
                        //Code for getting activity for stopPopup [Jackson Keenan START]
                        WindowManager wm = (WindowManager) getActivity().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                        Display display = wm.getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);

                        LayoutInflater popUpLayout = getActivity().getLayoutInflater();
                        View popUpView = popUpLayout.inflate(R.layout.popup, null);

                        int routePos = -1;
                        if (fragMode != 0) {
                            for (int x = 0; x < MainActivity.routeList.size(); x++) {
                                if (MainActivity.routeList.get(x).getRouteName().
                                        equals(stopList.get(position).getRouteName())) {
                                    routePos = x;
                                    break;
                                }
                            }
                        } else {
                            for (int x = 0; x < stopList.size(); x++) {
                                if (stopList.get(x).getStopName().
                                        equals(stopList.get(position).getStopName())) {
                                    routePos = x;
                                    break;
                                }
                            }
                        }

                        stopPopUp = new StopPopup(popUpView, ViewGroup.LayoutParams.WRAP_CONTENT
                                , size.x, routePos, position, MainActivity.serverURL, fragMode);
                        //Scrape next bus times, popup will automatically add
                        //JSON Parsed times when loadBus class is complete
                        stopPopUp.showAsDropDown(popUpView, 10, MainActivity.topHeight);
                        //PopUpWindow: Jackson Keenan [END]

                        //Stop Location Markers: Jackson Keenan [START]
                        //Get Stop Long + Lat
                        Float stopLat = stopList.get(position).getLatitude();
                        Float stopLong = stopList.get(position).getLongitude();
                        LatLng stopLatLong = new LatLng(stopLat, stopLong);

                        //Add Yellow Stop Marker
                        if (stopMarker != null) stopMarker.remove();
                        stopMarker = MainActivity.mainMap.addMarker(new MarkerOptions()
                                .position(stopLatLong)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                .title(stopList.get(position).getStopID().toString()));
                        //Change Zoom to 14 / 15 for release
                        MainActivity.mapUpdate = CameraUpdateFactory.newLatLngZoom(stopLatLong, 15);
                        MainActivity.mainMap.animateCamera(MainActivity.mapUpdate);

                        //Stop Location Markers: Jackson Keenan [END]
                    }

                }//end onItemClick
            });

            /******************************************************************/
            /**Click Listener for Map Markers *********************************/
            /******************************************************************/
            MainActivity.mainMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    WindowManager wm = (WindowManager) getContext().getSystemService(getContext().WINDOW_SERVICE);
                    Display display = wm.getDefaultDisplay();
                    final Point size = new Point();
                    display.getSize(size);
                    Log.d("D",marker.getTitle());
                    if(marker.getTitle().contains(",")) {
                        MainActivity.panelMode = 4;
                        List<String> localStopList = new ArrayList<String>(Arrays.asList(marker.getTitle().split(",")));
                        final CharSequence locStops[] = new CharSequence[localStopList.size()];
                        String overlapStopName = " ";
                        for (int x = 0; x < localStopList.size(); x++) {
                            for (int j = 0; j < MainActivity.routeList.size(); j++) {
                                for (int k = 0; k < MainActivity.routeList.get(j).getStopList().size(); k++) {
                                    if (MainActivity.routeList.get(j).getStopList().get(k).getStopID().equals(localStopList.get(x))) {
                                        locStops[x] = "Route " + MainActivity.routeList.get(j).getRouteName() + ": (" + localStopList.get(x) + ")";
                                        overlapStopName = MainActivity.routeList.get(j).getStopList().get(k).getStopName().toString();
                                    }
                                }
                            }
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle(overlapStopName);
                        builder.setItems(locStops, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (int j = 0; j < MainActivity.routeList.size(); j++) {
                                    for (int k = 0; k < MainActivity.routeList.get(j).getStopList().size(); k++) {
                                        if (locStops[which].toString().contains(MainActivity.routeList.get(j).getStopList().get(k).getStopID().toString())) {
                                            //Closing Previous popup
                                            if (stopPopUp != null) {
                                                stopPopUp.dismiss();
                                            }

                                            LayoutInflater popUpLayout = getActivity().getLayoutInflater();
                                            View popUpView = popUpLayout.inflate(R.layout.popup, null);
                                            stopPopUp = new StopPopup(popUpView, ViewGroup.LayoutParams.WRAP_CONTENT
                                                    , size.x, j, k, MainActivity.serverURL, fragMode);

                                            //Scrape next bus times, popup will automatically add
                                            //JSON Parsed times when loadBus class is complete

                                            stopPopUp.showAsDropDown(popUpView, 10, MainActivity.topHeight);

                                            //Get Stop Long + Lat
                                            Float stopLat = MainActivity.routeList.get(j).getStopList().get(k).getLatitude();
                                            Float stopLong = MainActivity.routeList.get(j).getStopList().get(k).getLongitude();
                                            LatLng stopLatLong = new LatLng(stopLat, stopLong);

                                            //Clear Old Markers
                                            MainActivity.mainMap.clear();

                                            //Add Stop Marker
                                            MainActivity.mainMap.addMarker(new MarkerOptions()
                                                    .position(stopLatLong)
                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                                    .title(MainActivity.routeList.get(j).getStopList().get(k).getStopID().toString()));
                                            MainActivity.mapUpdate = CameraUpdateFactory.newLatLng(stopLatLong);
                                            MainActivity.mainMap.animateCamera(MainActivity.mapUpdate);

                                            //Stop Location Markers: Jackson Keenan [END]
                                        }
                                    }
                                }
                            }
                        });
                        marker.hideInfoWindow();
                        //MainActivity.mainMap.animateCamera(mapUpdate);
                        builder.show();
                    }else{
                        for (int billybob = 0; billybob < stopList.size(); billybob++) {

                            if (stopList.get(billybob).getStopID().equals(marker.getTitle())) {
                                Log.d("D", "True");
                            /*Setup*/
                                if (stopPopUp != null) {
                                    stopPopUp.dismiss();
                                } //Closing Previous popup

                                //Code for getting activity for stopPopup [Jackson Keenan START]
                                LayoutInflater popUpLayout = getActivity().getLayoutInflater();
                                View popUpView = popUpLayout.inflate(R.layout.popup, null);
                                //Jackson Keenan [END]
                                //Custom popup class for showing popup with information about the stop
                                int routePos = -1;
                                for (int x = 0; x < MainActivity.routeList.size(); x++) {
                                    if (MainActivity.routeList.get(x).getRouteName().
                                            equals(stopList.get(billybob).getRouteName())) {
                                        routePos = x;
                                        break;
                                    }
                                }

                                stopPopUp = new StopPopup(popUpView, ViewGroup.LayoutParams.WRAP_CONTENT
                                        , size.x, routePos, billybob, MainActivity.serverURL, fragMode);
                                //Scrape next bus times, popup will automatically add
                                //JSON Parsed times when loadBus class is complete
                                /*Display PopUp*/
                                stopPopUp.showAsDropDown(popUpView, 10, MainActivity.topHeight);
                                //PopUpWindow: Jackson Keenan [END]

                                //Stop Location Markers: Jackson Keenan [START]

                                //Get Stop Long + Lat
                                Float stopLat = stopList.get(billybob).getLatitude();
                                Float stopLong = stopList.get(billybob).getLongitude();
                                LatLng stopLatLong = new LatLng(stopLat, stopLong);

                                //Add Yellow Stop Marker
                                if (stopMarker != null) stopMarker.remove();
                                stopMarker = MainActivity.mainMap.addMarker(new MarkerOptions()
                                        .position(stopLatLong)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                        .title(MainActivity.routeList.get(routePos).getStopList().get(billybob).getStopID().toString()));
                                MainActivity.mapUpdate = CameraUpdateFactory.newLatLng(stopLatLong);
                                MainActivity.mainMap.animateCamera(MainActivity.mapUpdate);
                                //Stop Location Markers: Jackson Keenan [END]
                                break;
                            }
                        }
                    }

                    return true;
                }
            });
        }
    }

