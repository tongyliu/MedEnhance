package com.mhacks.tong.medchance;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;

import java.net.MalformedURLException;
import java.util.List;


public class MainActivity extends Activity {
    public final static String TYPE = "com.mhacks.tong.medchance.TYPE";
    public final static String CHECK_TYPE = "com.mhacks.tong.medchance.CHECK";
    public final static String DATA_ENTRY_TYPE = "com.mhacks.tong.medchance.AZURE";

    private MobileServiceClient mClient;
    private int numDataEntries = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        try {
            mClient = new MobileServiceClient(
                    "https://medchance.azure-mobile.net/",
                    "mCRgxbCwdLQFcUngpeGOcCyHMuWwmS28",
                    this
            );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        mClient.getTable(DiseaseItem.class).execute(new TableQueryCallback<DiseaseItem>() {
            public void onCompleted(List<DiseaseItem> result, int count,
                                    Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    /*for (DiseaseItem item : result){
                        System.out.print(item.Disease + ", " + item.Symptoms + "\n");
                    }*/
                    numDataEntries = result.size();
                }
            }
        });
    }

    public void startDataEntry(View view) {
        Intent dataIntent = new Intent(this, DiseaseSelectionActivity.class);
        dataIntent.putExtra(TYPE, DATA_ENTRY_TYPE);
        startActivity(dataIntent);
    }

    public void startCheckType(View view) {
        final Intent checkIntent = new Intent(this, DiseaseSelectionActivity.class);
        checkIntent.putExtra(TYPE, CHECK_TYPE);
        if (numDataEntries > 200) {
            startActivity(checkIntent);
        }
        else {
            new AlertDialog.Builder(this)
                    .setTitle("Notice")
                    .setMessage("Our application makes calculations based on user reported data.\n\n" +
                            "Because our application does not yet have a statistically sufficient amount of users, " +
                            "we advise users to be wary of our estimates, as they are likely inaccurate.\n\n" +
                            "We also encourage users to report their own data " +
                            "under \"Contribute Your Data!\" in order to improve future estimates.\n\n" +
                            "Do you still wish to start a calculation?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(checkIntent);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
