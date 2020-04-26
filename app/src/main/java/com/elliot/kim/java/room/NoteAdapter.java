package com.elliot.kim.java.room;

import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.java.room.R;
import com.android.java.room.databinding.CardViewBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder>
        implements Filterable {
    private final Context context;
    private List<Note> noteList;
    private List<Note> noteListFiltered;

    NoteAdapter(Context context, List<Note> notes) {
        this.context = context;
        this.noteList = notes;
        noteListFiltered = notes;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String searchWord = constraint.toString();
                if(searchWord.isEmpty()) {
                    noteListFiltered = noteList;
                } else {
                    List<Note> noteListFiltering = new ArrayList<>();
                    for(Note note : noteList) {
                        if (note.getTitle().toLowerCase().contains(searchWord.toLowerCase())) {
                            noteListFiltering.add(note);
                        }
                    }
                    noteListFiltered = noteListFiltering;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = noteListFiltered;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                noteListFiltered = (List<Note>)results.values;
                notifyDataSetChanged();
            }
        };
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener{
        CardViewBinding binding;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CardViewBinding.bind(itemView);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem edit = menu.add(Menu.NONE, 1001, 1, "노트펼치기");
            MenuItem setAlarm, cancelAlarm;
            if (noteListFiltered.get(getAdapterPosition()).getAlarmSet()) {
                setAlarm = menu.add(Menu.NONE, 1002, 2, "알림변경");
                cancelAlarm = menu.add(Menu.NONE, 1003, 3, "알림삭제");
                cancelAlarm.setOnMenuItemClickListener(menuItemClickListener);
            }
            else
                setAlarm = menu.add(Menu.NONE, 1002, 2, "알림설정");

            MenuItem delete = menu.add(Menu.NONE, 1004, 4, "삭제하기");
            MenuItem share = menu.add(Menu.NONE, 1005, 5, "공유하기");

            edit.setOnMenuItemClickListener(menuItemClickListener);
            setAlarm.setOnMenuItemClickListener(menuItemClickListener);
            delete.setOnMenuItemClickListener(menuItemClickListener);
            share.setOnMenuItemClickListener(menuItemClickListener);
        }

        private final MenuItem.OnMenuItemClickListener menuItemClickListener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Note note = noteList.get(getAdapterPosition());
                switch (item.getItemId()) {
                    case 1001:
                        ((MainActivity) context).onEditNoteFragmentStart(note);
                        break;
                    case 1002:
                        ((MainActivity) context).onAlarmFragmentStart(note);
                        break;
                    case 1003:
                        ((MainActivity) context).cancelAlarm(note.getNumber());
                        note.setAlarmSet(false);
                        break;
                    case 1004:
                        ((MainActivity) context).deleteNote(note.getNumber());
                        delete(getAdapterPosition());
                        break;
                    case 1005:
                        share(note);
                        break;
                }
                return true;
            }
        };
    }

    @NonNull
    @Override
    public NoteAdapter.NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view, parent, false);

        return new NoteViewHolder(frameLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.NoteViewHolder holder, final int position) {
        Note note = noteListFiltered.get(position);
        holder.binding.textViewTitle.setText(note.getTitle());
        holder.binding.textViewDate.setText(note.getDateEdit());

        holder.binding.cardView.setOnClickListener(v -> ((MainActivity) context).onEditNoteFragmentStart(note));
        holder.binding.cardView.setOnLongClickListener(v -> false);
    }

    @Override
    public int getItemCount() {
        return noteListFiltered == null ? 0 : noteListFiltered.size();
    }

    void insert(Note note) {
        this.noteList.add(note);
        notifyItemInserted(this.noteList.size() - 1);
    }

    private void delete(int position) {
        noteList.remove(position);
        notifyItemRemoved(position);
    }

    void delete(Note note) {
        int position = noteList.indexOf(note);
        noteList.remove(note);
        notifyItemRemoved(position);
    }

    void sort(int sortBy) {
        Collections.sort(noteList, (o1, o2) -> {
            switch (sortBy) {
                case 0:
                    return (int) (Long.parseLong(o2.getDateAdd().replaceAll("[^0-9]",""))
                                                    - Long.parseLong(o1.getDateAdd().replaceAll("[^0-9]","")));
                case 1:
                    return (int) (Long.parseLong(o2.getDateEdit().replaceAll("[^0-9]",""))
                            - Long.parseLong(o1.getDateEdit().replaceAll("[^0-9]","")));
                case 2:
                    return o1.getTitle().compareTo(o2.getTitle());
                default:
                    return 0;
            }
        });
    }

    void share(Note note) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");

        String text = note.toStringToShare();
        intent.putExtra(Intent.EXTRA_SUBJECT, "Cat Note\n");
        intent.putExtra(Intent.EXTRA_TEXT, text);

        Intent chooser = Intent.createChooser(intent, "공유하기");
        context.startActivity(chooser);
    }
}
