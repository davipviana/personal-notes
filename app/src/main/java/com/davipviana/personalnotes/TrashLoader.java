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

public class TrashLoader extends AsyncTaskLoader<List<Trash>> {
    private List<Trash> trashList;
    private ContentResolver contentResolver;
    private Cursor cursor;

    public TrashLoader(Context ctx, ContentResolver cr) {
        super(ctx);
        this.contentResolver = cr;
    }

    @Override
    public List<Trash> loadInBackground() {
        List<Trash> entries = new ArrayList<>();
        String[] projection = {BaseColumns._ID,
                TrashContract.TrashColumns.TRASH_TITLE,
                TrashContract.TrashColumns.TRASH_DESCRIPTION,
                TrashContract.TrashColumns.TRASH_DATE_TIME
        };
        Uri uri = TrashContract.URI_TABLE;
        cursor = contentResolver.query(uri, projection, null, null, BaseColumns._ID + " DESC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndex(TrashContract.TrashColumns.TRASH_TITLE));
                    String description = cursor.getString(cursor.getColumnIndex(TrashContract.TrashColumns.TRASH_DESCRIPTION));
                    String dateTime = cursor.getString(cursor.getColumnIndex(TrashContract.TrashColumns.TRASH_DATE_TIME));
                    int _id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                    Trash trash = new Trash(_id, title, description, dateTime);

                    entries.add(trash);

                } while (cursor.moveToNext());
            }
        }
        return entries;
    }

    @Override
    public void deliverResult(List<Trash> trashs) {
        if (isReset()) {
            if(trashs != null) {
                releaseResources();
                return;
            }
        }
        List<Trash> oldTrash = trashList;
        trashList = trashs;
        if(isStarted()) {
            super.deliverResult(trashs);
        }
        if(oldTrash != null && oldTrash != trashs) {
            releaseResources();
        }
    }

    @Override
    protected void onStartLoading() {
        if(trashList != null) {
            deliverResult(trashList);
        }
        if(takeContentChanged()) {
            forceLoad();
        } else if(trashList == null) {
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
        if(trashList != null) {
            releaseResources();
            trashList = null;
        }
    }


    @Override
    public void onCanceled(List<Trash> trash) {
        super.onCanceled(trash);
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
