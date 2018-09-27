package com.example.photogallery;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SearchActivity extends AppCompatActivity {

    EditText keywordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        keywordText = findViewById(R.id.keywordText);
    }

    public void searchForKeyword(View view){
        Intent intent = new Intent();
        intent.putExtra("keywordText", keywordText.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }
}
