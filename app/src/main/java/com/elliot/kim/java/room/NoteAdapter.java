package com.elliot.kim.java.room;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.java.room.R;
import com.android.java.room.databinding.CardViewBinding;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder>
        implements Filterable {
    private List<Note> notes;
    private List<Note> notesFiltered;
    private final Context context;

    public NoteAdapter(List<Note> notes, Context context) {
        this.notes = notes;
        notesFiltered = notes;
        this.context = context;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String searchWord = constraint.toString();
                if(searchWord.isEmpty()) {
                    notesFiltered = notes;
                } else {
                    List<Note> notesFiltering = new ArrayList<>();
                    for(Note note : notes) {
                        if (note.getTitle().toLowerCase().contains(searchWord.toLowerCase())) {
                            notesFiltering.add(note);
                        }
                    }
                    notesFiltered = notesFiltering;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = notesFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notesFiltered = (List<Note>)results.values;
                notifyDataSetChanged();
            }
        };
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener{
        CardViewBinding binding;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CardViewBinding.bind(itemView);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem edit = menu.add(Menu.NONE, 1001, 1, "노트펼치기");
            MenuItem setAlarm = menu.add(Menu.NONE, 1002, 2, "알림설정");
            MenuItem delete = menu.add(Menu.NONE, 1003, 3, "삭제하기");
            MenuItem cancelAlarm = menu.add(Menu.NONE, 1004, 4, "알람해제");

            edit.setOnMenuItemClickListener(menuItemClickListener);
            setAlarm.setOnMenuItemClickListener(menuItemClickListener);
            delete.setOnMenuItemClickListener(menuItemClickListener);
            cancelAlarm.setOnMenuItemClickListener(menuItemClickListener);
        }

        private final MenuItem.OnMenuItemClickListener menuItemClickListener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1001:
                        ((MainActivity) context).onEditNoteFragmentStart(notes.get(getAdapterPosition()));
                        break;
                    case 1002:
                        ((MainActivity) context).onAlarmFragmentStart(notes.get(getAdapterPosition()));
                        break;
                    case 1003:
                        ((MainActivity) context).onItemDelete(notes.get(getAdapterPosition()).getNumber());
                        delete(getAdapterPosition());
                        break;
                    case 1004:
                        ((MainActivity) context).cancelAlarm(notes.get(getAdapterPosition()).getNumber());
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
        Note note = notesFiltered.get(position);
        holder.binding.textViewTitle.setText(note.getTitle());
        holder.binding.textViewDate.setText(note.getDateEdit());

        holder.binding.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).onEditNoteFragmentStart(notesFiltered.get(position));
            }
        });

        holder.binding.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notesFiltered == null ? 0 : notesFiltered.size();
    }

    public void insert(Note note) {
        this.notes.add(note);
        notifyItemInserted(this.notes.size() - 1);
    }

    public void delete(int position) {
        notes.remove(position);
        notifyItemRemoved(position);
    }
}
