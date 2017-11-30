package velocityraptor.guelphtransit.main.stopPopup;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import velocityraptor.guelphtransit.R;
import velocityraptor.guelphtransit.main.MainActivity;
import velocityraptor.guelphtransit.main.Stop;


/**
 * Created by William Maher on 27/03/15.
 * Most code inside is originally from Jackson
 * But variable declarations and last method (setText) made by William Aidan Maher
 * Which was refactored here since the popup is called by 2 different listeners
 *
 * @author William Maher
 */
public class StopPopup extends PopupWindow {

    private TextView popupTimes;
    private TextView popUpText;
    private CharSequence schedOne = "";
    private CharSequence schedTwo = "";
    private CharSequence schedThree = "";
    private CharSequence phpFile="getNextBus.php";

    //The selected stop
    private Stop stop;
    //PopUpWindow: Jackson Keenan [START]

    /**
     * Creates a popup with information about the bus stop on the main screen
     *
     * @param popupView  the View
     * @param height     Size of popup
     * @param width      Size of popup
     * @param routePosIn
     * @param positionIn
     */
    public StopPopup(View popupView, int height, int width, int routePosIn, int positionIn,String URL, int fragMode) {
        //Aidan Maher [Start]
        //Pass to superclass, necessary vars to instantiate correctly
        super(popupView, width, height);
        //this block is needed to get the day of the week and the current time
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat tempDay = new SimpleDateFormat("EE");
        String dayOfWeek = tempDay.format(cal.getTime());
        SimpleDateFormat dateTime = new SimpleDateFormat("HHmm");
        String currentTime = dateTime.format(cal.getTime());

        switch(fragMode) {
            case 0:
                stop=MainActivity.routeList.get(routePosIn).getStopList().get(positionIn);
                break;
            case 1:
                stop=MainActivity.favoriteList.get(positionIn);
                break;
            case 2:
                //stop=MainActivity.locationList.get(positionIn);
                stop=MainActivity.routeList.get(routePosIn).getStopList().get(positionIn);

                break;
        }


        //Aidan Maher[End]
        //PopUpWindow: Jackson Keenan [START]

        CharSequence listIterator;
        ArrayList<CharSequence> timeList;

        int stopInt = 0;
        ImageButton clsPopUp = (ImageButton) popupView.findViewById(R.id.dismiss);
        ImageButton favStop = (ImageButton)popupView.findViewById(R.id.fav);

        /*Calculating schedule Times*/
        switch (dayOfWeek) {
            case "Sat":
                timeList = stop.getSatTimes();
                break;
            case "Sun":
                timeList = stop.getSunTimes();
                break;
            default:
                timeList = stop.getWeekTimes();
                break;
        }

        for (int j = 0; j < timeList.size(); j++) {
            listIterator = timeList.get(j);
            listIterator = listIterator.toString().replace(":", "");
            if (Integer.parseInt(currentTime) < Integer.parseInt(listIterator.toString())) {
                stopInt = j;
                break;
            }
        }
        /*Setting Schedule times*/
        schedOne = timeList.get(stopInt);
        if (stopInt <= timeList.size() - 2) {
            schedTwo = timeList.get(stopInt + 1);
        } else {
            schedTwo = "- -";
        }
        if (stopInt <= timeList.size() - 3) {
            schedThree = timeList.get(stopInt + 2);
        } else {
            schedThree = "- -";
        }

        /*Populate Popup with default text*/
        popUpText = (TextView) popupView.findViewById(R.id.popUpTextViewTitle);
        popupTimes = (TextView) popupView.findViewById(
                R.id.popUpTextViewTimes);
//        popUpFirstOtherTimes = (TextView) popupView.findViewById(
//                R.id.popUpTextViewTimes);
        CharSequence popUpRouteTitle = stop.getRouteName();
        CharSequence popUpStopTitle = stop.getStopName() + "\nStop ID:"+ stop.getStopID();

        /*Setting Text In popUp*/
        popUpText.setText(" " + popUpRouteTitle + ": " + popUpStopTitle);
        popupTimes.setText(" " + schedOne + "\t\t\t\t--Loading Eta\n"
                + schedTwo + "\t\t\t\t--Loading Eta\n" +
                "  " + schedThree);
        //popUpFirstOtherTimes.setText("  " + schedTwo + "\t\t\t\t--Loading Eta\n  " + schedThree);


        /*Display PopUp*/
        //showAsDropDown(popupView, 10, 250);
        //Execute the async task
        LoadNextBus l = new LoadNextBus(this);
        //Re-add ":"
        currentTime=currentTime.substring(0,1)+":"+currentTime.substring(3,4);
        l.execute(URL+phpFile,stop.getStopID().toString(),currentTime,schedOne.toString());


        /*Closing PopUp*/
        clsPopUp.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        /*Favourite the stop*/
        favStop.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                boolean repeatFav=false;
                for (int i = 0; i < MainActivity.favoriteList.size(); i++) {
                    if (MainActivity.favoriteList.get(i).getStopID().equals(stop.getStopID())) {
                        Toast.makeText(v.getContext(), "Stop " + stop.getStopID()
                                + " is already in Favorites.", Toast.LENGTH_SHORT).show();
                        repeatFav = true;
                    }
                }
                if (!repeatFav) {
                    MainActivity.favoriteList.add(0, stop);
                    Toast.makeText(v.getContext().getApplicationContext(), "Stop " + stop.getStopID()
                            + " has been added to Favorites.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /*PopUp: Movement*/
        popupView.setOnTouchListener(new OnTouchListener() {
            int strX, strY, offX, offY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        strX = (int) event.getX();
                        strY = (int) event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        offX = (int) event.getRawX() - strX;
                        offY = (int) event.getRawY() - strY;
                        update(offX, offY, -1, -1, true);
                        break;
                }
                return true;
            }
        });
        //PopUpWindow: Jackson Keenan [END]

    }

    /**
     * Setting Text In popUp When scraping is finished
     */
    public void setText(String eta1, String eta2) {
        if (eta1.contains("NextBus Time unavailable") && eta2.contains("NextBus Time unavailable")) {
            popupTimes.setText(schedOne + "\t" + eta1 +"\n"
                + schedTwo + "\t" + eta2 + "\n" + schedThree);
            //popUpFirstOtherTimes.setText("  " + schedTwo + "\t\t" + eta2 + "\n  " + schedThree);
        } else {
            popupTimes.setText(" " + schedOne + "\t\t\t\t1st GPS ETA @ this stop: " + eta1 +"\n "
                    + schedTwo + "\t\t\t\t2nd GPS ETA @ this stop:  " + eta2 + "\n  " + schedThree);
            //popUpFirstOtherTimes.setText("  " + schedTwo + "\t\t\t\t2nd GPS ETA @ this stop:  " + eta2 + "\n  " + schedThree);
        }

    }


}
