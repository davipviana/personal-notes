package com.davipviana.personalnotes;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Davi Viana on 25/02/2018.
 */

public class NotesLoader extends AsyncTaskLoader<List<Note>> {

    private List<Note> noteList;
    private ContentResolver contentResolver;
    private Cursor cursor;
    private int type; // Reminder of a note

    public NotesLoader(Context context, ContentResolver contentResolver, int type) {
        super(context);
        this.contentResolver = contentResolver;
        this.type = type;
    }

    @Override
    public List<Note> loadInBackground() {
        List<Note> entries = new ArrayList<>();
        String[] projection = {
                BaseColumns._ID,
                NotesContract.NotesColumns.NOTES_TITLE,
                NotesContract.NotesColumns.NOTES_DESCRIPTION,
                NotesContract.NotesColumns.NOTES_TYPE,
                NotesContract.NotesColumns.NOTES_DATE,
                NotesContract.NotesColumns.NOTES_TIME,
                NotesContract.NotesColumns.NOTES_IMAGE,
                NotesContract.NotesColumns.NOTES_IMAGE_STORAGE_SELECTION };

        Uri uri = NotesContract.URI_TABLE;
        cursor = contentResolver.query(uri, projection, null, null, BaseColumns._ID + " DESC");
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                do {
                    String date = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTES_DATE));
                    String time = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTES_TIME));
                    String type = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTES_TYPE));
                    String title = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTES_TITLE));
                    String description = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTES_DESCRIPTION));
                    String imagePath = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTES_IMAGE));
                    int imageSelection = cursor.getInt(cursor.getColumnIndex(NotesContract.NotesColumns.NOTES_IMAGE_STORAGE_SELECTION));
                    int _id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                    if(this.type == BaseActivity.NOTES) {
                        if(time.equals(AppConstant.NO_TIME)) {
                            time = "";
                            Note note = new Note(title, description, date, time, _id, imageSelection, type);
                            if(!imagePath.equals(AppConstant.NO_IMAGE)) {
                                if(imageSelection == AppConstant.DEVICE_SELECTION) {
                                    note.setBitmap(imagePath);
                                } else {
                                    // Is a Google Drive or Dropbox Image
                                }
                            } else {
                                note.setImagePath(AppConstant.NO_IMAGE);
                            }

                            entries.add(note);
                        }

                    } else if(this.type == BaseActivity.REMINDERS) {
                        if(time.equals(AppConstant.NO_TIME)) {
                            Note note = new Note(title, description, date, time, _id, imageSelection, type);
                            if(!imagePath.equals(AppConstant.NO_IMAGE)) {
                                if(imageSelection == AppConstant.DEVICE_SELECTION) {
                                    note.setBitmap(imagePath);
                                } else {
                                    // Is a Google Drive or Dropbox Image
                                }
                            } else {
                                note.setImagePath(AppConstant.NO_IMAGE);
                            }

                            entries.add(note);
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid type = " + this.type);
                    }
                } while(cursor.moveToNext());
            }
        }

        return entries;
    }

    @Override
    public void deliverResult(List<Note> notes) {
        if (isReset()) {
            if(notes != null) {
                releaseResources();
                return;
            }
        }
        List<Note> oldNotes = noteList;
        noteList = notes;
        if(isStarted()) {
            super.deliverResult(notes);
        }
        if(oldNotes != null && oldNotes != notes) {
            releaseResources();
        }
    }

    @Override
    protected void onStartLoading() {
        if(noteList != null) {
            deliverResult(noteList);
        }
        if(takeContentChanged()) {
            forceLoad();
        } else if(noteList == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        if(noteList != null) {
            releaseResources();
            noteList = null;
        }
    }


    @Override
    public void onCanceled(List<Note> notes) {
        super.onCanceled(notes);
        releaseResources();
    }

    @Override
    public void forceLoad() {
        super.forceLoad();
    }

    private void releaseResources() {
        cursor.close();
    }
}
