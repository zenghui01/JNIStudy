package com.testndk.jnistudy.ui.andfix.replace;

import android.content.Context;
import android.widget.Toast;

import com.testndk.jnistudy.ui.andfix.MethodReplace;

/**
 * replace
 */
public class Calculator {

    @MethodReplace(className = "com.testndk.jnistudy.ui.andfix.error.Calculator", methodName = "calculator")
    public void calculator(Context context) {
        int i = 100;
        int y = 1;
        Toast.makeText(context, "" + (i / y), Toast.LENGTH_SHORT).show();
    }
}
