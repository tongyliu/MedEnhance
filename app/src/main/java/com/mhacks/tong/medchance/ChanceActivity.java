package com.mhacks.tong.medchance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableOperationCallback;
import com.microsoft.windowsazure.mobileservices.TableQueryCallback;

import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ChanceActivity extends Activity {

    private MobileServiceClient mClient;
    MobileServiceTable<DiseaseItem> dataTable;
    private int numDataEntries;
    private int numDiseaseOccurrences;
    private Hashtable<String, Integer> numSymOccurrences = new Hashtable<String, Integer>();
    private int numCallbacks = 0;

    private ArrayList<String> symptomsArrayList;
    private String currDisease;

    private double probDisease;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chance);

        Intent callingIntent = getIntent();
        currDisease = callingIntent.getStringExtra(DiseaseSelectionActivity.SELECTED_DISEASE_MSG);
        symptomsArrayList = callingIntent.getStringArrayListExtra(SymptomsActivity.SELECTED_SYMPTOMS_MSG);
        if (callingIntent.getStringExtra(MainActivity.TYPE).equals(MainActivity.DATA_ENTRY_TYPE)) {
            TextView percentTextView = (TextView) findViewById(R.id.percentView);
            percentTextView.setText("");

            // Reset number of callbacks
            numCallbacks = 0;
            // Add data to Azure database
            try {
                mClient = new MobileServiceClient(
                        "https://medchance.azure-mobile.net/",
                        "mCRgxbCwdLQFcUngpeGOcCyHMuWwmS28",
                        this
                );

                dataTable = mClient.getTable(DiseaseItem.class);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            DiseaseItem item = new DiseaseItem();
            item.Disease = currDisease;
            String symptomsString = "";

            for (String s : symptomsArrayList)
            {
                symptomsString += s + ", ";
            }
            item.Symptoms = symptomsString;
            System.out.println(symptomsString);
            final TextView resultTextView = (TextView) findViewById(R.id.myTextField);

            dataTable.insert(item, new TableOperationCallback<DiseaseItem>() {
                public void onCompleted(DiseaseItem entity, Exception exception, ServiceFilterResponse response) {
                    if (exception == null) {
                        System.out.println("insert succeeded");
                        resultTextView.setText(getString(R.string.data_received));
                    } else {
                        System.out.println("insert failed");
                        exception.printStackTrace();
                        resultTextView.setText("Server upload failed. Please try again.");
                    }
                }
            });
        }
        else {
            getDatabaseProb();
            TextView resultTextView = (TextView) findViewById(R.id.myTextField);
            resultTextView.setText(getString(R.string.results_page_disclaimer));
        }

    }

    protected void getDatabaseProb() {
        try {
            mClient = new MobileServiceClient(
                    "https://medchance.azure-mobile.net/",
                    "mCRgxbCwdLQFcUngpeGOcCyHMuWwmS28",
                    this
            );

            dataTable = mClient.getTable(DiseaseItem.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // Get the total population
        dataTable.execute(new TableQueryCallback<DiseaseItem>() {
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

        // Get number of times the disease occurs in the population
        dataTable.where().field("Disease").eq(currDisease).execute(new TableQueryCallback<DiseaseItem>() {
            public void onCompleted(List<DiseaseItem> result, int count,
                                    Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    numDiseaseOccurrences = result.size();
                    System.out.println("num of diseaes occurences: " + numDiseaseOccurrences);
                }
            }
        });

        // Get number of time each symptom occurs in the population
        for (final String sym : symptomsArrayList) {
            dataTable.where().indexOf("Symptoms", sym).ne(-1)
                    .execute(new TableQueryCallback<DiseaseItem>() {
                        public void onCompleted(List<DiseaseItem> result, int count,
                                                Exception exception, ServiceFilterResponse response) {
                    if (exception == null) {
                        /*for (DiseaseItem item : result) {
                            System.out.print(item.Disease + ", " + item.Symptoms + "\n");
                        }*/
                        numSymOccurrences.put(sym, result.size());
                        numCallbacks++;
                        System.out.println("Added (" + sym + ", " + result.size() + ") to the hash");
                        if (numCallbacks == symptomsArrayList.size()) {
                            getFinalProb();
                        }
                    }
                }
            });
        }
    }

    private void getFinalProb() {
        ArrayList<Double> symptomProbs = new ArrayList<Double>();
        for (Integer num : numSymOccurrences.values()) {
            symptomProbs.add((double) num.intValue() / numDataEntries);
        }
        Double[] probDubArr = new Double[symptomProbs.size()];
        probDubArr = symptomProbs.toArray(probDubArr);

        TextView percentTextView = (TextView) findViewById(R.id.percentView);
        double denom = calculateDenom(probDubArr);
        System.out.println("denom: " + calculateDenom(probDubArr));
        if (denom < (Math.pow(10, -8)) || Double.isNaN(denom)) {
            percentTextView.setText("Not enough data for this calculation.");
        }
        else {
            DecimalFormat df = new DecimalFormat("##.#");
            probDisease = ((double) numDiseaseOccurrences / numDataEntries) * sumProbSymptoms(probDubArr) / denom;
            percentTextView.setText("There is approximately a " + df.format(probDisease * 100) +
                                    "% chance of having " + currDisease + " given your selected symptoms.");
        }
    }

    private double sumProbSymptoms(Double[] prob) {
        double sum = 0;
        for (Double d : prob) {
            sum += d;
        }
        return sum;
    }

    private double calculateDenom(Double[] prob) {
        if (prob.length == 1) {
            return prob[0];
        }
        else if (prob.length == 2) {
            return prob[0] + prob[1] - prob[0] * prob[1];
        }
        else if (prob.length == 3) {
            return prob[0] + prob[1] + prob[2]
                    - prob[0] * prob[1] - prob[0] * prob[2] - prob[1] * prob[2]
                    + prob[0] * prob[1] * prob[2];
        }
        else if (prob.length == 4) {
            return prob[0] + prob[1] + prob[2] + prob[3]
                    - prob[0] * prob[1] - prob[0] * prob[2] - prob[0] * prob[3]
                    - prob[1] * prob[2] - prob[1] * prob[3]
                    - prob[2] * prob[3]
                    + prob[0] * prob[1] * prob[2] + prob[0] * prob[1] * prob[3]
                    + prob[1] * prob[2] * prob[3]
                    - prob[0] * prob[1] * prob[2] * prob[3];
        }
        else if (prob.length == 5) {
            return prob[0] + prob[1] + prob[2] + prob[3] + prob[4]
                    - prob[0] * prob[1] - prob[0] * prob[2] - prob[0] * prob[3] - prob[0] * prob[4]
                    - prob[1] * prob[2] - prob[1] * prob[3] - prob[1] * prob[4]
                    - prob[2] * prob[3] - prob[2] * prob[4]
                    - prob[3] * prob[4]
                    + prob[0] * prob[1] * prob[2] + prob[0] * prob[1] * prob[3] + prob[0] * prob[1] * prob[4]
                    + prob[1] * prob[2] * prob[3] + prob[1] * prob[2] * prob[4]
                    + prob[2] * prob[3] * prob[4]
                    - prob[0] * prob[1] * prob[2] * prob[3] - prob[0] * prob[1] * prob[2] * prob[4]
                    - prob[1] * prob[2] * prob[3] * prob[4]
                    + prob[0] * prob[1] * prob[2] * prob[3] * prob[4];
        }
        else {
            return -1;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_chance, menu);
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

    public void returnToHome(View view) {
        Intent homeIntent = new Intent(this, MainActivity.class);
        startActivity(homeIntent);
    }
}
