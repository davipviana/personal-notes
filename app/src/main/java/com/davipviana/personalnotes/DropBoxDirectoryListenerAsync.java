package com.davipviana.personalnotes;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Davi Viana on 25/02/2018.
 */

public class DropBoxDirectoryListenerAsync extends AsyncTask<Void, Long, Boolean> {
    private Context context;
    private DropboxAPI<?> dropboxAPI;
    private List<String> directoryList = new ArrayList<>();
    private String errorMessage;
    private String currentDirectory;
    private OnLoadFinished listener;

    public DropBoxDirectoryListenerAsync(Context context, DropboxAPI<?> dropboxAPI, String currentDirectory, OnLoadFinished listener) {
        this.context = context;
        this.dropboxAPI = dropboxAPI;
        this.currentDirectory = currentDirectory;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            errorMessage = null;
            DropboxAPI.Entry directoryEntry = dropboxAPI.metadata(currentDirectory, 1000, null, true, null);
            if(!directoryEntry.isDir || directoryEntry.contents == null) {
                errorMessage = "File or empty directory";
                return false;
            }
            for (DropboxAPI.Entry entry : directoryEntry.contents) {
                if(entry.isDir) {
                    directoryList.add(entry.fileName());
                }
            }


        } catch(DropboxUnlinkedException e) {
            errorMessage = "Authentication dropbox error!";
        } catch(DropboxPartialFileException e) {
            errorMessage = "Download canceled";
        } catch(DropboxServerException e) {
            errorMessage = "Network error, try again";
        } catch(DropboxParseException e) {
            errorMessage = "Dropbox Parse excepton, try again";
        } catch(DropboxException e) {
            errorMessage = "Unknown Dropbox error, try again";

        }

        if(errorMessage != null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(result) {
            listener.onLoadFinished(directoryList);
        } else {
            showToast(errorMessage);
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        error.show();
    }



    public interface OnLoadFinished {
        void onLoadFinished(List<String> values);
    }

}
