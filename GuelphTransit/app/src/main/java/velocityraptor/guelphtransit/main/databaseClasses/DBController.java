package velocityraptor.guelphtransit.main.databaseClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import velocityraptor.guelphtransit.main.Route;
import velocityraptor.guelphtransit.main.Stop;

/**
 * DBController controls the database by selecting and inserting information
 * with the SQLite database on the android device
 * <p>
 * Examples taken from androidhive.info
 * Author: Anthony Mazzawi
 * <p>
 * I have exclusive control over this submission via my password.
 * By including this statement in this header comment, I certify that:
 * 1) I have read and understood the University policy on academic integrity;
 * 2) I have completed the Computing with Integrity Tutorial on Moodle; and
 * I assert that this work is my own. I have appropriately acknowledged any and all material
 * (data, images, ideas or words) that I have used, whether directly quoted or paraphrased.
 * Furthermore, I certify that this assignment was prepared by me specifically for this course.
 */
public class DBController extends SQLiteOpenHelper {

    // Database Version
    private static final int DB_VERSION = 1;

    // Database Name
    private static final String DB_NAME = "guelphtransitdb";

    // Table Name
    private static final String TABLE_NAME = "Routes";

    // Column Names
    private static final String KEY_ROUTE = "route";
    private static final String KEY_STOPID = "stopID";
    private static final String KEY_STOPNAME = "stopName";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_WEEKTIMES = "weekTimes";
    private static final String KEY_SATTIMES = "satTimes";
    private static final String KEY_SUNTIMES = "sunTimes";

    // Table Create Statement
    private static final String CREATE_TABLE_STOPS = "CREATE TABLE "
            + TABLE_NAME + "(" + KEY_ROUTE + " varchar(100), "
            + KEY_STOPID + " varchar(5) PRIMARY KEY, " + KEY_STOPNAME
            + " varchar(100), " + KEY_LATITUDE + " float, " + KEY_LONGITUDE
            + " float, " + KEY_WEEKTIMES + " varchar(1000), " + KEY_SATTIMES
            + " varchar(100), " + KEY_SUNTIMES + " varchar(1000) " + ")";

    // Count Statement
    private static final String SELECT_COUNT = "SELECT count(*) FROM " + TABLE_NAME;

    // Select Statements
    private static final String SELECT_ALL = "SELECT * FROM " + TABLE_NAME;
    private static final String SELECT_ROUTE = "SELECT " + KEY_ROUTE + " FROM " + TABLE_NAME;


    public DBController(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Creates the table
     *
     * @param db The database to be created
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create required table
        db.execSQL(CREATE_TABLE_STOPS);
    }

    /**
     * Upgrades the current tables
     *
     * @param db         Database where the table is stored
     * @param oldVersion The old version of the database
     * @param newVersion The new version of the database
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // On upgrade drop older tables
        Log.e("D","DROPPING TABLES");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // create new tables
        onCreate(db);
    }

    /**
     *
     * Insert a single stop into the database
     */

    public void insert(String route, String stopID, String stopName, String latitude,
                       String longitude, String weekTimes, String satTimes,
                       String sunTimes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ROUTE, route);
        values.put(KEY_STOPID, stopID);
        values.put(KEY_STOPNAME, stopName);
        values.put(KEY_LATITUDE, latitude);
        values.put(KEY_LONGITUDE, longitude);
        values.put(KEY_WEEKTIMES, weekTimes);
        values.put(KEY_SATTIMES, satTimes);
        values.put(KEY_SUNTIMES, sunTimes);

        //Log.d("D",values.toString());
        db.insertWithOnConflict(TABLE_NAME, null, values,0);

        db.close();
    }

    /* Determine how many entries are in the SQLite table */
    public int getCount() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.rawQuery(SELECT_COUNT, null);
        c.moveToFirst();
        db.close();
        return c.getInt(0);
    }

    /**
     * Insert routes from database into routeList
     * @param routeList
     */
    public void insertRoutes(ArrayList<Route> routeList) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(SELECT_ROUTE, null);

        //Loop through all the entries
        if (c.moveToFirst()) {
            do {

                String route = c.getString(c.getColumnIndex(KEY_ROUTE));
                boolean express = false;
                // Iterate through the route list to determine if the
                // route already exists

                boolean noRoute = true;
                for (int i = 0; i < routeList.size(); i++) {
                    if (routeList.get(i).getRouteName().equals(route))
                        noRoute = false;
                }
                // If the route is not in the list, it is added
                if (noRoute) {
                    if (route.equals("50") || route.equals("56") || route.equals("57") || route.equals("58"))
                        express = true;
                    Route r = new Route(route, express);
                    routeList.add(r);
                }
            } while (c.moveToNext());
        }
        Collections.sort(routeList, new Comparator<Route>() {
            @Override
            public int compare(Route o1, Route o2) {
                if (o1.getStopList().size() != 0 && o2.getStopList().size() != 0) {
//                    Log.d("D","Comparing "+o1.getStopList().get(0).getStopID().toString()+" and "+
//                            o2.getStopList().get(0).getStopID().toString());
                    if (Integer.parseInt(o1.getStopList().get(0).getStopID().toString()) >
                            Integer.parseInt(o2.getStopList().get(0).getStopID().toString())) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else {
                    return 0;
                }
            }
        });

    }

    /**
     * Insert every stop in the array list of routes into the database
     */
    public void insertAllStops(ArrayList<Route> routeList) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(SELECT_ALL, null);

        //Loop through all the rows
        if (c.moveToFirst()) {
            do {
                CharSequence route = c.getString(c.getColumnIndex(KEY_ROUTE));
                CharSequence stopID = c.getString(c.getColumnIndex(KEY_STOPID));
                CharSequence stopName = c.getString(c.getColumnIndex(KEY_STOPNAME));
                Float latitude = c.getFloat(c.getColumnIndex(KEY_LATITUDE));
                Float longitude = c.getFloat(c.getColumnIndex(KEY_LONGITUDE));
                CharSequence weekTimes = c.getString(c.getColumnIndex(KEY_WEEKTIMES));
                CharSequence satTimes = c.getString(c.getColumnIndex(KEY_SATTIMES));
                CharSequence sunTimes = c.getString(c.getColumnIndex(KEY_SUNTIMES));

                Stop s = new Stop(stopID, stopName, weekTimes, satTimes,
                        sunTimes, latitude, longitude, route);

                int routeIndex = 0;
                for (int i = 0; i < routeList.size(); i++) {
                    if (routeList.get(i).getRouteName().equals(route)) {
                        routeIndex = i;
                    }
                }
                routeList.get(routeIndex).addToStopList(s);

            } while (c.moveToNext());
        }
    }

    /**
     * Delete everything from the table
     */
    public void deleteDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

    /**
     * Retrieves the Stop object associated with the stopID
     * @param id the bus stop's stopID
     * @return The Stop object :
     * Bus{String stopID, stopName,
     * ArrayList<String> weekTimes, satTimes, sunTimes,
     * float latitude, longitude}
     *
     */
    public Stop getStop(CharSequence id) {
        SQLiteDatabase db = getReadableDatabase();


        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE stopID = ?", new String[]{id + ""});
        String stopId = "", stopName = "", routeName="";
        Float latitude = null, longitude = null;
        String weekTimes = "", satTimes = "", sunTimes = "";


        if (cursor.getCount() > 0) {

            cursor.moveToFirst();
            stopId = cursor.getString(cursor.getColumnIndex(KEY_STOPID));
            stopName = cursor.getString(cursor.getColumnIndex(KEY_STOPNAME));
            latitude = cursor.getFloat(cursor.getColumnIndex(KEY_LATITUDE));
            longitude = cursor.getFloat(cursor.getColumnIndex(KEY_LONGITUDE));
            weekTimes = cursor.getString(cursor.getColumnIndex(KEY_WEEKTIMES));
            satTimes = cursor.getString(cursor.getColumnIndex(KEY_SATTIMES));
            sunTimes = cursor.getString(cursor.getColumnIndex(KEY_SUNTIMES));
            routeName = cursor.getString(cursor.getColumnIndex(KEY_ROUTE));

        }
        cursor.close();

        return new Stop(stopId, stopName, weekTimes, satTimes, sunTimes, latitude, longitude, routeName);


    }

}
