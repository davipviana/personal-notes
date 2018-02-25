package com.davipviana.personalnotes;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Davi Viana on 25/02/2018.
 */

public class DropBoxImageUploadAsync extends AsyncTask<Void, Long, Boolean> {
    private DropboxAPI<?> dropboxAPI;
    private String path;
    private File file;
    private String filename;

    public DropBoxImageUploadAsync(Context context, DropboxAPI<?> dropboxAPI, File file, String filename) {
        this.dropboxAPI = dropboxAPI;
        this.file = file;
        this.filename = filename;
        this.path = AppSharedPreferences.getDropBoxUploadPath(context);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String errorMessage;
        try {
            FileInputStream fis = new FileInputStream(file);
            String path = this.path + "/" + filename;
            DropboxAPI.UploadRequest request = dropboxAPI.putFileOverwriteRequest(path, fis, file.length(),
                    new ProgressListener() {

                        @Override
                        public long progressInterval() {
                            return 500;
                        }

                        @Override
                        public void onProgress(long bytes, long total) {
                            publishProgress(bytes);
                        }
                    });

            if(request != null) {
                request.upload();
                return true;
            }

        } catch(DropboxException e) {
            errorMessage = "Dropbox exception";
            // DropboxUnlinkedException, DropboxFileSizeException,DropboxPartialFileException,DropboxServerException
        } catch(FileNotFoundException e) {
            errorMessage = "File not found exception";
        }

        return false;
    }
}