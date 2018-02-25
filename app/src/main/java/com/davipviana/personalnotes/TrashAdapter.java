package com.davipviana.personalnotes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by Davi Viana on 25/02/2018.
 */

public class TrashAdapter extends RecyclerView.Adapter<TrashAdapter.NoteHolder> {
    private LayoutInflater layoutInflater;
    private List<Trash> data = Collections.emptyList();

    public TrashAdapter(Context context, List<Trash> data) {
        layoutInflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.custom_trash_archive_adapter_layout, parent, false);
        return new NoteHolder(view);
    }

    @Override
    public void onBindViewHolder(NoteHolder holder, int position) {
        holder.id.setText(data.get(position).getId()+"");
        holder.title.setText(data.get(position).getTitle());
        holder.description.setText(data.get(position).getDescription());
        holder.date.setText(data.get(position).getDateTime());
    }
    public void setData(List<Trash> data) {
        this.data = data;    }

    @Override
    public int getItemCount() {
        return data.size();    }

    public void delete(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    public class NoteHolder extends RecyclerView.ViewHolder {

        TextView title, description, date,id;

        public NoteHolder(View itemView) {
            super(itemView);
            id = (TextView) itemView.findViewById(R.id.id_note_custom_home);
            title = (TextView) itemView.findViewById(R.id.title_note_custom_home);
            description = (TextView) itemView.findViewById(R.id.description_note_custom_home);
            date = (TextView) itemView.findViewById(R.id.date_time_note_custom_home);
        }
    }
}
