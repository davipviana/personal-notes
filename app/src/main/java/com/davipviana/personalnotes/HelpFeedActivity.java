package com.davipviana.personalnotes;

import android.os.Bundle;

/**
 * Created by Davi Viana on 25/02/2018.
 */

public class HelpFeedActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_feedback_layout);
        toolBar = activateToolbar();
        setUpNavigationDrawer();
    }
}
