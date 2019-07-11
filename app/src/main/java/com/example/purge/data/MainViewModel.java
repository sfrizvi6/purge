package com.example.purge.data;

import androidx.lifecycle.ViewModel;
import android.view.View;

public class MainViewModel extends ViewModel {

    private static final String WELCOME_TEXT = "Welcome!";
    private static final String GET_STARTED = "Get Started";

    public View.OnClickListener scanItemButtonListener;
    public String welcomeText;

    public MainViewModel() {
        welcomeText = WELCOME_TEXT;
        scanItemButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                welcomeText = GET_STARTED;
            }
        };
    }

}
