package com.example.geofencepractice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

public class PopActivity extends Activity {

    final static double DEFAULT_VALUE = 0;

    TextView question;
    TextView latitudeView;
    TextView longitudeView;
    EditText nameBox;
    Button button_no;
    Button button_yes;

    String name;
    double latitude;
    double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop);

        Intent intent = getIntent();
        final String query = intent.getStringExtra(MainActivity.EXTRA_QUESTION);
        if(query == null) finish();
        latitudeView = findViewById(R.id.ExtraInfo1);
        nameBox = findViewById(R.id.editText);
        if(query.equals(getString(R.string.adding_question))) {
            latitude = intent.getDoubleExtra(MainActivity.EXTRA_LAT, DEFAULT_VALUE);
            longitude = intent.getDoubleExtra(MainActivity.EXTRA_LONG, DEFAULT_VALUE);
            latitudeView.setText(String.format("Latitude: %s", latitude));
            longitudeView = findViewById(R.id.ExtraInfo2);
            longitudeView.setText(String.format("Longitude: %s", longitude));
            nameBox.setVisibility(View.VISIBLE);
        } else if(query.equals(getString(R.string.removing_question))) {
            latitudeView = findViewById(R.id.ExtraInfo1);
            name = intent.getStringExtra(MainActivity.EXTRA_NAME);
            latitudeView.setText(String.format("Name: %s", name));
            nameBox.setVisibility(View.GONE);
        } else {
            finish();
        }

        //Set TextView
        question = findViewById(R.id.questionText);
        question.setText(query);

        //Set Buttons
        button_no = findViewById(R.id.button_no);
        button_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Return to main activity without doing anything
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        button_yes = findViewById(R.id.button_yes);
        button_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                int resultCode = RESULT_CANCELED;
                if(query.equals(getString(R.string.adding_question))) {
                    name = nameBox.getText().toString();
                    resultIntent.putExtra(MainActivity.EXTRA_NAME, name);
                    resultIntent.putExtra(MainActivity.EXTRA_LAT, latitude);
                    resultIntent.putExtra(MainActivity.EXTRA_LONG, longitude);
                    resultCode = RESULT_OK;
                } else if(query.equals(getString(R.string.removing_question))) {
                    resultIntent.putExtra(MainActivity.EXTRA_NAME, name);
                    resultCode = RESULT_OK;
                }
                setResult(resultCode, resultIntent);
                finish();
            }
        });


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.8), (int)(height*.5));


        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -50;
        getWindow().setAttributes(params);


    }
}
