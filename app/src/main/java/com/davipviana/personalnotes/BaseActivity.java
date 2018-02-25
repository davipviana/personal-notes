package com.davipviana.personalnotes;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Davi Viana on 24/02/2018.
 */
public abstract class BaseActivity extends AppCompatActivity {

    // Operation type (what is being executed)
    public static final int NOTES = 1;
    public static final int REMINDERS = 2;
    public static final int ARCHIVES = 3;
    public static final int TRASH = 4;
    public static final int SETTINGS = 5;

    private Class nextActivity;

    // Default toolbar title (can change)
    public static String title = AppConstant.NOTES;

    // Default type of operation
    public static int type = NOTES;

    // Misc
    protected Toolbar toolBar;

    private NavigationDrawerFragment drawerFragment;

    // These will be the 3 icons on the screen to add a new note, list notes, or photo note
    public static ImageView makeNote, makeListNote, makePhotoNote;

    public void actAsReminder() {
        type = REMINDERS;
        title = AppConstant.REMINDERS;
    }

    public static void actAsNote() {
        type = NOTES;
        title = AppConstant.NOTES;
    }

    protected void setUpActions() {
        makeNote = (ImageView) findViewById(R.id.new_note);
        makeListNote = (ImageView) findViewById(R.id.new_list);
        makePhotoNote = (ImageView) findViewById(R.id.new_image);

        makeNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NoteDetailActivity.class);
                intent.putExtra(AppConstant.NOTE_OR_REMINDER, title);
                startActivity(intent);
            }
        });

        makeListNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NoteDetailActivity.class);
                intent.putExtra(AppConstant.LIST_NOTES, AppConstant.TRUE);
                intent.putExtra(AppConstant.NOTE_OR_REMINDER, title);
                startActivity(intent);
            }
        });
        makePhotoNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NoteDetailActivity.class);
                intent.putExtra(AppConstant.GO_TO_CAMERA, AppConstant.TRUE);
                intent.putExtra(AppConstant.NOTE_OR_REMINDER, title);
                startActivity(intent);
            }
        });
    }

    protected Toolbar activateToolbar() {
        if (toolBar == null) {
            toolBar = (Toolbar) findViewById(R.id.app_bar);
            if (toolBar != null) {
                setSupportActionBar(toolBar);
                switch (type) {
                    case REMINDERS:
                        getSupportActionBar().setTitle(AppConstant.REMINDERS);
                        break;
                    case NOTES:
                        getSupportActionBar().setTitle(AppConstant.NOTES);
                        break;
                    case ARCHIVES:
                        getSupportActionBar().setTitle(AppConstant.ARCHIVES);
                        break;
                    case TRASH:
                        getSupportActionBar().setTitle(AppConstant.TRASH);
                        break;
                }
            }
        }
        return toolBar;
    }

    protected Toolbar activateToolbarWithHomeEnabled() {
        activateToolbar();
        if (toolBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (type == REMINDERS)
                getSupportActionBar().setTitle(AppConstant.MAKE_REMINDER);
            else if (type == NOTES)
                getSupportActionBar().setTitle(AppConstant.MAKE_NOTES);
            else if (type == SETTINGS)
                getSupportActionBar().setTitle(AppConstant.SETTINGS);
        }
        return toolBar;
    }

    protected void removeActions() {
        CardView cardView;
        cardView = (CardView) findViewById(R.id.card_view);
        cardView.setVisibility(View.GONE);
    }

    protected void setUpNavigationDrawer() {

        ListView navigationListView;
        //Few items, so added manually

        List<NavigationDrawerItem> items = new ArrayList<>();
        items.add(new NavigationDrawerItem(android.R.drawable.ic_menu_agenda, AppConstant.DRAWER_NOTES));
        items.add(new NavigationDrawerItem(android.R.drawable.ic_popup_reminder, AppConstant.DRAWER_REMINDERS));
        items.add(new NavigationDrawerItem(android.R.drawable.ic_menu_gallery, AppConstant.DRAWER_ARCHIVES));
        items.add(new NavigationDrawerItem(android.R.drawable.ic_menu_delete, AppConstant.DRAWER_TRASH));
        items.add(new NavigationDrawerItem(android.R.drawable.ic_menu_preferences, AppConstant.DRAWER_SETTINGS));
        items.add(new NavigationDrawerItem(android.R.drawable.ic_menu_help, AppConstant.DRAWER_HELP_AND_FEEDBACK));

        //initialize the drawer fragment
        drawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolBar);

        NavigationDrawerAdapter navigationDrawerAdapter = new NavigationDrawerAdapter(getApplicationContext(), items);
        //initialize the list view for navigation drawer
        navigationListView = (ListView) findViewById(R.id.navigation_list);
        navigationListView.setAdapter(navigationDrawerAdapter);

        navigationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        nextActivity = NotesActivity.class;
                        actAsNote();
                        break;
                    case 1:
                        nextActivity = NotesActivity.class;
                        actAsReminder();
                        break;
                    case 2:
                        nextActivity = ArchivesActivity.class;
                        type = ARCHIVES;
                        break;
                    case 3:
                        nextActivity = TrashActivity.class;
                        type = TRASH;
                        break;
                    case 4:
                        nextActivity = AppAuthenticationActivity.class;
                        type = SETTINGS;
                        break;
                    case 5:
                        nextActivity = HelpFeedActivity.class;
                        break;
                    default:
                        nextActivity = HelpFeedActivity.class;
                }

                AppSharedPreferences.setUserLearned(getApplicationContext(), AppConstant.KEY_USER_LEARNED_DRAWER, AppConstant.TRUE);
                Intent intent = new Intent(BaseActivity.this, nextActivity);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(intent, 0);
                overridePendingTransition(0, 0);
                drawerFragment.closeDrawer();
            }
        });
    }
}