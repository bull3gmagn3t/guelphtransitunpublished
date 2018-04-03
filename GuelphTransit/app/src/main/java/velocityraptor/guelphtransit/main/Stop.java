package velocityraptor.guelphtransit.main;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by Anthony Mazzawi on 2/23/15.
 * Class Skeleton Created by William (Aidan) Maher
 * <p>
 * This is class for representing the stops on the map/UI
 * which is used in Routes (Routes are composed of stops).
 */
public class Stop {

    private CharSequence stopID, stopName;
    private ArrayList<CharSequence> weekTimes, satTimes, sunTimes;
    private float latitude, longitude;
    public CharSequence routeName;

    //Written by William (Aidan) Maher and Anthony Mazzawi

    /**
     * Constructor with array lists for time
     */
    public Stop(CharSequence stopID, CharSequence stopName, ArrayList<CharSequence> weekTimes, ArrayList<CharSequence> satTimes, ArrayList<CharSequence> sunTimes,
                float latitude, float longitude) {
        this.stopName = stopName;
        this.stopID = stopID;
        this.weekTimes = new ArrayList<>();
        this.satTimes = new ArrayList<>();
        this.sunTimes = new ArrayList<>();

        for (int x=0;x<weekTimes.size();x++) {
            this.weekTimes.add(weekTimes.get(x));
        }
        for (int x=0;x<satTimes.size();x++) {
            this.satTimes.add(weekTimes.get(x));
        }
        for (int x=0;x<satTimes.size();x++) {
            this.sunTimes.add(weekTimes.get(x));
        }

        this.latitude = latitude;
        this.longitude = longitude;

    }

    /**
     * Constructor with strings for times
     */
    public Stop(CharSequence stopID, CharSequence stopName, CharSequence weekTimes, CharSequence satTimes,
                CharSequence sunTimes, float latitude, float longitude,CharSequence route) {
        this.stopID = stopID;
        this.stopName = stopName;
        this.latitude = latitude;
        this.longitude = longitude;

        this.weekTimes = new ArrayList<>();
        this.satTimes = new ArrayList<>();
        this.sunTimes = new ArrayList<>();

        this.routeName=route;
        StringTokenizer st1 = new StringTokenizer(weekTimes.toString(), " ");
        while (st1.hasMoreTokens()) {
            String time = st1.nextToken();
            this.weekTimes.add(time);
        }

        if (satTimes != null) {
            StringTokenizer st2 = new StringTokenizer(satTimes.toString(), " ");
            while (st2.hasMoreTokens()) {
                String time = st2.nextToken();
                this.satTimes.add(time);
            }
        } else
            this.satTimes = null;

        if (sunTimes != null) {
            StringTokenizer st3 = new StringTokenizer(sunTimes.toString(), " ");
            while (st3.hasMoreTokens()) {
                String time = st3.nextToken();
                this.sunTimes.add(time);
            }
        } else
            this.sunTimes = null;
    }
    /*
    Getters and setters, hopefully self documenting
    Written by Anthony and Aidan
    */

    /**
     * Return the Stop's ID
     * @return the Stop's ID
     */
    public CharSequence getStopID() {
        return this.stopID;
    }

    /**
     * Return the Stop's name
     * @return the Stop's name
     */
    public CharSequence getStopName() {
        return this.stopName.toString();
    }

    /**
     * Get the Week Times
     * @return Week Times in an Array List
     */
    public ArrayList<CharSequence> getWeekTimes() {
        return this.weekTimes;
    }

    /**
     * Get the Saturday Times
     * @return Saturday Times in an Array List
     */
    public ArrayList<CharSequence> getSatTimes() {
        return this.satTimes;
    }

    /**
     * Get the  Sunday Times
     * @return Sunday Times in an Array List
     */
    public ArrayList<CharSequence> getSunTimes() {
        return this.sunTimes;
    }

    /**
     * Return the latitude
     * @return The latitude of the bus Stop
     */
    public float getLatitude() {
        return this.latitude;
    }

    /**
     * Return the longitude
     * @return the Stop's longitude
     */
    public float getLongitude() {
        return this.longitude;
    }

    /**
     * Return the stop's Route's name
     * @return String : of the route's name
     */
    public CharSequence getRouteName(){
        return routeName;
    }

    /**
     * Simple equals method
     * @return true or false
     */
    public boolean equals(Stop s){
        return this.getStopID() == s.getStopID();
    }
}
