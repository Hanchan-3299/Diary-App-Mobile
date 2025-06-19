package com.example.diaryapp.note;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.diaryapp.MainActivity;
import com.example.diaryapp.R;
import com.example.diaryapp.auth.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.play.core.integrity.IntegrityTokenRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class DetailActivity extends AppCompatActivity {
    ImageView detailImageView, btnUpdate, btnDelete;
    TextView detailTitle, detailMessage, detailDate, detailLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);

        //inisialisasi
        detailTitle = findViewById(R.id.txtDetailTitle);
        detailMessage = findViewById(R.id.multiTxtDetailMessage);
        detailDate = findViewById(R.id.txtDetailDate);
        detailImageView = findViewById(R.id.imgDetailNote);
        detailLocation = findViewById(R.id.txtDetailLoc);

        btnUpdate = findViewById(R.id.btnDetailEdit);
        btnDelete = findViewById(R.id.btnDetailDelete);

        //ambil value dari intent
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        String date = intent.getStringExtra("date");
        String imageBase64 = intent.getStringExtra("imageBase64");
        String locationName = intent.getStringExtra("locationName");

        //set ke view detail
        detailTitle.setText(title);
        detailMessage.setText(message);
        detailDate.setText(date);
        detailImageView.setImageBitmap(ImageHelper.decodeBase64ToBitmap(imageBase64));
        detailLocation.setText(locationName);
        if (locationName == null || locationName.isEmpty() || locationName.equals("Unknown Location")){
            detailLocation.setCompoundDrawablePadding(-55);
            detailLocation.setText("Unknown Location");
        }else{
            detailLocation.setText(locationName);
        }
        //scrollbar
        detailMessage.setMovementMethod(new android.text.method.ScrollingMovementMethod());

        //back button
        ImageView btnBack = findViewById(R.id.btnDetailExit);
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(DetailActivity.this, MainActivity.class));
            finish();
        });


        //gmaps pas ngeklik
        if (locationName != null && !locationName.equals("Unknown Location")){
            detailLocation.setOnClickListener(v ->{
                String mapQuery = "geo:0,0?q=" + locationName;
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(mapQuery));
                mapIntent.setPackage("com.google.android.apps.maps");

                if (mapIntent.resolveActivity(getPackageManager()) != null){
                    startActivity(mapIntent);
                }else {
                    Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=" + locationName));
                    startActivity(fallbackIntent);
                    Toast.makeText(this, "Location is not found", Toast.LENGTH_SHORT).show();
                }
            });
        }

        //inisialisasi noteId
        String noteId = intent.getStringExtra("noteId");
        String curentUserId = intent.getStringExtra("userId");

        // delete note
        btnDelete.setOnClickListener(v -> {
            if (noteId != null){
                String[] options = {"Yes", "No"};
                new AlertDialog.Builder(this)
                        .setTitle("Are you sure want to Delete this Note ?")
                        .setItems(options, (dialog, which) -> {
                            if (which == 0){
                                DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("notes").child(Objects.requireNonNull(curentUserId));
                                notesRef.orderByChild("noteId").equalTo(noteId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()){
                                                    for (DataSnapshot snap : snapshot.getChildren()){
                                                        snap.getRef().removeValue();
                                                    }
                                                    Toast.makeText(DetailActivity.this, "Note is successfully deleted", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(DetailActivity.this, MainActivity.class));
                                                    finish();
                                                }else {
                                                    Toast.makeText(DetailActivity.this, "Failed to delete note", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(DetailActivity.this, "Failed to delete note", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }else {
                                Toast.makeText(this, "Delete Canceled", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }).show();
            }
        });

        //update button
        btnUpdate.setOnClickListener(v -> {
            Intent editIntent = new Intent(DetailActivity.this, NoteActivity.class);
            editIntent.putExtra("noteId", noteId);
            editIntent.putExtra("title", title);
            editIntent.putExtra("message", message);
            editIntent.putExtra("imageBase64", imageBase64);
            Log.d("DetailActivity", "Sending noteId: " + noteId);
            startActivity(editIntent);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}