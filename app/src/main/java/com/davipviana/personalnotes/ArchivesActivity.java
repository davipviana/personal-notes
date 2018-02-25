package com.davipviana.personalnotes;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Davi Viana on 25/02/2018.
 */

public class ArchivesActivity extends BaseActivity
        implements LoaderManager.LoaderCallbacks<List<Archive>>
{
    private static final int LOADER_ID = 1;
    private RecyclerView recyclerView;
    private ArchivesAdapter archivesAdapter;
    private ContentResolver contentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_layout);
        toolBar = activateToolbar();
        setUpNavigationDrawer();
        setUpRecycleView();
        removeActions();
    }

    private void setUpRecycleView() {
        contentResolver = getContentResolver();
        getSupportLoaderManager().initLoader(LOADER_ID, null, ArchivesActivity.this);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_home);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        archivesAdapter = new ArchivesAdapter(ArchivesActivity.this, new ArrayList<Archive>());
        recyclerView.setAdapter(archivesAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(final View view, final int position) {
                PopupMenu popup = new PopupMenu(ArchivesActivity.this, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.actions_archive_delete, popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuitem) {
                        if (menuitem.getItemId() == R.id.action_delete_archive) {
                            moveToDeleted();
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

    private void moveToDeleted() {
        ContentValues values = new ContentValues();
        TextView title = (TextView) findViewById(R.id.title_note_custom_home);
        TextView description = (TextView) findViewById(R.id.description_note_custom_home);
        TextView dateTime = (TextView) findViewById(R.id.date_time_note_custom_home);

        values.put(TrashContract.TrashColumns.TRASH_TITLE, title.getText().toString());
        values.put(TrashContract.TrashColumns.TRASH_DESCRIPTION, description.getText().toString());
        values.put(TrashContract.TrashColumns.TRASH_DATE_TIME, dateTime.getText().toString());
        ContentResolver cr = getContentResolver();
        Uri uri = TrashContract.URI_TABLE;
        cr.insert(uri, values);
    }

    @Override
    public Loader<List<Archive>> onCreateLoader(int id, Bundle args) {
        contentResolver = getContentResolver();
        return new ArchivesLoader(ArchivesActivity.this, contentResolver);
    }

    @Override
    public void onLoadFinished(Loader<List<Archive>> loader, List<Archive> data) {
        archivesAdapter.setData(data);
        changeNoItemTag();
    }

    @Override
    public void onLoaderReset(Loader<List<Archive>> loader) {
        archivesAdapter.setData(null);
    }

    private void changeNoItemTag() {
        if (archivesAdapter.getItemCount() != 0) {
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

    private void delete(View view, int position) {
        String _ID = ((TextView) view.findViewById(R.id.id_note_custom_home)).getText().toString();
        Uri uri = ArchivesContract.Archives.buildArchiveUri(_ID);
        contentResolver.delete(uri, null, null);
        archivesAdapter.delete(position);
        changeNoItemTag();
    }




}
