package com.mhacks.tong.medchance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SymptomsActivity extends Activity {

    public final static String SELECTED_SYMPTOMS_MSG = "com.mhacks.tong.medchance.SELECTED_SYMPTOMS";

    private ArrayList<String> checkedSymptoms = new ArrayList();
    private String currDisease;

    private String type;

    public void goToChance(View view) {
        if (checkedSymptoms.size() > 0) {
            Intent chanceIntent = new Intent(this, ChanceActivity.class);
            chanceIntent.putExtra(SELECTED_SYMPTOMS_MSG, this.checkedSymptoms);
            chanceIntent.putExtra(DiseaseSelectionActivity.SELECTED_DISEASE_MSG, this.currDisease);
            // Pass on the type extra
            chanceIntent.putExtra(MainActivity.TYPE, getIntent().getStringExtra(MainActivity.TYPE));
            startActivity(chanceIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms);

        Intent callingIntent = getIntent();

        String appropriateTense = "";
        if (callingIntent.getStringExtra(MainActivity.TYPE).equals(MainActivity.DATA_ENTRY_TYPE)) {
            appropriateTense = "you experienced:";
            setTitle("Contribute Data");
            this.type = MainActivity.DATA_ENTRY_TYPE;
        }
        else {
            appropriateTense = "you are experiencing: (You may select up to five symptoms)";
            setTitle("Calculate Chance");
            this.type = MainActivity.CHECK_TYPE;
        }

        TextView diseaseSpinner = (TextView) findViewById(R.id.whoop);
        this.currDisease = callingIntent.getStringExtra(DiseaseSelectionActivity.SELECTED_DISEASE_MSG);
        diseaseSpinner.setText("Select which of these symptoms of " + currDisease + " and related diseases " + appropriateTense);

        createSymptomCheckboxes(currDisease);
    }

    private void createSymptomCheckboxes(String disease) {
        ArrayList<String> symptomClasses = new ArrayList();
        List<String> fluClass = Arrays.asList(getResources().getStringArray(R.array.flu_class_diseases));
        if (fluClass.contains(disease)) {
            symptomClasses.add("flu_class_symptoms");
        }
        List<String> stdClass = Arrays.asList(getResources().getStringArray(R.array.std_class_diseases));
        if (stdClass.contains(disease)) {
            symptomClasses.add("std_class_symptoms");
        }
        List<String> chronicClass = Arrays.asList(getResources().getStringArray(R.array.chronic_class_diseases));
        if (chronicClass.contains(disease)) {
            symptomClasses.add("chronic_class_symptoms");
        }

        for (String diseaseClass : symptomClasses){
            LinearLayout checkboxLayout = (LinearLayout) findViewById(R.id.symptomLL);
            int symptomsResId = getResId(diseaseClass, R.array.class);
            String[] syms = getResources().getStringArray(symptomsResId);

            for (int i = 0; i < syms.length; i++) {
                final CheckBox ch = new CheckBox(this);
                ch.setText(syms[i]);
                ch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (type.equals(MainActivity.CHECK_TYPE)) {
                            if (ch.isChecked() && checkedSymptoms.size() < 5) {
                                checkedSymptoms.add((String) ch.getText());
                            } else if (ch.isChecked()) {
                                ch.setChecked(false);
                            } else {
                                checkedSymptoms.remove(ch.getText());
                            }
                        }
                        else {
                            if (ch.isChecked()) {
                                checkedSymptoms.add((String) ch.getText());
                            } else {
                                checkedSymptoms.remove(ch.getText());
                            }
                        }
                    }
                });
                checkboxLayout.addView(ch);
            }
        }
    }

    // http://stackoverflow.com/questions/4427608/android-getting-resource-id-from-string
    public static int getResId(String variableName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(variableName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_symptoms, menu);
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
