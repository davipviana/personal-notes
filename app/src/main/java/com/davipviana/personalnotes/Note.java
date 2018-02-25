package com.davipviana.personalnotes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Davi Viana on 24/02/2018.
 */
public class Note {
    private String title, description, date, time, imagePath, type;
    private int id;
    private boolean hasNoImage = false;
    private int storageSelection;

    // Contains the image (if any) attached to this note
    private Bitmap bitmap;

    public Note(String title, String description, String date, String time, int id, int storageSelection, String type) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.id = id;
        this.storageSelection = storageSelection;
        this.type = type;
    }

    // Create note from a reminderString (contains contents in a single string
    // delimited by a $ sign - see convertToString method later which
    // creates this.
    public Note(String reminderString) {
        // using \\ before a character tells the function
        // to NOT treat the character as a special regular expression
        // $ is normally interpreted as end of line or end of string
        String[] fields = reminderString.split("\\$");
        this.type = fields[0];
        this.id = Integer.parseInt(fields[1]);
        this.title = fields[2];
        this.date = fields[5];
        this.time = fields[3];
        this.imagePath = fields[4];
        this.storageSelection = Integer.parseInt(fields[6]);
        if (type.equals(AppConstant.NORMAL)) {
            this.description = fields[7];
            Note aNote = new Note(this.title, this.description, this.date, this.time, this.id, this.storageSelection, this.type);
            // Previous constructor does not set this, so we do it manually after invoking
            // the constructor
            aNote.setImagePath(this.imagePath);
        } else {
            String list = "";
            for(int i = 7;i<fields.length;i++)
                list = list+fields[i];
            this.description = list;
        }
    }

    public String convertToString() {
        return type + "$"
                + id + "$"
                + title + "$"
                + time + "$"
                + imagePath + "$"
                + date + "$"
                + storageSelection + "$"
                + description;
    }

    public void setBitmap(String path) {
        setImagePath(path);
        this.bitmap = BitmapFactory.decodeFile(path);
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isHasNoImage() {
        return hasNoImage;
    }

    public void setHasNoImage(boolean hasNoImage) {
        this.hasNoImage = hasNoImage;
    }

    public int getStorageSelection() {
        return storageSelection;
    }

    public void setStorageSelection(int storageSelection) {
        this.storageSelection = storageSelection;
    }
}
