/*Route.java
@author: Aidan Maher, Anthony Mazzawi, Nic Durish and Jackson Keenan
Skeleton originally made by William (Aidan) Maher
Anthony, Nick and Aidan worked overall on the file
* This is the class representing routes, composed of stops
* which is pulled and populated from our database.
*/

package velocityraptor.guelphtransit.main;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Class for Bus Routes
 */
public class Route {
    private CharSequence routeName;
    private CharSequence routeNameString;
    private ArrayList<Stop> stopList;
//    Stop = {String stopID, stopName
//    ArrayList<String> weekTimes, satTimes, sunTimes
//    float latitude, longitude}
    private boolean isExpress;

    /**
     * Constructors
     */
    public Route(String routeName, boolean exp) {
        this.routeName = routeName;
        this.isExpress = exp;
        generateRouteNameString();
        this.stopList = new ArrayList<>();
    }

    public Route(String routeName, ArrayList<Stop> stopList, boolean exp) {
        this.routeName = routeName;
        generateRouteNameString();
        this.isExpress = exp;
        this.stopList = new ArrayList<>();
        this.isExpress = false;
        for (Stop s : stopList) {
            this.stopList.add(new Stop(s.getStopName(), s.getStopID(),
                    s.getWeekTimes(), s.getSatTimes(), s.getSunTimes(),
                    s.getLatitude(), s.getLongitude()));
        }
    }

    /**
     * Setters
     */
    public void setRouteName(String routeName) {
        this.routeName = routeName;
        generateRouteNameString();
    }

    public void setRouteNameString(String routeNameString) {
        this.routeNameString = routeNameString;
    }

    public void setIsExpress(boolean exp) {
        this.isExpress = exp;
    }

    /**
     * Getters
     */

    public CharSequence getRouteName() {
        return this.routeName;
    }

    public String getRouteNameString() {
        return this.routeNameString.toString();
    }

    public ArrayList<Stop> getStopList() {
        return this.stopList;
    }

    public boolean getExpress() {
        return this.isExpress;
    }

    /**
     * Extra Methods
     */
    //Generate routeNameString based on preloaded strings for routeName
    public void generateRouteNameString() {
        switch (routeName.toString()) {
            case "1A":
                this.routeNameString = "College Edinburgh";
                break;
            case "1B":
                this.routeNameString = "College Edinburgh";
                break;
            case "2A":
                this.routeNameString = "West Loop";
                break;
            case "2B":
                this.routeNameString = "West Loop";
                break;
            case "3A":
                this.routeNameString = "East Loop";
                break;
            case "3B":
                this.routeNameString = "East Loop";
                break;
            case "4":
                this.routeNameString = "York";
                break;
            case "5":
                this.routeNameString = "Gordon";
                break;
            case "6":
                this.routeNameString = "Harvard Ironwood";
                break;
            case "7":
                this.routeNameString = "Kortright Downey";
                break;
            case "8":
                this.routeNameString = "Stone Road Mall";
                break;
            case "9":
                this.routeNameString = "Waterloo";
                break;
            case "10":
                this.routeNameString = "Imperial";
                break;
            case "11":
                this.routeNameString = "Willow West";
                break;
            case "12":
                this.routeNameString = "General Hospital";
                break;
            case "13":
                this.routeNameString = "Victoria Rd Rec Centre";
                break;
            case "14":
                this.routeNameString = "Grange";
                break;
            case "15":
                this.routeNameString = "University College";
                break;
            case "16":
                this.routeNameString = "Southgate";
                break;
            case "20":
                this.routeNameString = "NorthWest Industrial";
                break;
//            case "50":
//                this.routeNameString = "Stone Road Express";
//                break;
//            case "56":
//                this.routeNameString = "Victoria Express";
//                break;
//            case "57":
//                this.routeNameString = "Harvard Express";
//                break;
//            case "58":
//                this.routeNameString = "Edinburgh Express";
//                break;
            default:
                this.routeNameString = "Unknown Route";
                break;
        }
    }

    /**
     * Add a stop to the list when an object is given.
     *
     * @param s Stop object
     */
    public void addToStopList(Stop s) {
        this.stopList.add(s);
    }

    /**
     * Compare one route to another by route name (ie. 1A vs 5)
     * So that the lists are inserted in order
     */
    public static Comparator<Route> routeSort = new Comparator<Route>() {
        @Override
        public int compare(Route lhs, Route rhs) {
            boolean lhsLetter = false;
            boolean rhsLetter = false;

            /* The routes that contain a letter are only 1,2 and 3 which means
            they will be at the front of the list.  If a route contains a letter
            it is automatically in front and will only be compared to another route
            if the other one contains a letter as well */
            if (lhs.getRouteName().toString().contains("A") || lhs.getRouteName().toString().contains("B"))
                lhsLetter = true;

            if (rhs.getRouteName().toString().contains("A") || rhs.getRouteName().toString().contains("B"))
                rhsLetter = true;

            /* The first two options are if one route has a letter and the other doesn't */
            if (lhsLetter && !rhsLetter) {
                return -1;
            } else if (!lhsLetter && rhsLetter) {
                return 1;
            /* If both routes have letters they will be compared */
            } else if (lhsLetter) {
                char one, two;
                one = lhs.getRouteName().charAt(0);
                two = rhs.getRouteName().charAt(0);

                if (one < two) {
                    return -1;
                } else if (one > two) {
                    return 1;
                } else if (one == two) {
                    char three, four;
                    three = lhs.getRouteName().charAt(1);
                    four = rhs.getRouteName().charAt(1);
                    if (three < four) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            /* If both routes do not have letters they can be compared as ints */
            } else {
                int lhsInt, rhsInt;
                lhsInt = Integer.parseInt(lhs.getRouteName().toString());
                rhsInt = Integer.parseInt(rhs.getRouteName().toString());
                return lhsInt - rhsInt;
            }

            return 0;
        }
    };
}
