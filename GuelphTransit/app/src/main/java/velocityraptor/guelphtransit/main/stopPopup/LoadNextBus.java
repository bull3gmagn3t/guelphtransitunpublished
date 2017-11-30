package velocityraptor.guelphtransit.main.stopPopup;

import android.os.AsyncTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import velocityraptor.guelphtransit.main.databaseClasses.JSONParser;

/**
 * This class is for sending a POST request to our server
 * to scrape nextBus times identified by an ID in the database
 * Author: William Aidan Maher on 26/03/15.
 * <p>
 * I have exclusive control over this submission via my password.
 * By including this statement in this header comment, I certify that:
 * 1) I have read and understood the University policy on academic integrity;
 * 2) I have completed the Computing with Integrity Tutorial on Moodle; and
 * I assert that this work is my own. I have appropriately acknowledged any and all material
 * (data, images, ideas or words) that I have used, whether directly quoted or paraphrased.
 * Furthermore, I certify that this assignment was prepared by me specifically for this course.
 */

public class LoadNextBus extends AsyncTask<String, String, String> {
    //NextBus times from server:William Aidan Maher [START]

    private String etaOne = "null",etaTwo = "null";
    private StopPopup stopPopup;
    private CharSequence currentTime;
    private CharSequence schedOne;

    public LoadNextBus(StopPopup s) {
        super();
        stopPopup = s;

    }

    /**
     * This method will run in the background instead of the main thread
     * Will load in the time and store it for the main activity
     * @param args . args[0] = postUrl
     *             args[1] = stopID
     *             args[2] = HH:mm time
     *             args[3] = HH:mm time
     * @return null
     */
    @Override
    protected String doInBackground(String... args) {

        List<NameValuePair> postParams = new ArrayList<NameValuePair>();
        //Get the stopID from list to send to server
        String stopID = args[1];
        currentTime=args[2];
        schedOne=args[3];
        if (stopID.length() == 3) {
            stopID = "0" + stopID;
        }

        postParams.add(new BasicNameValuePair("stopID", stopID.toString()));
        String postURL = args[0];

        JSONParser jParser = new JSONParser();
        JSONObject postResp = new JSONObject();
        //Log.d("D", "making POST request to: " + postURL);
        postResp = jParser.makeHttpRequest(postURL, "POST", postParams);
        if (postResp != null) {
            try {
                etaOne = postResp.getString("ETA1");
                etaTwo = postResp.getString("ETA2");
            } catch (JSONException j) {

            }
        }
        return "";
    }

    /**
     * When JSON request complete, change text in popup*
     * @param s
     * @return null
     */
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute("");
        String scheduleETA="-1";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        int minuteCur=-1,minuteSched=-1;
        minuteCur=Integer.parseInt(currentTime.toString().substring(2));
        minuteSched=Integer.parseInt(schedOne.toString().substring(3));


        if(minuteCur>minuteSched){
            scheduleETA=Integer.toString((60-minuteCur)+minuteSched)+"m.";
        }else if(minuteSched>minuteCur){
            scheduleETA=Integer.toString(minuteSched-minuteCur)+"m.";
        }else{
            scheduleETA="Now.";
        }


        if ((etaOne == "null" && etaTwo == "null")
                || (etaOne == "0" && etaTwo == "0")) {

            etaOne = "NextBus Time unavailable, Scheduled ETA is "+scheduleETA;
            etaTwo = "...";
        }
        try {
            if (Integer.parseInt(etaOne) >= 50) {
                etaTwo = etaOne;
                etaOne = "Nextbus ETA off, Scheduled ETA is "+scheduleETA;
            }
            if (stopPopup != null) {
                stopPopup.setText(etaOne, etaTwo);
            }
        } catch (NumberFormatException e) {
                /*if an exception occurs it usually means the server
                is not running the selenium web scraper
                 */
        } finally {
            etaTwo = etaOne;
            if (stopPopup != null) {
                stopPopup.setText(etaOne, etaTwo);
            }
        }
    }

    //NextBus times from server:William Aidan Maher [END]
}
