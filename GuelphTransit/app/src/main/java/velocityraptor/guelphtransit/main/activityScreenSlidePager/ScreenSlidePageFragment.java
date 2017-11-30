package velocityraptor.guelphtransit.main.activityScreenSlidePager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import velocityraptor.guelphtransit.R;

/**
 * The individual fragment which shows the help page images, or images defined in a
 * FragmentActivity's filedescriptor ex R.drawable.id (direct int filepath descriptor)
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
public final class ScreenSlidePageFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Sets the image as defined in xml and gets the proper image from this class's bundle
     * sent on construction or newInstance
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_help_slide_page, container, false);

        ImageView image = (ImageView) v.findViewById(R.id.help_pager_image);
        //Set the image based on the int from the Bundle sent
        image.setImageResource(getArguments().getInt("msg"));

        return v;
    }

    /**
     * When a new instance is created the image is set
     * @param i the current picture frame to define limits for swiping
     * @return the fragment to be set in the ActivityScreenSlidePager
     */
    public static ScreenSlidePageFragment newInstance(int i) {
        ScreenSlidePageFragment f = new ScreenSlidePageFragment();
        Bundle b = new Bundle();
        b.putInt("msg", i);
        f.setArguments(b);
        return f;
    }

}
