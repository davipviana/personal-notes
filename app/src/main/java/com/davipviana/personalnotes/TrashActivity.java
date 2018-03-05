package com.davipviana.personalnotes;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Davi Viana on 25/02/2018.
 */

public class TrashActivity extends BaseActivity implements
        LoaderManager.LoaderCallbacks<List<Trash>> {
    private static final int LOADER_ID = 1;
    private RecyclerView recyclerView;
    private TrashAdapter trashAdapter;
    private ContentResolver contentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_layout);
        toolBar = activateToolbar();
        setUpNavigationDrawer();
        setUpRecyclerView();
        removeActions();
    }

    private void setUpRecyclerView() {
        contentResolver = getContentResolver();
        getSupportLoaderManager().initLoader(LOADER_ID, null, TrashActivity.this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_home);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        trashAdapter = new TrashAdapter(TrashActivity.this, new ArrayList<Trash>());
        recyclerView.setAdapter(trashAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(final View view, final int position) {
                PopupMenu popup = new PopupMenu(TrashActivity.this, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.actions_archive_delete, popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.action_delete_archive) {
                            delete(view, position);
                        }
                        return false;
                    }
                });
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));
    }

    private void delete(View view, int position) {
        String _ID = ((TextView) view.findViewById(R.id.id_note_custom_home)).getText().toString();
        Uri uri = TrashContract.Trash.buildTrashUri(_ID);
        contentResolver.delete(uri, null, null);
        trashAdapter.delete(position);
        changeNoItemTag();
    }

    private void changeNoItemTag() {
        if (trashAdapter.getItemCount() != 0) {
            TextView noItemTV = (TextView) findViewById(R.id.no_item_textview);
            noItemTV.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            TextView noItemTV = (TextView) findViewById(R.id.no_item_textview);
            noItemTV.setText(AppConstant.NO_ARCHIVES);
            noItemTV.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public Loader<List<Trash>> onCreateLoader(int id, Bundle args) {
        contentResolver = getContentResolver();
        return new TrashLoader(TrashActivity.this, contentResolver);
    }

    @Override
    public void onLoadFinished(Loader<List<Trash>> loader, List<Trash> data) {
        trashAdapter.setData(data);
        changeNoItemTag();
    }

    @Override
    public void onLoaderReset(Loader<List<Trash>> loader) {
        trashAdapter.setData(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_trash_empty, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_empty_trash) {
            new AppDatabase(getApplicationContext()).emptyTrash();
            trashAdapter.setData(new ArrayList<Trash>());
            trashAdapter.notifyDataSetChanged();
            showToast(AppConstant.EMPTY_TRASH);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
