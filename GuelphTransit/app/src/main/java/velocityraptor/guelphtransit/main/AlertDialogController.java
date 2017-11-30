package velocityraptor.guelphtransit.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import velocityraptor.guelphtransit.R;

/**
 * This class creates AlertDialogs (popup with text) to notify the user if their
 * schedules are out of date
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
public class AlertDialogController {

    private Context context;

    public AlertDialogController(Context context) {
        this.context = context;
    }

    /**
     * User will be able to select if they want auto-updates or not (Anthony)
     */
    public void autoUpdateDialog(Boolean autoUpdate) {
        int start;
        final Boolean[] temp = new Boolean[1];

        /* When the user opens this setting, the dialog will already
        have ON or OFF toggled depending on the state of autoUpdate  */
        if (autoUpdate) {
            start = 1;
        }else {
            start = 0;
        }

        //Two options the user has
        final CharSequence[] items = {"OFF", "ON"};

        AlertDialog.Builder ADB = new AlertDialog.Builder(this.context);
        ADB.setTitle(R.string.title_auto_update);
        ADB.setCancelable(false);
        ADB.setSingleChoiceItems(items, start, null);
        ADB.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();

                /* For some reason boolean doesn't work but the array does
                There probably is a better way to do this but for now this is the way that works */
                temp[0] = (selectedPosition != 0);

                MainActivity.setAutoUpdate(temp[0]);
                dialog.dismiss();
            }
        });
        ADB.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ADB.show();
    }

    /**
     * Popup dialog to tell the user that the phone database is up to date with the server
     *
     * @param a If true, the db is already up to date.  If false the db is now up to date
     */
    public void updateDialog(final Boolean a) {
        final AlertDialog.Builder ADB = new AlertDialog.Builder(context);

        //Boolean determines the two differences in the title
        if (a) {
            ADB.setTitle("Schedules are up to date.");
        }else {
            ADB.setTitle("Schedules are now up to date.");
        }
        ADB.setCancelable(false);
        ADB.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                /* Since the new schedules were just downloaded, the
                   activity needs to be restarted  */
                if (!a) {
                    Intent intent = new Intent(context, MainActivity.class);
                    ((Activity) context).finish();
                    context.startActivity(intent);

                }
            }
        });
        ADB.show();
    }
}
