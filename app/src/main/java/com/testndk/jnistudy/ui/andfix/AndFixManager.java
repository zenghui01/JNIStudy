package com.testndk.jnistudy.ui.andfix;

public class AndFixManager {
    private static final AndFixManager ourInstance = new AndFixManager();

    public static AndFixManager getInstance() {
        return ourInstance;
    }

    private AndFixManager() {

    }
}
