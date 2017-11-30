package velocityraptor.guelphtransit.main.databaseClasses;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import velocityraptor.guelphtransit.main.AlertDialogController;
import velocityraptor.guelphtransit.main.MainActivity;

/**
 * This class loads the JSON from the url provided via an asynctask which
 * doesn't run on the main thread.  It parses the information and stores
 * it on the phone.
 * Author: Anthony Mazzawi, Aidan Maher
 * <p>
 * I have exclusive control over this submission via my password.
 * By including this statement in this header comment, I certify that:
 * 1) I have read and understood the University policy on academic integrity;
 * 2) I have completed the Computing with Integrity Tutorial on Moodle; and
 * I assert that this work is my own. I have appropriately acknowledged any and all material
 * (data, images, ideas or words) that I have used, whether directly quoted or paraphrased.
 * Furthermore, I certify that this assignment was prepared by me specifically for this course.
 */
public class LoadStops extends AsyncTask<String, String, String> {

    private ProgressDialog pDialog;
    private DBController dbController;
    private AlertDialogController ADC;

    // JSON Variables
    JSONParser jParser = new JSONParser();
    JSONArray stops = null;

    // URL with the JSON Bus information
    //private static final String URL = "http://ec2-54-218-117-134.us-west-2.compute.amazonaws.com/androidSqlConnect/getRoutes.php";
    private static final String phpFile = "getRoutes.php";

    //JSON node names, case sensitive
    private static final String TAG_SUCCESS = "success";

    //JSON array names, case sensitive
    private static final String TAG_ROUTES = "Routes";
    private static final String TAG_TIME = "Time";


    //Json value names, case sensitive
    private static final String TAG_ROUTE = "route";
    private static final String TAG_STOPID = "stopID";
    private static final String TAG_STOPNAME = "stopName";
    private static final String TAG_LATITUDE = "latitude";
    private static final String TAG_LONGITUDE = "longitude";
    //private static final String TAG_TIMELIST = "timeList";
    private static final String TAG_WEEKTIME = "weekTimes";
    private static final String TAG_SATTIME ="satTimes";
    private static final String TAG_SUNTIME ="sunTimes";


    /**
     * Constructor which creates a popup dialog and construct two objects
     * to store the information and create a new alertdialog
     *
     * @param context Context currently active
     */
    public LoadStops(Context context) {
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Loading all stops... Please wait");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);

        this.dbController = new DBController(context);
        this.ADC = new AlertDialogController(context);
    }

    /**
     * This method is executed directly before doInBackground
     * It will show the dialog created in the constructor
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pDialog.show();
    }

    /**
     * This method will load the JSON Object from the url and parse it into the
     * sqLite db that's on the phone.  Will also store the time of the server db
     * onto main activity.
     *
     * @param args .
     * @return null
     */
    @Override
    protected String doInBackground(String... args) {
        // Building Parameters
        List<NameValuePair> params = new ArrayList<>();
        String URL = MainActivity.serverURL+phpFile;
        // Getting JSON String From URL
        JSONObject json = jParser.makeHttpRequest(URL, "GET", params);

        try {
            // Check for success tag
            int success = json.getInt(TAG_SUCCESS);

            //Stops found, get array of stops
            if (success == 1) {
                // Get the different JSON Arrays from the Object
                stops = json.getJSONArray(TAG_ROUTES);

                // Temp variables
                String route, stopID, stopName;
                String latitude, longitude, updateTime;
                String weekTimes,satTimes,sunTimes;


                //loop through every stop
                for (int i = 0; i < stops.length(); i++) {
                    JSONObject c = stops.getJSONObject(i);

                    //Store each item into a variable
                    route = c.getString(TAG_ROUTE);
                    stopID = c.getString(TAG_STOPID);
                    stopName = c.getString(TAG_STOPNAME);
                    latitude = c.getString(TAG_LATITUDE);
                    longitude = c.getString(TAG_LONGITUDE);
                    weekTimes = c.getString(TAG_WEEKTIME);
                    satTimes = c.getString(TAG_SATTIME);
                    sunTimes = c.getString(TAG_SUNTIME);

                    // Insert into the database

                    dbController.insert(route, stopID, stopName, latitude, longitude,
                            weekTimes, satTimes, sunTimes);
//                    try{
//                        dbController.insert(route, stopID, stopName, latitude, longitude,
//                                weekTimes, satTimes, sunTimes);
//                    }catch(Exception e){
//                        if(e instanceof android.database.sqlite.SQLiteConstraintException){
//                            Log.d("D","Ignoring already added stop "+stopID+".");
//                        }
//                    }

                }
                // Update the time
                //JSONObject c = time.getJSONObject(0);
                //updateTime = c.getString(TAG_UPDATE);

                /* Since this class is only used when the database is updated
                   that means the db is up to date and both times will be the same */
                //MainActivity.setTimes(updateTime, updateTime);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method runs immediately after doInBackground and will
     * dismiss the dialog currently open and will open a new dialog.
     *
     * @param unused .
     */
    @Override
    protected void onPostExecute(String unused) {
        pDialog.dismiss();
        ADC.updateDialog(false);
    }
}