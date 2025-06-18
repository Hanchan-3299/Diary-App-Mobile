package com.example.diaryapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.diaryapp.auth.LoginActivity;
import com.example.diaryapp.note.DetailActivity;
import com.example.diaryapp.note.Note;
import com.example.diaryapp.note.NoteActivity;
import com.example.diaryapp.note.NoteAdapter;
import com.example.diaryapp.note.NoteHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView btnLogout;
    FirebaseAuth mAuth;

    RecyclerView recyclerView;
    TextView noNotesText;
    List<Note> noteList;
    NoteAdapter adapter;
    DatabaseReference notesRef;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //inisialisasi
        mAuth = FirebaseAuth.getInstance();
        btnLogout = findViewById(R.id.btnMainExit);

        recyclerView = findViewById(R.id.noteRecyclerView);
        noNotesText = findViewById(R.id.txtMainNoDiaryText);
        ImageView addNote = findViewById(R.id.btnMainAdd);

        //ambil id user yang udh login
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        notesRef = FirebaseDatabase.getInstance().getReference("notes");

        noteList = new ArrayList<>();
        adapter = new NoteAdapter(this, noteList, note -> {

            //intent ke detail activity
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("title", note.title);
            intent.putExtra("message", note.message);
            intent.putExtra("date", note.date);
            intent.putExtra("imageBase64", note.imageBase64);
            intent.putExtra("locationName", note.locationName);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        NoteHelper.loadNotes(notesRef, currentUserId, noteList, adapter, noNotesText, MainActivity.this);



        //add notes
        addNote.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, NoteActivity.class));
        });

        //logout
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}