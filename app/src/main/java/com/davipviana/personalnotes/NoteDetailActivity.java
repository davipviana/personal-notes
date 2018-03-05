package com.davipviana.personalnotes;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

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
            Note aNote = new Note(getIntent().getStringExtra(AppConstant.REMINDER));
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
                        setUpList(description);
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

    private void setValues(Note note) {
        getSupportActionBar().setTitle(AppConstant.REMINDERS);
        String title = note.getTitle();
        String description = note.getDescription();
        String time = note.getTime();
        String date = note.getDate();
        String image = note.getImagePath();
        if(note.getType().equals(AppConstant.LIST)) {
            isList = true;
        }
        titleEditText.setText(title);
        if(isList) {
            initializeComponents(LIST);
            CardView cardView = (CardView) findViewById(R.id.card_view);
            cardView.setVisibility(View.GONE);
            setUpList(description);
        } else {
            descriptionEditText.setText(description);
        }
        timeTextView.setText(time);
        dateTextView.setText(date);
        imagePath = image;
        int storageSelection = note.getStorageSelection();
        switch(storageSelection) {
            case AppConstant.GOOGLE_DRIVE_SELECTION:
                updateStorageSelection(null, R.drawable.ic_google_drive, AppConstant.GOOGLE_DRIVE_SELECTION);
                break;
            case AppConstant.DEVICE_SELECTION:
            case AppConstant.NONE_SELECTION:
                if(!imagePath.equals(AppConstant.NO_IMAGE)) {
                    updateStorageSelection(null, R.drawable.ic_local, AppConstant.DEVICE_SELECTION);
                }
                break;
            case AppConstant.DROP_BOX_SELECTION:
                updateStorageSelection(BitmapFactory.decodeFile(imagePath), R.drawable.ic_dropbox, AppConstant.DROP_BOX_SELECTION);
                break;

            default:
                break;
        }
    }

    private void updateStorageSelection(Bitmap bitmap, int storageSelectionResource, int selection) {
        if(bitmap != null) {
            noteImage.setImageBitmap(bitmap);
        }
        storageSelection.setBackgroundResource(storageSelectionResource);
        AppSharedPreferences.setPersonalNotesPreference(getApplicationContext(), selection);
    }

    private void setUpList(String description) {
        description = description;
        if(!isNotificationMode) {
            noteCustomList.setUpForEditMode(description);
        } else {
            LinearLayout newItemLayout = (LinearLayout) findViewById(R.id.add_check_list_layout);
            newItemLayout.setVisibility(View.GONE);
            noteCustomList.setUpForListNotification(description);
        }

        LinearLayout layout = (LinearLayout) findViewById(R.id.add_check_list_layout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteCustomList.addNewCheckBox();
            }
        });
    }

    private void initializeComponents(int choice) {
        if(choice == LIST) {
            CardView cardView = (CardView) findViewById(R.id.card_view);
            cardView.setVisibility(View.GONE);
            cardView = (CardView) findViewById(R.id.card_view_list);
            cardView.setVisibility(View.VISIBLE);
            isList = true;
        } else if(choice == NORMAL) {
            CardView cardView = (CardView) findViewById(R.id.card_view_list);
            cardView.setVisibility(View.GONE);
            isList = false;
        }

        storageSelection = (ImageView) findViewById(R.id.image_storage);
        if(AppSharedPreferences.getUploadPreference(getApplicationContext()) ==
                AppConstant.GOOGLE_DRIVE_SELECTION) {
            storageSelection.setBackgroundResource(R.drawable.ic_google_drive);
        } else if(AppSharedPreferences.getUploadPreference(getApplicationContext()) ==
                AppConstant.DROP_BOX_SELECTION) {
            storageSelection.setBackgroundResource(R.drawable.ic_dropbox);
        } else {
            storageSelection.setBackgroundResource(R.drawable.ic_local);
        }

        noteCustomList = new NoteCustomList(this);
        noteCustomList.setUp();
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.check_list_layout);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(noteCustomList);
        storageSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(NoteDetailActivity.this, v);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.actions_image_selection, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getItemId() == R.id.action_device) {
                            updateStorageSelection(null, R.drawable.ic_loading, AppConstant.DEVICE_SELECTION);
                        } else if(menuItem.getItemId() == R.id.action_google_drive) {
                            if(!AppSharedPreferences.isGoogleDriveAuthenticated(getApplicationContext())) {
                                startActivity(new Intent(NoteDetailActivity.this, GoogleDriveSelectionActivity.class));
                                finish();
                            } else {
                                updateStorageSelection(null, R.drawable.ic_google_drive, AppConstant.GOOGLE_DRIVE_SELECTION);
                            }
                        } else if(menuItem.getItemId() == R.id.action_dropbox) {
                            AppSharedPreferences.setPersonalNotesPreference(getApplicationContext(), AppConstant.DROP_BOX_SELECTION);
                            if(!AppSharedPreferences.isGoogleDriveAuthenticated(getApplicationContext())) {
                                startActivity(new Intent(NoteDetailActivity.this, DropBoxPickerActivity.class));
                                finish();
                            } else {
                                updateStorageSelection(null, R.drawable.ic_dropbox, AppConstant.DROP_BOX_SELECTION);
                            }
                        }

                        if(bundle != null) {
                            cameraFileName = bundle.getString("cameraFileName");
                        }
                        AndroidAuthSession session = DropBoxActions.buildSession(getApplicationContext());
                        dropboxAPI = new DropboxAPI<AndroidAuthSession>(session);

                        return false;
                    }
                });
            }
        });

        titleEditText = (EditText) findViewById(R.id.make_note_title);
        noteImage = (ImageView) findViewById(R.id.image_make_note);
        descriptionEditText = (EditText) findViewById(R.id.make_note_detail);
        dateTextView = (TextView) findViewById(R.id.date_textview_make_note);
        timeTextView = (TextView) findViewById(R.id.time_textview_make_note);
        ImageView datePickerImageView = (ImageView) findViewById(R.id.date_picker_button);
        ImageView dateTimeDeleteImageView = (ImageView) findViewById(R.id.delete_make_note);
        dateTimeDeleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateTextView.setText("");
                timeTextView.setText(AppConstant.NO_TIME);
            }
        });

        datePickerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppDatePickerDialog datePickerDialog = new AppDatePickerDialog();
                datePickerDialog.show(getSupportFragmentManager(), AppConstant.DATE_PICKER);
            }
        });

        LinearLayout layout = (LinearLayout) findViewById(R.id.add_check_list_layout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteCustomList.addNewCheckBox();
            }
        });
    }

    private Calendar getTargetTime() {
        Calendar calNow = Calendar.getInstance();
        Calendar calSet = (Calendar) calNow.clone();
        calSet.set(Calendar.MONTH, month);
        calSet.set(Calendar.YEAR, year);
        calSet.set(Calendar.DAY_OF_MONTH, day);
        calSet.set(Calendar.HOUR_OF_DAY, hour);
        calSet.set(Calendar.MINUTE, minute);
        calSet.set(Calendar.SECOND, second);
        calSet.set(Calendar.MILLISECOND, 0);
        if(calSet.compareTo(calNow) <=0) {
            calSet.add(Calendar.DATE, 1);
        }

        return calSet;
    }

    private void setAlarm(Calendar targetCal, Note note) {
        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(AppConstant.REMINDER, note.convertToString());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                note.getId(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);
    }

    private void saveInDropBox() {
        AndroidAuthSession session = DropBoxActions.buildSession(getApplicationContext());
        dropboxAPI = new DropboxAPI<AndroidAuthSession>(session);
        session = dropboxAPI.getSession();
        if(session.authenticationSuccessful()) {
            try  {
                session.finishAuthentication();
                DropBoxActions.storeAuth(session, getApplicationContext());
            } catch(IllegalStateException e) {
                showToast(AppConstant.AUTH_ERROR_DROPBOX + e.getLocalizedMessage());
            }
        }

        DropBoxImageUploadAsync upload = new DropBoxImageUploadAsync(this, dropboxAPI,
                dropBoxFile, AppConstant.NOTE_PREFIX + GDUT.time2Titl(null) + AppConstant.JPG);
        upload.execute();
        ContentValues values = createContentValues(AppConstant.NOTE_PREFIX + GDUT.time2Titl(null), AppConstant.DROP_BOX_SELECTION, true);
        createNoteAlarm(values, insertNote(values));
    }

    private void saveInGoogleDrive() {
        GDUT.init(this);
        if(checkPlayServices() && checkUserAccount()) {
            GDActions.init(this, GDUT.AM.getActiveEmil());
            GDActions.connect(true);
        }
        if(bundle != null) {
            tmpFlNm = bundle.getString(AppConstant.TMP_FILE_NAME);
        }
        final String resourceId = AppConstant.NOTE_PREFIX + GDUT.time2Titl(null) + AppConstant.JPG;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    File tmpFl = new File(imagePath);
                    GDActions.create(AppSharedPreferences.getGoogleDriveResourceId(getApplicationContext()),
                            resourceId, GDUT.MIME_JPEG, GDUT.file2Bytes(tmpFl));
                } catch(InterruptedException e) {
                    e.printStackTrace();
                    // Add more error handling here
                }

            }
        }).start();
        ContentValues values = createContentValues(AppConstant.NOTE_PREFIX + GDUT.time2Titl(null) +
                AppConstant.JPG, AppConstant.GOOGLE_DRIVE_SELECTION, true);
        createNoteAlarm(values, insertNote(values));
    }

    private void saveInDevice() {
        ContentValues values = createContentValues(imagePath, AppConstant.DEVICE_SELECTION, true);
        int newId = insertNote(values);
        id = newId + "";
        createNoteAlarm(values, newId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(AppConstant.TMP_FILE_NAME, tmpFlNm);
        outState.putString("cameraFileName", cameraFileName);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(NoteDetailActivity.this, NotesActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case R.id.action_camera:
                goingToCameraOrGallery = true;
                callCamera();
                break;

            case R.id.action_gallery:
                goingToCameraOrGallery = true;
                callGallery();
                break;

            case android.R.id.home:
                if(!isNotificationMode) {
                    saveNote();
                } else {
                    if(!timeTextView.getText().toString().equals(AppConstant.NO_TIME)) {
                        actAsReminder();
                    } else {
                        actAsNote();
                    }
                    moveToArchive(isList);
                    type = ARCHIVES;
                    title = AppConstant.ARCHIVES;
                    startActivity(new Intent(NoteDetailActivity.this, ArchivesActivity.class));
                    finish();
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void removeFromReminder(Note reminder) {
        ContentResolver cr = getContentResolver();
        Uri uri = Uri.parse(NotesContract.BASE_CONTENT_URI + "/notes/" + reminder.getId());
        cr.delete(uri, null, null);
    }

    private void moveToArchive(boolean isList) {
        String type;
        ContentValues values = new ContentValues();
        TextView title = (TextView) findViewById(R.id.make_note_title);
        TextView description = (TextView) findViewById(R.id.make_note_detail);
        TextView dateTime = (TextView) findViewById(R.id.time_textview_make_note);
        values.put(ArchivesContract.ArchivesColumns.ARCHIVES_TITLE, title.getText().toString());
        values.put(ArchivesContract.ArchivesColumns.ARCHIVES_DATE_TIME, dateTime.getText().toString());
        if(isList) {
            type = AppConstant.LIST;
            values.put(ArchivesContract.ArchivesColumns.ARCHIVES_DESCRIPTION, this.description);
        } else {
            type = AppConstant.NORMAL;
            values.put(ArchivesContract.ArchivesColumns.ARCHIVES_DESCRIPTION, description.getText().toString());
        }

        values.put(ArchivesContract.ArchivesColumns.ARCHIVES_TYPE, type);
        values.put(ArchivesContract.ArchivesColumns.ARCHIVES_CATEGORY, AppConstant.REMINDERS);

        ContentResolver cr = getContentResolver();
        Uri uri = Uri.parse(ArchivesContract.BASE_CONTENT_URI + "/archives");
        cr.insert(uri, values);
    }

    private void callGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, TAKE_GALLERY_CODE);
    }

    protected void saveNote() {
        if (isEditing) {
            switch (AppSharedPreferences.getUploadPreference(getApplicationContext())) {
                case AppConstant.DROP_BOX_SELECTION:
                    if (!imagePath.equals(AppConstant.NO_IMAGE)) {
                        editForSaveInDropBox();
                    } else {
                        editForSaveInDevice();
                    }
                    break;

                case AppConstant.GOOGLE_DRIVE_SELECTION:
                    if (!imagePath.equals(AppConstant.NO_IMAGE) && isImageSet) {
                        editForSaveInGoogleDrive();
                    } else {
                        editForSaveInDevice();
                    }
                    break;

                case AppConstant.DEVICE_SELECTION:
                case AppConstant.NONE_SELECTION:
                    editForSaveInDevice();
                    break;
            }
        } else if (titleEditText.getText().toString().length() > 0 && !goingToCameraOrGallery) {
            switch (AppSharedPreferences.getUploadPreference(getApplicationContext())) {
                case AppConstant.DROP_BOX_SELECTION:
                    if (!imagePath.equals(AppConstant.NO_IMAGE)) {
                        saveInDropBox();
                    } else {
                        saveInDevice();
                    }
                    break;

                case AppConstant.GOOGLE_DRIVE_SELECTION:
                    if (!imagePath.equals(AppConstant.NO_IMAGE)) {
                        saveInGoogleDrive();
                    } else {
                        saveInDevice();
                    }
                    break;

                case AppConstant.DEVICE_SELECTION:
                case AppConstant.NONE_SELECTION:
                    saveInDevice();
                    break;
            }
        }
        startActivity(new Intent(NoteDetailActivity.this, NotesActivity.class));
        finish();
    }

    private void editForSaveInDropBox() {
        if(AppSharedPreferences.isDropBoxAuthenticated(getApplicationContext())) {
            AndroidAuthSession session = DropBoxActions.buildSession(getApplicationContext());
            dropboxAPI = new DropboxAPI<AndroidAuthSession>(session);
            session = dropboxAPI.getSession();
            if(session.authenticationSuccessful()) {
                try {
                    session.finishAuthentication();
                    DropBoxActions.storeAuth(session, getApplicationContext());
                } catch(IllegalStateException e) {
                    showToast(AppConstant.AUTH_ERROR_DROPBOX + e.getLocalizedMessage());
                }
            }
        }
        ContentValues values = createContentValues("", AppConstant.DROP_BOX_SELECTION, false);
        if(isImageSet) {
            String filename = AppConstant.NOTE_PREFIX + GDUT.time2Titl(null) + AppConstant.JPG;
            values.put(NotesContract.NotesColumns.NOTES_IMAGE, filename);
            dropBoxFile = new File(getApplicationContext().getCacheDir(), filename);
            try {
                dropBoxFile.createNewFile();
            } catch(IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Bitmap newImage = ((BitmapDrawable) noteImage.getDrawable()).getBitmap();
            newImage.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapData = bos.toByteArray();
            try {
                FileOutputStream fos = new FileOutputStream(dropBoxFile);
                fos.write(bitmapData);
                fos.flush();
                fos.close();
            } catch(IOException e) {
                e.printStackTrace();
            }

            DropBoxImageUploadAsync uploadAsync = new DropBoxImageUploadAsync(this, dropboxAPI, dropBoxFile, filename);
            uploadAsync.execute();
        }

        updateNote(values);
        createNoteAlarm(values, (int) System.currentTimeMillis());
    }

    private void editForSaveInGoogleDrive() {
        GDUT.init(this);
        final String resourceId = AppConstant.NOTE_PREFIX + GDUT.time2Titl(null) + AppConstant.JPG;
        if(checkPlayServices() && checkUserAccount()) {
            GDActions.init(this, GDUT.AM.getActiveEmil());
            GDActions.connect(true);
        }
        if(bundle != null) {
            tmpFlNm = bundle.getString(AppConstant.TMP_FILE_NAME);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    if(isImageSet) {
                        File tmpFL = null;
                        try {
                            tmpFL = new File(imagePath);
                            GDActions.create(AppSharedPreferences.getGoogleDriveResourceId(getApplicationContext()),
                                    resourceId, GDUT.MIME_JPEG, GDUT.file2Bytes(tmpFL));
                        } finally {
//                            if(tmpFL != null) {
//                                tmpFL.delete();
//                            }
                        }
                    }
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        ContentValues values = createContentValues(resourceId, AppConstant.GOOGLE_DRIVE_SELECTION,  false);
        updateNote(values);
        createNoteAlarm(values, (int) System.currentTimeMillis());
    }

    private void editForSaveInDevice() {
        ContentValues values = createContentValues(imagePath, AppConstant.DEVICE_SELECTION, false);
        updateNote(values);
        createNoteAlarm(values, Integer.parseInt(id));
    }

    private void callCamera() {
        Intent cameraIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap photo = null;
        switch(requestCode) {
            case AppConstant.REQ_ACCPICK:
                if(resultCode == Activity.RESULT_OK && data != null) {
                    String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if(GDUT.AM.setEmil(email) == GDUT.AM.CHANGED) {
                        GDActions.init(this, GDUT.AM.getActiveEmil());
                        GDActions.connect(true);
                    }
                } else if(GDUT.AM.getActiveEmil() == null) {
                    GDUT.AM.removeActiveAccnt();
                    finish();
                }
                break;
            case AppConstant.REQ_AUTH:
            case AppConstant.REQ_RECOVER:
                isInAuth = false;
                if(resultCode == Activity.RESULT_OK) {
                    GDActions.connect(true);
                } else if(resultCode == RESULT_CANCELED) {
                    GDUT.AM.removeActiveAccnt();
                    finish();
                }
                break;

            case AppConstant.REQ_SCAN: {
                if(resultCode == Activity.RESULT_OK) {
                    final String titl = GDUT.time2Titl(null);
                    if (titl != null && tmpFlNm != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                File tmpFl = null;
                                GDActions.createTreeGDAA(GDUT.MYROOT, titl, GDUT.file2Bytes(tmpFl));
                            }
                        }).start();
                    }
                }
                break;
            }

        }

        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            goingToCameraOrGallery = false;
            photo = (Bitmap) data.getExtras().get("data");
            noteImage.setImageBitmap(photo);
            Uri tempUri = getImageUri(getApplicationContext(), photo);
            File finalFile = new File(getRealPathFromURI(tempUri));
            imagePath = finalFile.toString();
            isImageSet = true;
        } else if (requestCode == TAKE_GALLERY_CODE) {
            if(resultCode == RESULT_OK) {
                goingToCameraOrGallery = false;
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imagePath = cursor.getString(columnIndex);
                cursor.close();
                File tempFile = new File(imagePath);
                photo = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
                noteImage.setVisibility(View.VISIBLE);
                noteImage.setImageBitmap(photo);
                isImageSet = true;
            } else {
                isImageSet = false;
            }
        }
        if(isImageSet) {
            dropBoxFile = new File(imagePath);
        }
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(),
                inImage, "Title", null);
        return Uri.parse(path);
    }

    private String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if(!isInAuth) {
            if(connectionResult.hasResolution()) {
                try {
                    isInAuth = true;
                    connectionResult.startResolutionForResult(this, AppConstant.REQ_AUTH);
                } catch(IntentSender.SendIntentException e) {
                    e.printStackTrace();
                    // Add other error handling here
                    finish();
                }
            } else {
                finish();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    private boolean checkUserAccount() {
        String email = GDUT.AM.getActiveEmil();
        Account account = GDUT.AM.getPrimaryAccnt(true);
        if(email == null) {
            if(account == null) {
                account = showAccountPicker();
                return false;
            } else {
                // Only one a/c registered
                GDUT.AM.setEmil(account.name);
            }
            return true;
        }

        account = GDUT.AM.getActiveAccnt();
        if(account == null) {
            account = showAccountPicker();
            return false;
        }
        return true;
    }

    private boolean checkPlayServices() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(status != ConnectionResult.SUCCESS) {
            if(GooglePlayServicesUtil.isUserRecoverableError(status)) {
                errorDialog(status, AppConstant.REQ_RECOVER);
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    private void errorDialog(int errorCode, int requestCode) {
        Bundle args = new Bundle();
        args.putInt(AppConstant.DIALOG_ERROR, errorCode);
        args.putInt(AppConstant.REQUEST_CODE, requestCode);
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), AppConstant.DIALOG_ERROR);
    }

    private ContentValues createContentValues(String noteImage, int storageSelection, boolean isSave) {
        if(noteImage == null || noteImage.equals("")) {
            noteImage = AppConstant.NO_IMAGE;
        }
        ContentValues values = new ContentValues();
        values.put(NotesContract.NotesColumns.NOTES_TITLE, titleEditText.getText().toString());
        values.put(NotesContract.NotesColumns.NOTES_DATE, dateTextView.getText().toString());
        values.put(NotesContract.NotesColumns.NOTES_TIME, timeTextView.getText().toString());
        if(isImageSet || isSave) {
            values.put(NotesContract.NotesColumns.NOTES_IMAGE, noteImage);
        }
        values.put(NotesContract.NotesColumns.NOTES_IMAGE_STORAGE_SELECTION, storageSelection);
        String type = AppConstant.NORMAL;
        String description = descriptionEditText.getText().toString();
        if(isList) {
            description = noteCustomList.getLists();
            type = AppConstant.LIST;
        }

        values.put(NotesContract.NotesColumns.NOTES_TYPE, type);
        values.put(NotesContract.NotesColumns.NOTES_DESCRIPTION, description);

        return values;
    }

    private int insertNote(ContentValues values) {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = Uri.parse(NotesContract.BASE_CONTENT_URI + "/notes");
        Uri returned = contentResolver.insert(uri, values);
        String[] temp = returned.toString().split("/");
        return Integer.parseInt(temp[temp.length-1]);
    }

    private void updateNote(ContentValues values) {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = Uri.parse(NotesContract.BASE_CONTENT_URI + "/notes");
        String selection = NotesContract.NotesColumns.NOTE_ID + " = " + id;
        contentResolver.update(uri, values, selection, null);
    }

    private void createNoteAlarm(ContentValues values, int id) {
        if(!timeTextView.getText().toString().equals(AppConstant.NO_TIME)) {
            Note note = new Note(values.getAsString(NotesContract.NotesColumns.NOTES_TITLE),
                    values.getAsString(NotesContract.NotesColumns.NOTES_DESCRIPTION),
                    values.getAsString(NotesContract.NotesColumns.NOTES_DATE),
                    values.getAsString(NotesContract.NotesColumns.NOTES_TIME),
                    id,
                    values.getAsInteger(NotesContract.NotesColumns.NOTES_IMAGE_STORAGE_SELECTION),
                    values.getAsString(NotesContract.NotesColumns.NOTES_TYPE));
            note.setImagePath(values.getAsString(NotesContract.NotesColumns.NOTES_IMAGE));
            setAlarm(getTargetTime(), note);

        }
    }

    private Account showAccountPicker() {
        Account account = GDUT.AM.getPrimaryAccnt(false);
        Intent intent = AccountPicker.newChooseAccountIntent(account, null,
                new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE},true,null, null, null, null);
        startActivityForResult(intent, AppConstant.REQ_ACCPICK);
        return account;
    }

    public static class AppDatePickerDialog extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private int mYear, mMonth, mDay;
        private String tempMonth;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            tempMonth = c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.UK);
            return new DatePickerDialog(getActivity(), this, mYear, mMonth, mDay);
        }

        @Override
        public void onDateSet(DatePicker view, int newYear, int monthOfYear, int dayOfMonth) {
            if(newYear == mYear) {
                if(monthOfYear == mMonth) {
                    if(dayOfMonth == mDay) {
                        dateTextView.setText(AppConstant.TODAY);
                    } else {
                        dateTextView.setText(dayOfMonth + " " + month);
                    }
                } else {
                    dateTextView.setText(dayOfMonth + " " + month);
                }
            } else {
                dateTextView.setText(dayOfMonth + " " + month + " " + newYear);
            }
            year = newYear;
            month = monthOfYear;
            day = dayOfMonth;
            AppTimePickerDialog timePickerDialog = new AppTimePickerDialog();
            timePickerDialog.show(getFragmentManager(), AppConstant.DATE_PICKER);
        }
    }

    public static class AppTimePickerDialog extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        private int mHour, mMinute;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, mHour, mMinute, DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int newMinute) {
            if(minute <10) {
                timeTextView.setText(hourOfDay + ":0" + newMinute);
            } else {
                timeTextView.setText(hourOfDay + ":" + newMinute);
            }
            hour = hourOfDay;
            minute = newMinute;
            second = 0;
        }
    }
}
