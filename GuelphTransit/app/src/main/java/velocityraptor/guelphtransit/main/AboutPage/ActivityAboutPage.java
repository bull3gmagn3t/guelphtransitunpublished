package velocityraptor.guelphtransit.main.AboutPage;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import velocityraptor.guelphtransit.R;

/**
 * Opens the About page and sets the image
 * Created by Nic Durish on 15-04-14.
 * * I have exclusive control over this submission via my password.
 * By including this statement in this header comment, I certify that:
 * 1) I have read and understood the University policy on academic integrity;
 * 2) I have completed the Computing with Integrity Tutorial on Moodle; and
 * I assert that this work is my own. I have appropriately acknowledged any and all material
 * (data, images, ideas or words) that I have used, whether directly quoted or paraphrased.
 * Furthermore, I certify that this assignment was prepared by me specifically for this course.
 */

public class ActivityAboutPage extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_about);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}

