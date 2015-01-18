package com.mhacks.tong.medchance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.lang.reflect.Field;


public class DiseaseSelectionActivity extends Activity implements AdapterView.OnItemSelectedListener {

    public final static String SELECTED_DISEASE_MSG = "com.mhacks.tong.medchance.SELECTED_DISEASE";
    private String selectedDisease = "...";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_selection);

        Intent callingIntent = getIntent();
        String type = callingIntent.getStringExtra(MainActivity.TYPE);
        if (type.equals(MainActivity.CHECK_TYPE)) {
            setTitle("Calculate Chance");
        }
        else {
            setTitle("Contribute Data");
        }

        Spinner diseaseSpinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.diseases_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        diseaseSpinner.setAdapter(adapter);
        diseaseSpinner.setOnItemSelectedListener(this);
    }

    // Spinner listener methods
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        TextView aboutTextView = (TextView) findViewById(R.id.aboutDisease);
        if (pos != 0) {
            this.selectedDisease = (String) parent.getItemAtPosition(pos);
            int diseaseID = getResId(selectedDisease.replace(' ', '_').replace('/', '_') + "_about", R.string.class);
            aboutTextView.setText(diseaseID);
        }
        else {
            this.selectedDisease = "...";
            aboutTextView.setText("");
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    // Submit button
    public void submitDisease(View view) {
        if (selectedDisease != "...") {
            Intent symIntent = new Intent(this, SymptomsActivity.class);
            symIntent.putExtra(SELECTED_DISEASE_MSG, this.selectedDisease);
            // Pass on type extra
            symIntent.putExtra(MainActivity.TYPE, getIntent().getStringExtra(MainActivity.TYPE));
            startActivity(symIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_disease_selection, menu);
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
}
