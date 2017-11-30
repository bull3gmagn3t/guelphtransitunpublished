package velocityraptor.guelphtransit.main.activityScheduleBus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import velocityraptor.guelphtransit.R;
import velocityraptor.guelphtransit.main.BotBarAdapter;
import velocityraptor.guelphtransit.main.Item;
import velocityraptor.guelphtransit.main.MainActivity;

/**
 * This class defines the Activity which builds a bus/stop list page when "schedules" is pressed.
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
public class ActivityScheduleBus extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private String selectedRoute = "Transit Schedule";
    private int selectedRoutePos = 0;

    /**
     * Set up the list of schedules when the schedules button is pressed
     *
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_listview_sched);

        //Adapter to create bus schedule list
        ArrayList<Item> stopListPairs = new ArrayList<>();
        for(int x=0;x<MainActivity.routeList.size();x++){
            stopListPairs.add(new Item(MainActivity.routeList
                    .get(x)
                    .getRouteName(),
                    MainActivity.
                            routeList.
                            get(x).
                            getRouteNameString(),x));
        }
        BotBarAdapter busListRouteAdapter = new BotBarAdapter(
                this, "list_bus_route", stopListPairs,0,null);

        //Get a reference to the listView
        ListView listView = (ListView) this.findViewById(R.id.list_view_sched);
        listView.setOnItemClickListener(this);
        listView.setAdapter(busListRouteAdapter);
    }

    /**
     * Deals with anything pressed in the "Schedule" main page
     * @param item The selected item from the list, could be the back button
     * @return true on success or super.onOptionsItemSelected(item)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                //Called when a route is selected and the back button in the top left is pressed
                if (!selectedRoute.equals("Transit Schedule")) {
                    selectedRoute = "Transit Schedule";
                    selectedRoutePos = 0;
                    setTitle(selectedRoute);

                    //Adapter to create bus schedule list
                    ArrayList<Item> stopListPairs = new ArrayList<>();
                    for(int x=0;x<MainActivity.routeList.size();x++){
                        stopListPairs.add(new Item(MainActivity.routeList
                                .get(x)
                                .getRouteName(),
                                MainActivity.routeList
                                        .get(x)
                                        .getRouteNameString(),x));
                    }
                    BotBarAdapter busListRouteAdapter = new BotBarAdapter(
                            this, "list_bus_route", stopListPairs,0,null);
                    //Get a reference to the listView
                    ListView listView = (ListView) this.findViewById(R.id.list_view_sched);
                    listView.setOnItemClickListener(this);
                    listView.setAdapter(busListRouteAdapter);
                } else {
                    //Called when the back button is pressed when the Routes list page is open
                    //Goes back to main page
                    NavUtils.navigateUpFromSameTask(this);
                }
                //Returns success, goes back to Routes list page or main page
                return true;
        }
        //Called when the back button in the schedule page is pressed when a
        //route is already selected or when at the Routes list page to go back to main page
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when a Item from the Schedules page is pressed, either a route or a stop itself

     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //show the stops from the selected Route
        if (selectedRoute.equals("Transit Schedule")) {
            TextView txt = (TextView) view.findViewById(R.id.bus_route);
            selectedRoute = txt.getText().toString();
            selectedRoutePos = position;
            setTitle("Route " + MainActivity.routeList.get(selectedRoutePos).getRouteName()
                    + ": " + selectedRoute);

            //Adapter to create bus schedule list
            ArrayList<Item> stopListPairs = new ArrayList<>();
            for(int x=0;x<MainActivity.routeList.get(position).getStopList().size();x++){
                stopListPairs.add(new Item(MainActivity.routeList.get(position)
                        .getStopList()
                        .get(x)
                        .getStopID(),
                        MainActivity.routeList.get(position)
                                .getStopList()
                                .get(x)
                                .getStopName(),x));
            }
            BotBarAdapter busStopListAdapter = new BotBarAdapter(
                    this, "list_schedule_stop", stopListPairs,0,null);

            //Get a reference to the listView
            ListView listView = (ListView) this.findViewById(R.id.list_view_sched);
            listView.setOnItemClickListener(this);
            listView.setAdapter(busStopListAdapter);
        } else {

            //called with a stop on the schedules page is pressed
            if (!MainActivity.routeList.get(selectedRoutePos).getExpress()) {
                //Create an ActivitySchedule class which shows the scheduled
                //times for a specific stop
                Intent intent = new Intent(this, ActivityScheduledTimes.class);
                Bundle extras = new Bundle();
                extras.putInt("EXTRA_STOP_POS", position);
                extras.putInt("EXTRA_ROUTE_POS", selectedRoutePos);
                intent.putExtras(extras);
                startActivity(intent);
            }
        }
    }
}