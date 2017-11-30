package velocityraptor.guelphtransit.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import velocityraptor.guelphtransit.R;
import velocityraptor.guelphtransit.main.AboutPage.ActivityAboutPage;
import velocityraptor.guelphtransit.main.activityScheduleBus.ActivityScheduleBus;
import velocityraptor.guelphtransit.main.activityScreenSlidePager.ActivityScreenSlidePager;
import velocityraptor.guelphtransit.main.databaseClasses.DBController;
import velocityraptor.guelphtransit.main.databaseClasses.LoadStops;
import velocityraptor.guelphtransit.main.databaseClasses.LoadTime;
import velocityraptor.guelphtransit.main.stopPopup.StopPopup;

/**
 * This class defines the Fragment for the main bottom bar, filled with Bus Stops
 * Authors: Nic Durish, Aidan Maher, Anthony Mazzawi & Jackson Keenan.
 * <p>
 * I have exclusive control over this submission via my password.
 * By including this statement in this header comment, I certify that:
 * 1) I have read and understood the University policy on academic integrity;
 * 2) I have completed the Computing with Integrity Tutorial on Moodle; and
 * I assert that this work is my own. I have appropriately acknowledged any and all material
 * (data, images, ideas or words) that I have used, whether directly quoted or paraphrased.
 * Furthermore, I certify that this assignment was prepared by me specifically for this course.
 */
public class MainActivity extends AppCompatActivity {

    // Getting main context & activity
    public Context thisContext;

    //database IP
    public static final String serverURL = "http://ec2-54-202-132-186.us-west-2.compute.amazonaws.com/androidSqlConnect/";

    // List of Routes for
    public static ArrayList<Stop> favoriteList = new ArrayList<>();
    public static ArrayList<Route> routeList = new ArrayList<>();
    public static ArrayList<Stop> locationList = new ArrayList<>();
    //list of stopPopups
    public static ArrayList<StopPopup> stopPopupList = new ArrayList<>();

    //Update Schedule variables
    //If the autoUpdate feature is enabled
    static boolean autoUpdate;
    //last time the Database was updated
    static String lastDBUpdateTime = "";
    //The time the DB was created on the phone
    static String serverDBCreateTime = "";

    //Bottom Panel Control (0=Home, 1=Favourites ,2=My Location,
    // 3=Secondary State(Favourites), 4=Secondary State(Location))
    public static int panelMode = 0;

    //Height of Top Bars, used in placing stopPopup (Set in onCreateOptionsMenu)
    public static int topHeight;

    //Fragment for the Bottom Bar (Routes)
    private FragmentBotBar busBarFragment;

    //popUp
    public StopPopup stopPopup;

    //Google Map API
    public static GoogleMap mainMap;
    public static CameraUpdate mapUpdate;

    //Loc Manager
    private final LatLng LOCATION_GUELPH = new LatLng(43.5500, -80.2500);
    private LocationManager locManager;

    //Database functions
    DBController dbController;

    //Alert Dialogs
    AlertDialogController ADC;

    //Determine if the app has been opened for the first time
    public enum AppStart {
        FIRST_TIME, FIRST_VERSION, NORMAL
    }

    private static final String LAST_APP_VERSION = "last_app_version";
    private FragmentManager fragmentManager = getSupportFragmentManager();
    public static Item selected =null;
    public static Rect scrollAreas[]=null;

    /***********
     * METHODS *
     ***********/

    /**
     * Called when the activity is first created
     * <p>
     * Determine if the app has been started for the first time or if it's normal
     * <p>
     * When the phone starts up normally, the phone will get the last time the
     * server database has been updated and the time the phone db has been updated
     * <p>
     * Initialize DBController; if the db is empty, load from server
     * <p>
     * Map & Location Manager Setup
     * <p>
     * Set Static Context variable
     * <p>
     * Set Button PanelMode to 0
     * 0=Home,
     * 1=Favourites ,
     * 2=My Location,
     * 3=Secondary State(Favourites),
     * 4=Secondary State(Location)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Determine if the app has been started for the first time or if it's normal */
        switch (checkAppStart()) {
            /* When the phone starts up normally, the phone will get the last time the
               server database has been updated and the time the phone db has been updated */
            case NORMAL:
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                lastDBUpdateTime = preferences.getString("lastDBUpdateTime", "");
                new LoadTime().execute();
                break;
            case FIRST_VERSION:
                //show what's new
                break;
            case FIRST_TIME:
                Intent intent = new Intent(this, ActivityScreenSlidePager.class);
                startActivity(intent);
                break;
            default:
                break;
        }


        // Initialize DBController; if the db is empty, load from server
        dbController = new DBController(getApplicationContext());
        ADC = new AlertDialogController(this);
        if (dbController.getCount() <= 0) {
            new LoadStops(this).execute();
            new LoadTime().execute();
        }

        // Insert the routes into the route list
        dbController.insertRoutes(routeList);
        // Insert the stops into the stopLists inside the routeList
        dbController.insertAllStops(routeList);
        //sort the routeList
        Collections.sort(routeList,Route.routeSort);
        //Set Button PanelMode
        panelMode = 0;
        // Initialize BottomBar Fragment
        busBarFragment = new FragmentBotBar();
        //Set to Route mode (0 is Route, 1 is Favourites, 2 is Location Button)
        Bundle args = new Bundle();
        args.putInt("FRAG_MODE", panelMode);
        busBarFragment.setArguments(args);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.bot_bar_frag_view, busBarFragment);
        fragmentTransaction.commit();
        //Set View of Contents
        setContentView(R.layout.activity_main);

        //Map & Location Manager Setup [Jackson Keenan]
        mainMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        mapUpdate = CameraUpdateFactory.newLatLngZoom(LOCATION_GUELPH, 12);
        mainMap.animateCamera(mapUpdate);
        locManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //Set Static Context
        thisContext = getApplicationContext();

        //colour the home, favourite (heart) or location button depending on panelMode
        setPanelColour();

        ListView list;
        LegendAdapter adapter = new LegendAdapter(MainActivity.this,
                new String[]{
                    "More than 15 mins",
                    "5 - 15 mins",
                    "Less than 5 mins"});

        list=(ListView)findViewById(R.id.legend);
        list.setAdapter(adapter);
    }

    /**
     * Called when the activity is about to become visible
     * <p>
     * Writes to debug log that the activity is starting
     */
    @Override
    protected void onStart() {
        super.onStart();

        Log.d("Android: ", "Activity Starting");
    }

    /**
     * Called when the activity has become visible
     * <p>
     * Writes to debug log that the activity is resuming
     * <p>
     * Shared preferences are stored in the phone even when the app closes
     * when the app starts up auto update will be toggled to whatever has been
     * stored, if there is nothing there than it will default to false
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Android: ", "Activity Resuming");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        autoUpdate = preferences.getBoolean("autoUpdate", false);
        if (autoUpdate) {
            checkUpdateTime();
        }
        String fav = preferences.getString("favourites", "");
        favoriteList.clear();
        if (!fav.equals("")) {
            String tokens[] = fav.split(",");
            for (CharSequence token : tokens) {
                favoriteList.add(dbController.getStop(token));
            }
        }

        // Sort the Routes List when the activity resumes
    }

    /**
     * Called when another activity is taking focus
     * Saves user preferences and favourites
     * Writes to debug log that the activity is paused
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Android: ", "Activity Paused");
        if (autoUpdate) {
            new LoadTime().execute();
        }
        /* When the user leaves the activities, the preferences will be saved */
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("autoUpdate", autoUpdate);
        editor.putString("lastDBUpdateTime", lastDBUpdateTime);

        String fav = "";
        for (int i = 0; i < favoriteList.size(); i++) {
            if (i == 0) {
                fav = favoriteList.get(i).getStopID() + ",";
            } else {
                fav += favoriteList.get(i).getStopID() + ",";
            }
        }

        if (fav.equals("")) {
            editor.putString("favourites", "");
        } else {
            editor.putString("favourites", fav);
        }
        editor.apply();
    }

    /**
     * Called when the activity is no longer visible
     * Writes to debug log that the activity has stopped
     */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Android: ", "Activity Stopped");
    }

    /**
     * Called when the activity is being destroyed
     * Tells debugger to stop tracing
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Android: ", "Activity Destroyed");
        android.os.Debug.stopMethodTracing();
    }

    /**
     * Inflates the menu (3 dots) from the xml menu_main.xml
     *
     * @param menu
     * @return boolean if complete
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //Getting Height Of Top Status bar for PopUp placement
        topHeight = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        Rect r = new Rect();
        Window w = getWindow();
        w.getDecorView().getWindowVisibleDisplayFrame(r);
        topHeight = topHeight + r.top;

        return true;
    }

    /**
     * Called when the Overflow (3 dots menu button) is pressed
     *
     * @param item the selected item from the (3 dots) menu button
     * @return when the operation has completed successfully it returns true
     * Otherwise returns the result of the superclass's onOptionItemSelected method
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here.
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            //show the list of schedules
            case R.id.action_schedule:
                //Log.d("D", "action_schedule");
                intent = new Intent(this, ActivityScheduleBus.class);
                startActivity(intent);
                break;

            //show the about page
            case R.id.action_about:
                //Log.d("D", "action_about");
                intent = new Intent(this, ActivityAboutPage.class);
                startActivity(intent);
                break;

            //show the help page
            case R.id.action_help:
                //Log.d("D", "action_help");
                intent = new Intent(this, ActivityScreenSlidePager.class);
                startActivity(intent);
                break;

            //Clear the favourites
            case R.id.action_clear_favorites:
                //Log.d("D", "action_clear_favorites");
                favoriteList.clear();
                Toast.makeText(getApplicationContext(), "Your Favorites have been cleared."
                        , Toast.LENGTH_LONG).show();

                //perform home button click
                findViewById(R.id.button_home).performClick();
                break;

            //Update the schedules
            case R.id.action_update_schedules:
                //Log.d("D", "action_update_schedules");
                //Get server time
                new LoadTime().execute();
                //See if time differs
                checkUpdateTime();
                break;

            //Toggle the autoupdate feature
            case R.id.action_toggle_autoupdate:
                //Log.d("D", "action_toggle_autoupdate");
                ADC.autoUpdateDialog(autoUpdate);
                break;
            default:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Compare the phone db time with the server time
     * if it's different, then it will be updated
     * if not then it will show a pop up saying the db it up to date
     */
    public void checkUpdateTime() {
        /* Compare the phone db time with the server time
                   if it's different, then it will be updated
                   if not then it will show a pop up saying the db it up to date */
        if (lastDBUpdateTime.equals(serverDBCreateTime)) {
            //Log.d("D", "DB is up to date!");
            ADC.updateDialog(true);
        } else {
            //Log.d("D", "DB is updating!");
            lastDBUpdateTime = serverDBCreateTime;
            dbController.deleteDB();
            new LoadStops(this).execute();
            new LoadTime().execute();
        }
    }

    /**
     * Sets the buttons colours on the main panel along the bottom
     * The three buttons are the home, favorite and loation icon
     * Changed when "modes" are switched
     * 0=Home, 1=Favourites ,
     * 2=My Location,
     * 3=Secondary State(Favourites),
     * 4=Secondary State(Location)
     */
    public void setPanelColour() {

        String homeImage = "@drawable/home_icon";
        String favImage = "@drawable/favorite_icon";
        String locImage = "@drawable/location_icon";
        //Bottom Panel Control (0=Home, 1=Favourites ,2=My Location, 3=Secondary State(Favourites), 4=Secondary State(Location))
        switch (panelMode) {
            case 0://home icon is highlighted
                homeImage = "@drawable/blue_home_icon";
                break;
            case 1://fav icon is highlighted
                favImage = "@drawable/blue_favorite_icon";
                break;
            case 2://location icon is highlighted
                locImage = "@drawable/blue_location_icon";
                break;
        }

        //Set home, favorite and location buttons

        ((ImageView) findViewById(R.id.button_image_home)).setImageDrawable(
                getResources().getDrawable(getResources().getIdentifier(
                        homeImage, null, getPackageName())));

        ((ImageView) findViewById(R.id.button_image_fav)).setImageDrawable(
                getResources().getDrawable(getResources().getIdentifier(
                        favImage, null, getPackageName())));

        ((ImageView) findViewById(R.id.button_image_loc)).setImageDrawable(
                getResources().getDrawable(getResources().getIdentifier(
                        locImage, null, getPackageName())));

    }

    /**
     * Helper function for location (loc) and homeButtonClick.
     * Closes open dialog boxes safely.
     */
    public void dismissOpenFrames() {
        //Log.d("D", "Close Open");
        try {
            if (stopPopupList != null && stopPopupList.size() > 0) {
                for (int x = 0; x < stopPopupList.size(); x++) {
                    stopPopupList.get(x).dismiss();
                }
                stopPopupList.clear();
            }
        } catch (Exception noPopUpToClose) {
            noPopUpToClose.printStackTrace();
        }
        try {
            if (busBarFragment != null &&
                    busBarFragment.newFragment != null &&
                    busBarFragment.newFragment.stopPopUp != null) {
                busBarFragment.newFragment.stopPopUp.dismiss();
            }
        } catch (Exception noPopUpToClose) {
            noPopUpToClose.printStackTrace();
        }
        try {
            if (stopPopup != null) {
                stopPopup.dismiss();
            }
        } catch (Exception noPopUpToClose) {
            noPopUpToClose.printStackTrace();
        }
    }

    /**
     * Method called when favorite, location or home are pressed
     * panelModes are switched in this method
     * Bottom Panel Control (0=Home, 1=Favourites ,2=My Location, 3=Secondary State(Favourites), 4=Secondary State(Location))
     *
     * @param view Default android view parameter set in activity_main.xml
     */
    public void buttonClick(View view) {
        // Potentially Closing Open PopupDialog
        dismissOpenFrames();
        mainMap.clear();
        FragmentTransaction transaction;
        switch (view.getId()) {
            case R.id.button_home:
                //Log.d("D", "home pressed");
                panelMode = 0;
                //Reloading Route Bar
                busBarFragment = new FragmentBotBar();
                //Set to Route mode (0 is Route, 1 is Favourites, 2 is Location Button)
                Bundle args = new Bundle();
                args.putInt("FRAG_MODE", panelMode);
                args.putInt("SHOW", 0);
                busBarFragment.setArguments(args);
                transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.bot_bar_frag_view, busBarFragment);
                transaction.commit();
                mapUpdate = CameraUpdateFactory.newLatLngZoom(LOCATION_GUELPH, 12);
                mainMap.animateCamera(mapUpdate);


                break;
            case R.id.button_favorites:
                //Log.d("D", "favourites pressed");
                panelMode = 1;
                busBarFragment = new FragmentBotBar();
                // Create fragment and give it an argument specifying the article it should show
                args = new Bundle();
                args.putInt("FRAG_MODE", panelMode);
                busBarFragment.setArguments(args);

                transaction = fragmentManager.beginTransaction();
                // Replace whatever is in the fragment_container view with this fragment,
                transaction.replace(R.id.bot_bar_frag_view, busBarFragment);
                transaction.commit();
                mapUpdate = CameraUpdateFactory.newLatLngZoom(LOCATION_GUELPH, 12);
                mainMap.animateCamera(mapUpdate);

                break;
            // when location button is clicked
            case R.id.button_location:
                locationList.clear();
                //Log.d("D", "location pressed");
                //Bottom Panel Control (0=Home, 1=Favourites ,2=My Location,
                // 3=Secondary State(Favourites), 4=Secondary State(Location))
                panelMode = 2;
                /**
                 * Current Location [Jackson Keenan]
                 * Finding Device Location and set camera to it
                 */
                Location myCurrentLoc = locManager.getLastKnownLocation(locManager.getBestProvider(new Criteria(), true));
                //LatLng For Loc Marker
                LatLng myLocLatLng;
                //Loc. Unavailable
                if (myCurrentLoc == null) {
                    myLocLatLng = LOCATION_GUELPH;
                }//Loc. Available
                else {
                    myLocLatLng = new LatLng(myCurrentLoc.getLatitude(), myCurrentLoc.getLongitude());
                }
                busBarFragment.setLoc(myLocLatLng);
                /** Add the Blue marker indicating your location marker and zoom on it*/
                mainMap.addMarker(new MarkerOptions().position(myLocLatLng).icon
                        (BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .title("You Are Here"));
                MainActivity.mapUpdate = CameraUpdateFactory.newLatLngZoom(myLocLatLng, 13);
                MainActivity.mainMap.animateCamera(MainActivity.mapUpdate);

                /** Find the nearby stops and add their pins*/
                locationList = new ArrayList<>();
                /**Finding distance between current location and stop locations*/
                for (Route r : MainActivity.routeList) {
                    for (Stop s : r.getStopList()) {
                        double distFromLoc = this.determineDistance(myLocLatLng,
                                new LatLng(s.getLatitude(),s.getLongitude()));
                        //If Stop is within radius add to ArrayList<Stop> locationList
                        if (distFromLoc <= 0.5) {
                            /**Add this stop to the location list, it's <=.5km away*/
                            locationList.add(0, s);
                        }
                    }
                }
                /** See which stops overlap and which are individuals*/
                ArrayList<Stop> overlapMarker = new ArrayList<>();
                ArrayList<Stop> markers = new ArrayList<>();
                for(Stop s : locationList){
                    for(Stop t : locationList){
                        if(s.getLatitude()==t.getLatitude()&&t.getLongitude()==s.getLongitude()&&
                                !s.getStopID().toString().equals(t.getStopID().toString())){

                            if(!overlapMarker.contains(s)&&!overlapMarker.contains(t)){
                                //Log.d("D",t.getStopID()+" and "+s.getStopID()+" overlap!");
                                overlapMarker.add(s);
                                overlapMarker.add(t);
                            }


                        }
                    }

                }
                /** the individual stops are the ones that don't overlap*/
                for(Stop s : locationList){
                    if(!overlapMarker.contains(s)){
                        markers.add(s);
                    }
                }
                /**place individual markers*/
                for(Stop s : markers){
                    int colour = determineColour(s);
                    Log.d("D"," adding individual "+s.getStopID());
                    mainMap.addMarker(new MarkerOptions()
                                .position(
                                        new LatLng(s.getLatitude(), s.getLongitude()))
                                .icon(BitmapDescriptorFactory.fromResource(colour))
                                .title(s.getStopID().toString()));

                }

                /** dont add the same coordinates twice*/
                ArrayList<LatLng> checked = new ArrayList<>();
                ArrayList<LatLng> overLap = new ArrayList<>();
                for(int x=0;x<overlapMarker.size();x++){
                    overLap.add(new LatLng(overlapMarker.get(x).getLatitude(),overlapMarker.get(x).getLongitude()));
                }
                for(LatLng l : overLap){
                    if(!checked.contains(l)){
                        for(LatLng t : overLap){
                            if(t.longitude==l.longitude&&t.latitude==l.latitude){
                                if(!checked.contains(t)) {
                                    checked.add(t);
                                }
                            }
                        }
                    }
                }

                /**draw overlapping stops*/
                ArrayList<LatLng> added = new ArrayList<>();
                for(Stop s : overlapMarker){
                    for(LatLng l : checked){
                        if(s.getLatitude()==l.latitude&&s.getLongitude()==l.longitude&&
                            !added.contains(l)){
                            added.add(l);
                            Log.d("D"," adding overlap" + s.getStopID());
                            String title = s.getStopID().toString();
                            for(Stop t : overlapMarker){
                                if(t.getLongitude()==s.getLongitude()&&t.getLatitude()==s.getLatitude()
                                        &&!t.getStopID().toString().equals(s.getStopID().toString())
                                        && !title.contains(t.getStopID().toString())){
                                    title+=","+t.getStopID().toString();
                                }
                            }
                            Log.d("D",title);
                            mainMap.addMarker(new MarkerOptions()
                                    .position(
                                            new LatLng(s.getLatitude(), s.getLongitude()))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.green_dot))
                                    .title(title));
                        }
                    }
                }

//                /**Add all the pins in the location list which is the close by stops */
//                for (int i = 0; i < locationList.size(); i++) {
//                    placedStop = false;
//                    /**See if this pin overlaps another*/
//                    for (int j = 0; j < stopMarker.size(); j++) {
//                        LatLng latLngNearby = new LatLng(locationList.get(i).getLatitude(),
//                                locationList.get(i).getLongitude());
//                        double distFromLoc = this.determineDistance(latLngNearby, i, j, stopMarker);
//                        if (distFromLoc <= 0.05) {
//                            /**Set title for marker clicker to indicate it overlaps*/
//                            if (!stopMarker.get(j).getTitle().contains(locationList.get(i).getStopID())) {
//                                stopMarker.get(j).setTitle(stopMarker.get(j).getTitle()
//                                        + "," + locationList.get(i).getStopID());
//                                Log.d("D", "Set title " + stopMarker.get(j).getTitle());
//                            }
//                            placedStop = true;
//                        }
//                    }//end loop
//
//                    /**Place location stops that don't overlap*/
//                    if (!placedStop) {
//                        ArrayList<Stop> dummy = new ArrayList<>();
//                        dummy.add(locationList.get(i));
//                        placeColouredPins(dummy);
//                        dummy.clear();
//                        MainActivity.mainMap.animateCamera(MainActivity.mapUpdate);
//
//                        stopMarker.add(mainMap.addMarker(new MarkerOptions()
//                                .position(
//                                        new LatLng(locationList.get(i).getLatitude(), locationList.get(i).getLongitude()))
//
//                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.green_dot))
//                                .title(locationList.get(i).getStopID().toString())));
//                    }
//
//                }


                // Create fragment and give it an argument specifying the article it should show
                busBarFragment = new FragmentBotBar();
                args = new Bundle();
                args.putInt("FRAG_MODE", panelMode);
                busBarFragment.setArguments(args);

                FragmentManager fragmentManager = getSupportFragmentManager();
                transaction = fragmentManager.beginTransaction();

                // Replace whatever is in the fragment_container view with this fragment,
                transaction.replace(R.id.bot_bar_frag_view, busBarFragment);
                transaction.commit();
                break;
        }
        setPanelColour();

    }
    public String getDayofWeeek(){
        SimpleDateFormat tempDay = new SimpleDateFormat("EE");
        Calendar cal = Calendar.getInstance();
        return tempDay.format(cal.getTime());

    }
    public String getTimeString(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateTime = new SimpleDateFormat("HHmm");
        return dateTime.format(cal.getTime());
    }
    public int determineColour(Stop s){
        ArrayList<CharSequence> timeList;
        CharSequence listIterator;
        String dayOfWeek = getDayofWeeek();
        String currentTime =getTimeString();
        switch (getDayofWeeek()) {
            case "Sat":
                timeList = s.getSatTimes();
                break;
            case "Sun":
                timeList = s.getSunTimes();
                break;
            default:
                timeList = s.getWeekTimes();
                break;
        }

        int stopInt = -1;
        for (int j = 0; j < timeList.size(); j++) {
            listIterator = timeList.get(j);
            listIterator = listIterator.toString().replace(":", "");
            if (Integer.parseInt(currentTime) < Integer.parseInt(listIterator.toString())) {
                stopInt = j;
                break;
            }
        }

        String stopTime = timeList.get(stopInt).toString().replace(":", "");
        currentTime = currentTime.replace(":", "");
        int difference = Integer.parseInt(stopTime) - Integer.parseInt(currentTime);

        //Based on difference in time, choose different pin colour
        int pinResourceID;
        if (difference >= 15) {
            pinResourceID = R.drawable.green_dot;
        } else if (difference >= 5 && difference < 15) {
            pinResourceID = R.drawable.yellow_dot;
        } else if (difference < 5) {
            pinResourceID = R.drawable.red_dot;
        } else {
            pinResourceID = R.drawable.red_dot;
        }
        return pinResourceID;
    }
    /**
     * Places a stopList on the map with coloured pins.
     * If a bus is coming in more than 15min then it is green.
     * Between 5 and 15 mins is yellow and <5 is red.
     * @param stopList a non-null list of stops
     */
    public void placeColouredPins(ArrayList<Stop> stopList) {

        /*Place specific coloured dot based on time [WILLIAM MAHER]*/
        /*Calculating schedule Times*/
        for(int i=0;i<stopList.size();i++) {
            int pinResourceID = determineColour(stopList.get(i));

//            Float maxLat = (float) 0.00;
//            Float minLat = (float) 100.00;
//            Float maxLong = (float) -120.00;
//            Float minLong = (float) 0.00;
//            if (stopLat < minLat) {
//                minLat = stopLat;
//            } else if (stopLat > maxLat) {
//                maxLat = stopLat;
//            }
//            if (stopLong < minLong) {
//                minLong = stopLong;
//            } else if (stopLong > maxLong) {
//                maxLong = stopLong;
//            }
            Float stopLat = stopList.get(i).getLatitude();
            Float stopLong = stopList.get(i).getLongitude();
            LatLng stopLatLong = new LatLng(stopLat, stopLong);

            //[END WILLIAM MAHER]

            MainActivity.mainMap.addMarker(new MarkerOptions()
                    .position(stopLatLong)
                    .icon(BitmapDescriptorFactory.fromResource(pinResourceID))
                    .title(stopList.get(i).getStopID().toString()));
        }
    }

    /**
     * Updates the "serverDBCreateTime" which if it doesn't match the
     * updateTime then the LoadStops to update the database
     * and LoadTime class will be executed
     * @param updateDBTime The new update time
     */
    public static void setTimes(String updateDBTime) {
        //first time
        if(lastDBUpdateTime.equals("")) {
            lastDBUpdateTime = updateDBTime;
            serverDBCreateTime = updateDBTime;
        }else{
            serverDBCreateTime = updateDBTime;
        }
        //Log.d("D","serverCBCreateTime "+serverDBCreateTime);

    }

    /**
     * Called from alertDialog controller
     *
     * @param auto the new boolean state
     */
    public static void setAutoUpdate(Boolean auto) {
        autoUpdate = auto;
    }

    /**
     * This method will determine if the user has started the app for the first time
     */
    public AppStart checkAppStart() {
        PackageInfo pInfo;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        AppStart appStart = AppStart.NORMAL;

        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int lastVersion = sharedPreferences.getInt(LAST_APP_VERSION, -1);
            int currentVersion = pInfo.versionCode;
            appStart = checkAppStart(currentVersion, lastVersion);

            // Update the version in preferences
            sharedPreferences.edit().putInt(LAST_APP_VERSION, currentVersion).apply();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appStart;
    }

    /**
     * This is called by the other checkAppStart function and it simply
     * compares the versions sent to it
     */
    public AppStart checkAppStart(int current, int last) {
        if (last == -1)
            return AppStart.FIRST_TIME;
        else if (last < current)
            return AppStart.FIRST_VERSION;
        else
            return AppStart.NORMAL;
    }

    /**
     * Helper function for location button to determine
     * the distance from theLatLng and a stop
     * used in FragmentBotBar and StopBar
     * @param latLngOne
     * @param latLngTwo
     * @return double distance from location and stop (kilometres)
     */
    public double determineDistance(LatLng latLngOne,LatLng latLngTwo){
        double theta;

            theta = latLngOne.longitude - latLngTwo.longitude;

        double distFromLoc;
        distFromLoc=Math.sin(latLngOne.latitude * Math.PI / 180.0) *
                Math.sin(latLngTwo.latitude * Math.PI / 180.0) +
                Math.cos(latLngOne.latitude * Math.PI / 180.0) *
                        Math.cos(latLngTwo.latitude * Math.PI / 180.0) *
                        Math.cos(theta * Math.PI / 180.0);

        distFromLoc = Math.acos(distFromLoc);
        distFromLoc = (distFromLoc * 180.0 / Math.PI);
        //Miles => Km
        distFromLoc = (distFromLoc * 60 * 1.1515) * 1.609344;
        return distFromLoc;
    }

    /**
     * Swap item in the bottom favourites bar
     * @param i
     * @param j
     */
    public static void swapFavList(int i,int j){
        Collections.swap(MainActivity.favoriteList,i,j);
    }

}