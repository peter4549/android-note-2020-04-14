package com.android.java.room;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> notes;
    private final Context context;
    private int position;

    public NoteAdapter(List<Note> notes, Context context) {
        this.notes = notes;
        this.context = context;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener{
        CardView cardView;
        TextView textViewNumber;
        TextView textViewDate;
        TextView textViewNote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            textViewNumber = itemView.findViewById(R.id.text_view_number);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            textViewNote = itemView.findViewById(R.id.text_view_note);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem edit = menu.add(Menu.NONE, 1001, 1, "노트펼치기");
            MenuItem setAlarm = menu.add(Menu.NONE, 1002, 2, "알림설정");
            MenuItem delete = menu.add(Menu.NONE, 1003, 3, "삭제하기");
            edit.setOnMenuItemClickListener(menuItemClickListener);
            setAlarm.setOnMenuItemClickListener(menuItemClickListener);
            delete.setOnMenuItemClickListener(menuItemClickListener);
        }

        private final MenuItem.OnMenuItemClickListener menuItemClickListener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1001:
                        ((MainActivity) context).onFragmentChanged(0, notes.get(getAdapterPosition()));
                        break;
                    case 1002:


                        break;
                    case 1003:
                        ((MainActivity) context).onItemDelete(notes.get(getAdapterPosition()).getNumber());
                        delete(getAdapterPosition());
                        break;
                }
                return true;
            }
        };
    }

    @NonNull
    @Override
    public NoteAdapter.NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view, parent, false);
        return new NoteViewHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.NoteViewHolder holder, final int position) {
        Note newsData = notes.get(position);
        holder.textViewNumber.setText("No. " + newsData.getNumber());
        holder.textViewDate.setText(newsData.getDate());
        holder.textViewNote.setText(newsData.getNote());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).onFragmentChanged(0, notes.get(position));
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes == null ? 0 : notes.size();
    }

    public void insert(List<Note> notes) {
        for (Note note : notes) {
            if(!this.notes.contains(note)) {
                this.notes.add(note);
                notifyItemInserted(this.notes.size() - 1);
            }
        }
    }

    public void delete(int position) {
        notes.remove(position);
        notifyItemRemoved(position);
    }
}
