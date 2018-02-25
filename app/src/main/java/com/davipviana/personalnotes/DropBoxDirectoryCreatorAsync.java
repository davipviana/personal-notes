package com.davipviana.personalnotes;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

/**
 * Created by Davi Viana on 25/02/2018.
 */

public class DropBoxDirectoryCreatorAsync extends AsyncTask<Void, Long, Boolean> {
    private DropboxAPI<?> dropboxAPI;
    private String path;
    private Context context;
    private OnDirectoryCreateFinished listener;
    private String name;
    private String message;

    public DropBoxDirectoryCreatorAsync(Context context, DropboxAPI<?> dropboxAPI, String name, String path, OnDirectoryCreateFinished listener) {
        this.context = context;
        this.dropboxAPI = dropboxAPI;
        this.name = name;
        this.path = path;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            dropboxAPI.createFolder(path);
            message = AppConstant.FOLDER_CREATED;

        } catch(DropboxException e) {
            message = AppConstant.FOLDER_CREATE_ERROR;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if(result) {
            listener.onDirectoryCreateFinished(name);
            Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    public interface OnDirectoryCreateFinished {
        void onDirectoryCreateFinished(String dirName);
    }
}
