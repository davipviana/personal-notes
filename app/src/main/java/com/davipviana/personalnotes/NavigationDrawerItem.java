package com.davipviana.personalnotes;

/**
 * Created by Davi Viana on 24/02/2018.
 */

public class NavigationDrawerItem {
    private int iconId;
    private String title;

    public NavigationDrawerItem(int iconId, String title) {
        this.iconId = iconId;
        this.title = title;
    }

    public int getIconId() {
        return iconId;
    }

    public String getTitle() {
        return title;
    }
}
