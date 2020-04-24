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
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.java.room.R;
import com.android.java.room.databinding.CardViewBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder>
        implements Filterable {
    private List<Note> notes;
    private List<Note> notesFiltered;
    private final Context context;
    private int position;

    public NoteAdapter(List<Note> notes, Context context) {
        this.notes = notes;
        notesFiltered = notes;
        this.context = context;
    }

    private void setPosition(int position) {
        this.position = position;
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
            MenuItem share = menu.add(Menu.NONE, 1005, 5, "공유하기");

            edit.setOnMenuItemClickListener(menuItemClickListener);
            setAlarm.setOnMenuItemClickListener(menuItemClickListener);
            delete.setOnMenuItemClickListener(menuItemClickListener);
            cancelAlarm.setOnMenuItemClickListener(menuItemClickListener);
            share.setOnMenuItemClickListener(menuItemClickListener);
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
                        break;
                    case 1005:
                        share();
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
        setPosition(position);
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

    void insert(Note note) {
        this.notes.add(note);
        notifyItemInserted(this.notes.size() - 1);
    }

    private void delete(int position) {
        notes.remove(position);
        notifyItemRemoved(position);
    }

    void sort(int sortBy) {
        Collections.sort(notes, new Comparator<Note>() {
            @Override
            public int compare(Note o1, Note o2) {
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
            }
        });
        notifyDataSetChanged();
    }

    void share() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
// Set default text message
// 카톡, 이메일, MMS 다 이걸로 설정 가능
//String subject = "문자의 제목";
        String text = notes.get(position).toStringToShare();
        intent.putExtra(Intent.EXTRA_SUBJECT, "catpaws\n");
        intent.putExtra(Intent.EXTRA_TEXT, text);

// Title of intent
        Intent chooser = Intent.createChooser(intent, "친구에게 공유하기");
        context.startActivity(chooser);

    }
}
