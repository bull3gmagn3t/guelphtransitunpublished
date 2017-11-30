package velocityraptor.guelphtransit.main.activityScheduleBus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import velocityraptor.guelphtransit.R;
import velocityraptor.guelphtransit.main.MainActivity;
import velocityraptor.guelphtransit.main.Stop;

/**
 * This class defines the Activity which builds a stops scheduled times page, given
 * the route position and stop position.
 *
 * @author: Nic Durish.
 * @author: Jackson Keenan
 * <p>
 * I have exclusive control over this submission via my password.
 * By including this statement in this header comment, I certify that:
 * 1) I have read and understood the University policy on academic integrity;
 * 2) I have completed the Computing with Integrity Tutorial on Moodle; and
 * I assert that this work is my own. I have appropriately acknowledged any and all material
 * (data, images, ideas or words) that I have used, whether directly quoted or paraphrased.
 * Furthermore, I certify that this assignment was prepared by me specifically for this course.
 */
public class ActivityScheduledTimes extends AppCompatActivity {

    public static GoogleMap stopMap;
    public static CameraUpdate mapUpdate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set reference on content and unpack bundles
        setContentView(R.layout.page_schedule_times);
        Bundle extras = getIntent().getExtras();
        Integer stopPos = extras.getInt("EXTRA_STOP_POS");
        Integer routePos = extras.getInt("EXTRA_ROUTE_POS");
        CharSequence routeNum = MainActivity.routeList.get(routePos).getRouteName();
        String routeName = MainActivity.routeList.get(routePos).getRouteNameString();

        //Retrieve Stop
        Stop stop = MainActivity.routeList.get(routePos).getStopList().get(stopPos);

        //Set Texts for given boxes
        TextView txt = (TextView) findViewById(R.id.route_id);
        txt.setText("Route " + routeNum + ":");
        txt = (TextView) findViewById(R.id.route_name);
        txt.setText(routeName);
        txt = (TextView) findViewById(R.id.stop_id);
        txt.setText("Stop " + stop.getStopID() + ":");
        txt = (TextView) findViewById(R.id.stop_name);
        txt.setText(stop.getStopName());

        //Map Setup [Jackson Keenan]
        stopMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapStop)).getMap();
        mapUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(stop.getLatitude(), stop.getLongitude()), 12);
        stopMap.moveCamera(mapUpdate);
        stopMap.addMarker(new MarkerOptions().position(new LatLng(stop.getLatitude(),
                stop.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker
                (BitmapDescriptorFactory.HUE_YELLOW)).title("NextBus Times Will Be Here"));

        //Adapter to create bus schedule lists
        ArrayAdapter<CharSequence> weekAdapter = new ArrayAdapter<>(this
                , R.layout.textview_times, stop.getWeekTimes());
        ArrayAdapter<CharSequence> satAdapter = new ArrayAdapter<>(this
                , R.layout.textview_times, stop.getSatTimes());
        ArrayAdapter<CharSequence> sunAdapter = new ArrayAdapter<>(this
                , R.layout.textview_times, stop.getSunTimes());

        //Get a reference to the listViews
        ListView listView = (ListView) this.findViewById(R.id.stop_times_week_list);
        listView.setAdapter(weekAdapter);
        listView = (ListView) this.findViewById(R.id.stop_times_sat_list);
        listView.setAdapter(satAdapter);
        listView = (ListView) this.findViewById(R.id.stop_times_sun_list);
        listView.setAdapter(sunAdapter);


        /* This code references code provided by User: Bhavin on May 17 '12 at 10:54
         * , the code can be found at: http://stackoverflow.com/questions/10634231/
         * how-to-display-current-time-that-changes-dynamically-for-every-second-in-android
         * Description: Begin Dynamic Clock Thread */
        Thread myThread;
        Runnable myRunnableThread = new CountDownRunner();
        myThread = new Thread(myRunnableThread);
        myThread.start();
    }

    /**
     * Shows the current time on the individual stop schedule page
     */
    public void doWork() {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView txtCurrentTime = (TextView) findViewById(R.id.clock_current);
                try {
                    Calendar cal = Calendar.getInstance();
                    SimpleDateFormat dateTime = new SimpleDateFormat("HHmm");
                    String curTime = dateTime.format(cal.getTime());
                    txtCurrentTime.setText(curTime);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    if(e instanceof NullPointerException){
                        Log.e("E","Calendar Null pointer ActivityScheduledTimes.java");
                    }else if(e instanceof IllegalArgumentException){
                        Log.e("E","Calendar Illegal Argument ActivityScheduledTimes.java");
                    }
                }finally{
                    txtCurrentTime.setText("Could not get current time.");
                }
            }
        });
    }

    /**
     * Counts the time on the individual stop page with 1 second interrupts
     */
    class CountDownRunner implements Runnable {
        //@Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    doWork();
                    Thread.sleep(1000); // Pause of 1 Second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}

