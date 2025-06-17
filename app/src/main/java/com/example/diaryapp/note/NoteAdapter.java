package com.example.diaryapp.note;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diaryapp.R;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    List<Note> noteList;
    Context context;
    onNoteClickListener listener;


    public NoteAdapter(Context context, List<Note> noteList, onNoteClickListener listener){
        this.context = context;
        this.noteList = noteList;
        this.listener = listener;
    }

    public interface onNoteClickListener{
        void onNoteClick(Note note);
    }



    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.title.setText(note.title);
        holder.date.setText(note.date);
        holder.thumbnail.setImageBitmap(ImageHelper.decodeBase64ToBitmap(note.imageBase64));

        holder.itemView.setOnClickListener(v -> {
            listener.onNoteClick(note);
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }


    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title, date;
        ImageView thumbnail;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.noteTitle);
            date = itemView.findViewById(R.id.noteDate);
            thumbnail = itemView.findViewById(R.id.noteThumbnail);
        }
    }
}
