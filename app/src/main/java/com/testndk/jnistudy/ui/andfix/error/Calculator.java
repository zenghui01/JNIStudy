package com.testndk.jnistudy.ui.andfix.error;

import android.content.Context;
import android.widget.Toast;

/**
 * error
 */
public class Calculator {

    public void calculator(Context context) {
        int i = 100;
        int y = 0;
        Toast.makeText(context, "" + (i / y), Toast.LENGTH_SHORT).show();
    }
}
