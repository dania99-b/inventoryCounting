package com.example.inventorycountingsystem.Activities;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.inventorycountingsystem.R;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private Cursor cursor;
    private String columnName; // Add a field to store the column name in the adapter


    public ItemAdapter(Cursor cursor,String columnName) {
        this.cursor = cursor;
        this.columnName = columnName;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item layout and create a ViewHolder
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false); // Replace with your item layout
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) {
            // Couldn't move to the position in the cursor, return.
            return;
        }

        // Extract data from the cursor
         @SuppressLint("Range") String item = cursor.getString(cursor.getColumnIndex(columnName)); // Replace "column_name" with the actual column name

        // Bind data to the views within each item
        holder.bind(item);
    }

    @Override
    public int getItemCount() {

        if (cursor != null) {
            return cursor.getCount();
        }
        return 0;
    }

    @SuppressLint("SuspiciousIndentation")
    public void addItem(String item) {
        // Add the new item to the dataset
         // This will refresh the entire dataset based on the cursor's updated data.

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView itemNameTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView); // Replace with your item TextView ID
        }

        public void bind(String item) {
            // Bind data to views
            itemNameTextView.setText(item);
        }
    }
    public void updateCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
    }
}
