package com.davipviana.personalnotes;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;

/**
 * Created by Davi Viana on 25/02/2018.
 */

public class NoteDetailActivity extends BaseActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    // Constants
    public static final int NORMAL = 1;
    public static final int LIST = 2;
    public static final int CAMERA_REQUEST = 1888;
    public static final int TAKE_GALLERY_CODE = 1;
    private static int month, year, hour, day, minute, second;
    private static TextView dateTextView, timeTextView;
    private static boolean isInAuth;
    private static String tmpFlNm;
    private DropboxAPI<AndroidAuthSession> dropboxAPI;
    private File dropBoxFile;
    private String cameraFileName;
    private NoteCustomList noteCustomList;
    private EditText titleEditText, descriptionEditText;
    private ImageView noteImage;
    private String imagePath = AppConstant.NO_IMAGE;
    private String id;
    private boolean goingToCameraOrGallery = false, isEditing = false;
    private boolean isImageSet = false;
    private boolean isList = false;
    private Bundle bundle;
    private ImageView storageSelection;
    private boolean isNotificationMode = false;
    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.bundle = savedInstanceState;
        setContentView(R.layout.activity_detail_note_layout);
        activateToolbarWithHomeEnabled();
        if (getIntent().getStringExtra(AppConstant.LIST_NOTES) != null) {
            initializeComponents(LIST);
        } else {
            initializeComponents(NORMAL);
        }

        setUpIfEditing();
        if (getIntent().getStringExtra(AppConstant.GO_TO_CAMERA) != null) {
            callCamera();
        }

    }

    private void setUpIfEditing() {
        if (getIntent().getStringExtra(AppConstant.ID) != null) {
            id = getIntent().getStringExtra(AppConstant.ID);
            isEditing = true;
            if (getIntent().getStringExtra(AppConstant.LIST_NOTES) != null) {
                initializeComponents(LIST);
            }
            setValues(id);
            storageSelection.setEnabled(false);
        }
        if (getIntent().getStringExtra(AppConstant.REMINDER) != null) {
            Note aNote = new Note(getIntent().getStringExtra(AppConstant.REMINDER)));
            id = aNote.getId() + "";
            isNotificationMode = true;
            setValues(aNote);
            removeFromReminder(aNote);
            storageSelection.setEnabled(false);
        }
    }

    private void setValues(String id) {
        String[] projection = {BaseColumns._ID,
                NotesContract.NotesColumns.NOTES_TITLE,
                NotesContract.NotesColumns.NOTES_DESCRIPTION,
                NotesContract.NotesColumns.NOTES_DATE,
                NotesContract.NotesColumns.NOTES_IMAGE,
                NotesContract.NotesColumns.NOTES_IMAGE_STORAGE_SELECTION,
                NotesContract.NotesColumns.NOTES_TIME};
        // Query database - check parameters to return only partial records.
        Uri r = NotesContract.URI_TABLE;
        String selection = NotesContract.NotesColumns.NOTE_ID + " = " + id;
        Cursor cursor = getContentResolver().query(r, projection, selection, null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTES_TITLE));
                    String description = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTES_DESCRIPTION));
                    String time = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTES_TIME));
                    String date = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTES_DATE));
                    String image = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTES_IMAGE));
                    int storageSelection = cursor.getInt(cursor.getColumnIndex(NotesContract.NotesColumns.NOTES_IMAGE_STORAGE_SELECTION));
                    titleEditText.setText(title);
                    if(isList) {
                        CardView cardView = (CardView) findViewById(R.id.card_view);
                        cardView.setVisibility(View.GONE);
                        setupList(description);
                    } else {
                        descriptionEditText.setText(description);
                    }
                    timeTextView.setText(time);
                    dateTextView.setText(date);
                    imagePath = image;
                    if(!image.equals(AppConstant.NO_IMAGE)) {
                        noteImage.setImageBitmap(NotesActivity.sendingImage);
                    }
                    switch(storageSelection) {
                        case AppConstant.GOOGLE_DRIVE_SELECTION:
                            updateStorageSelection(null, R.drawable.ic_google_drive, AppConstant.GOOGLE_DRIVE_SELECTION);
                            break;
                        case AppConstant.DEVICE_SELECTION:
                        case AppConstant.NONE_SELECTION:
                            updateStorageSelection(null, R.drawable.ic_local, AppConstant.DEVICE_SELECTION);
                            break;
                        case AppConstant.DROP_BOX_SELECTION:
                            updateStorageSelection(null, R.drawable.ic_dropbox, AppConstant.DROP_BOX_SELECTION);
                            break;
                    }
                } while(cursor.moveToNext());
            }
        }

    }
}
