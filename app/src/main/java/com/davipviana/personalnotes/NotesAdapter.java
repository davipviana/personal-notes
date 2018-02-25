package com.davipviana.personalnotes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by Davi Viana on 24/02/2018.
 */

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteHolder> {

    private LayoutInflater layoutInflater;
    private List<Note> notes = Collections.emptyList();
    private Context context;

    public NotesAdapter(Context context, List<Note> notes) {
        layoutInflater = LayoutInflater.from(context);
        this.notes = notes;
        this.context = context;
    }

    // Called when the RecyclerView needs a new RecyclerView.ViewHolder (NoteHolder)
    // to represent an item.  We inflate the XML layout and return our view (NoteHolder)
    @Override
    public NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_notes_adapter_layout, parent, false);
        return new NoteHolder(view);
    }

    // Called by RecyclerView to display the data (a note) at the specified position.
    // This method needs to update the contents of the view to reflect the item at the
    // given position e.g. we are updating the view here with the data
    @Override
    public void onBindViewHolder(NoteHolder holder, int position) {
        holder.mId.setText(notes.get(position).getId() + "");
        holder.mTitle.setText(notes.get(position).getTitle());
        // We have to deal with a note or a note list here, so
        // check for both types and process accordingly
        if (notes.get(position).getType().equals(AppConstant.LIST)) {
            NoteCustomList noteCustomList = new NoteCustomList(context);
            noteCustomList.setUpForHomeAdapter(notes.get(position).getDescription());
            holder.mLinearLayout.removeAllViews();
            holder.mLinearLayout.addView(noteCustomList);
            holder.mDescription.setVisibility(View.GONE);
        } else {
            // Not a list note, so hide it.
            holder.mLinearLayout.setVisibility(View.GONE);
            holder.mDescription.setText(notes.get(position).getDescription());
        }
        holder.mDate.setText(notes.get(position).getDate() + " " + notes.get(position).getTime());

        // Display an image, but only if we have one to display.
        if (notes.get(position).getBitmap() != null) {
            holder.mImage.setImageBitmap(notes.get(position).getBitmap());
            holder.mImage.setVisibility(View.VISIBLE);
        } else if (notes.get(position).getImagePath().equals(AppConstant.NO_IMAGE)) {
            // No image, so hide.
            holder.mImage.setVisibility(View.GONE);
        } else {
            // Just in case...
            holder.mImage.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void setData(List<Note> notes) {
        this.notes = notes;
    }

    public void delete(int position) {
        notes.remove(position);
        notifyItemRemoved(position);
    }

    // This will be called when an image is obtained from
    // Google Drive or Dropbox, as it will happen at a timeframe
    // after the non image data has been changed.  So we need to
    // notify the recyclerview of a change of data
    public void notifyImageObtained() {
        notifyDataSetChanged();
    }

    // Simple nested class that holds the various view components for the adapter
    // and as specified in custom_notes_adapter_layout.xml which we just created
    public class NoteHolder extends RecyclerView.ViewHolder {

        TextView mTitle, mDescription, mDate, mId;
        ImageView mImage;
        LinearLayout mLinearLayout;

        public NoteHolder(View itemView) {
            super(itemView);
            mId = (TextView) itemView.findViewById(R.id.id_note_custom_home);
            mTitle = (TextView) itemView.findViewById(R.id.title_note_custom_home);
            mDescription = (TextView) itemView.findViewById(R.id.description_note_custom_home);
            mLinearLayout = (LinearLayout) itemView.findViewById(R.id.home_list);
            mDate = (TextView) itemView.findViewById(R.id.date_time_note_custom_home);
            mImage = (ImageView) itemView.findViewById(R.id.image_note_custom_home);
        }
    }

}
