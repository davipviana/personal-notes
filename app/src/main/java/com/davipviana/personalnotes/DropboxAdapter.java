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

public class DropboxAdapter extends RecyclerView.Adapter<DropboxAdapter.RecyclerViewHolder> {
    private LayoutInflater layoutInflater;
    private List<String> data = Collections.emptyList();

    public DropboxAdapter(Context context, List<String> data) {
        layoutInflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = layoutInflater.inflate(R.layout.custom_dbx_adapter_layout, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        holder.title.setText(data.get(position));
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public void add(String dirName) {
        data.add(dirName);
        Collections.sort(data, String.CASE_INSENSITIVE_ORDER);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.drop_box_directory_name);
        }
    }
}
