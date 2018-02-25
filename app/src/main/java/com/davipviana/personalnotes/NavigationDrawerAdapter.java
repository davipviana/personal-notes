package com.davipviana.personalnotes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Davi Viana on 24/02/2018.
 */

public class NavigationDrawerAdapter extends BaseAdapter {
    private List<NavigationDrawerItem> drawerItems;
    private LayoutInflater layoutInflater;

    public NavigationDrawerAdapter(Context context, List<NavigationDrawerItem> drawerItems) {
        super();
        this.drawerItems = drawerItems;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return this.drawerItems.size();
    }

    @Override
    public Object getItem(int position) {
        // Not used in this application
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = this.layoutInflater.inflate(R.layout.custom_navigation_drawer, null);
        NavigationDrawerItem navigationDrawerItem = this.drawerItems.get(position);

        TextView title = (TextView) convertView.findViewById(R.id.navigation_item_title);
        title.setText(navigationDrawerItem.getTitle());

        ImageView icon = (ImageView) convertView.findViewById(R.id.navigation_item_icon);
        icon.setImageResource(navigationDrawerItem.getIconId());

        return convertView;
    }
}
