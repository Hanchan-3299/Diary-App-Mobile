package com.example.diaryapp.note;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.diaryapp.auth.AuthHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteHelper {

    //save note ke database
    public static void saveNoteToDatabase(String titleParam, String messageParam, Bitmap imageBitmapParam, String userId, DatabaseReference notesRef, int dstWidth, int dstHeight, Activity activity){
        String noteId = notesRef.push().getKey();
        String title = titleParam;
        String message = messageParam;
        Bitmap imageBitmap = imageBitmapParam;
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        if (title.isEmpty() || message.isEmpty()){
            Toast.makeText(activity, "Title & Message must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        if(imageBitmap == null) {
            Toast.makeText(activity, "Pilih gambar dulu", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, dstWidth, dstHeight, true);
        String encodedImage = ImageHelper.encodeImageToBase64(scaledBitmap, 50);

        Note note = new Note(noteId, title, message, date, encodedImage, userId);
        notesRef.child(userId).push().setValue(note)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(activity, "Note Saved", Toast.LENGTH_SHORT).show();
                    activity.finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(activity, "Failed to Save Note", Toast.LENGTH_SHORT).show();
                });

    }


    //load note ke recycler
    public static void loadNotes(DatabaseReference notesRef, String currentUserId, List<Note> noteList, NoteAdapter adapter, TextView noNoteText, Activity activity){
        notesRef.child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        noteList.clear();
                        for (DataSnapshot noteSnapshot : snapshot.getChildren()){
                            Note note = noteSnapshot.getValue(Note.class);
                            if (note != null){
                                noteList.add(note);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        noNoteText.setText("MyDiary");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(activity, "Failed to Load Data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
