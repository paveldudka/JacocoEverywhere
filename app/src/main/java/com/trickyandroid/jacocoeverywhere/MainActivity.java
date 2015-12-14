package com.trickyandroid.jacocoeverywhere;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override public boolean onSearchRequested() {
        return super.onSearchRequested();
    }

    @Override protected void onStart() {
        super.onStart();
    }

    @Override protected void onStop() {
        super.onStop();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }

    @Override protected void onPause() {
        super.onPause();
    }

    @Override protected void onRestart() {
        super.onRestart();
    }

    @Override protected void onResume() {
        super.onResume();
    }
}
