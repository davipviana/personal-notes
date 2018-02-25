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

public class ArchivesLoader extends AsyncTaskLoader<List<Archive>> {
    private List<Archive> archiveList;
    private ContentResolver contentResolver;
    private Cursor cursor;

    public ArchivesLoader(Context ctx, ContentResolver cr) {
        super(ctx);
        this.contentResolver = cr;
    }

    @Override
    public List<Archive> loadInBackground() {
        List<Archive> entries = new ArrayList<>();
        String[] projection = {BaseColumns._ID,
                ArchivesContract.ArchivesColumns.ARCHIVES_TITLE,
                ArchivesContract.ArchivesColumns.ARCHIVES_DESCRIPTION,
                ArchivesContract.ArchivesColumns.ARCHIVES_DATE_TIME,
                ArchivesContract.ArchivesColumns.ARCHIVES_CATEGORY,
                ArchivesContract.ArchivesColumns.ARCHIVES_TYPE
        };
        Uri uri = ArchivesContract.URI_TABLE;
        cursor = contentResolver.query(uri, projection, null, null, BaseColumns._ID + " DESC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndex(ArchivesContract.ArchivesColumns.ARCHIVES_TITLE));
                    String description = cursor.getString(cursor.getColumnIndex(ArchivesContract.ArchivesColumns.ARCHIVES_DESCRIPTION));
                    String dateTime = cursor.getString(cursor.getColumnIndex(ArchivesContract.ArchivesColumns.ARCHIVES_DATE_TIME));
                    String category = cursor.getString(cursor.getColumnIndex(ArchivesContract.ArchivesColumns.ARCHIVES_CATEGORY));
                    String type = cursor.getString(cursor.getColumnIndex(ArchivesContract.ArchivesColumns.ARCHIVES_TYPE));
                    int _id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                    Archive archive = new Archive(title, description, dateTime, category, type, _id);

                    entries.add(archive);

                } while (cursor.moveToNext());
            }
        }
        return entries;
    }

    @Override
    public void deliverResult(List<Archive> archives) {
        if (isReset()) {
            if(archives != null) {
                releaseResources();
                return;
            }
        }
        List<Archive> oldArchives = archiveList;
        archiveList = archives;
        if(isStarted()) {
            super.deliverResult(archives);
        }
        if(oldArchives != null && oldArchives != archives) {
            releaseResources();
        }
    }

    @Override
    protected void onStartLoading() {
        if(archiveList != null) {
            deliverResult(archiveList);
        }
        if(takeContentChanged()) {
            forceLoad();
        } else if(archiveList == null) {
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
        if(archiveList != null) {
            releaseResources();
            archiveList = null;
        }
    }


    @Override
    public void onCanceled(List<Archive> archive) {
        super.onCanceled(archive);
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
