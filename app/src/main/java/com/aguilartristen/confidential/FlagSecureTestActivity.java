package com.aguilartristen.confidential;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class FlagSecureTestActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_confidential_chat);
    }
}