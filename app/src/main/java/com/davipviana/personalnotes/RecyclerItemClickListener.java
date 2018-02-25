package com.davipviana.personalnotes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Davi Viana on 25/02/2018.
 */

public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

    public static interface OnItemClickListener
    {
        public void onItemClick(View view, int position);
        public void onItemLongClick(View view, int position);
    }

    private OnItemClickListener listener;
    private GestureDetector gestureDetector;

    public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
        this.listener = listener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            public void onLongPress(MotionEvent motionEvent) {
                View childView = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                if(childView != null && RecyclerItemClickListener.this.listener != null) {
                    RecyclerItemClickListener.this.listener.onItemLongClick(childView, recyclerView.getChildAdapterPosition(childView));
                }

            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
        View childView = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
        if(childView != null && listener != null && gestureDetector.onTouchEvent(motionEvent)) {
            listener.onItemClick(childView, recyclerView.getChildAdapterPosition(childView));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent){
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
}
