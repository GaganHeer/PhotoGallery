package com.example.photogallery;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SearchActivity extends AppCompatActivity {

    EditText keywordText;
    EditText startDateText;
    EditText endDateText;
    EditText topLatText;
    EditText topLongText;
    EditText btmLatText;
    EditText btmLongText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        keywordText = findViewById(R.id.keywordText);
        startDateText = findViewById(R.id.startDateText);
        endDateText = findViewById(R.id.endDateText);
        topLatText = findViewById(R.id.topLatText);
        topLongText = findViewById(R.id.topLongText);
        btmLatText = findViewById(R.id.btmLatText);
        btmLongText = findViewById(R.id.btmLongText);
    }

    public void search(View view){
        Intent intent = new Intent();
        intent.putExtra("keywordText", keywordText.getText().toString());
        intent.putExtra("startDateText", startDateText.getText().toString());
        intent.putExtra("endDateText", endDateText.getText().toString());
        intent.putExtra("topLatText", topLatText.getText().toString());
        intent.putExtra("topLongText", topLongText.getText().toString());
        intent.putExtra("btmLatText", btmLatText.getText().toString());
        intent.putExtra("btmLongText", btmLongText.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }
}
