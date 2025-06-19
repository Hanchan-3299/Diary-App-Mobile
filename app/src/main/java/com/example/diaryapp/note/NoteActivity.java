package com.example.diaryapp.note;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.diaryapp.MainActivity;
import com.example.diaryapp.R;
import com.example.diaryapp.auth.AuthHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yalantis.ucrop.UCrop;

import java.io.IOException;
import java.util.Objects;

public class NoteActivity extends AppCompatActivity {
    EditText etTitle, etMessage;
    ImageView imageView;
    Bitmap imageBitmap;
    DatabaseReference notesRef;
    String userId, noteId;
    boolean isUpdateMode = false;

    Button btnSave;
    ProgressBar progressBar;
    FusedLocationProviderClient fusedLocationClient;
    boolean locationResolved = false;
    Handler timeOutHandler;
    Runnable timeOutRunnable;

    static final int REQUEST_CAMERA = 100;
    static final int REQUEST_GALLERY = 200;
    static final int CAMERA_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_note);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthHelper.loginChecker(NoteActivity.this, user);

        userId = user.getUid();

        etTitle = findViewById(R.id.etNoteTitle);
        etMessage = findViewById(R.id.multiEtNoteBodyDiary);
        imageView = findViewById(R.id.imgDetailNote);
        btnSave = findViewById(R.id.btnNotePost);

        notesRef = FirebaseDatabase.getInstance().getReference("notes");
        userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        //update mode
        Intent intent = getIntent();
        noteId = intent.getStringExtra("noteId");
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        String imageBase64 = intent.getStringExtra("imageBase64");
        Log.d("NoteActivity", "Received noteId: " + noteId);

        isUpdateMode = (noteId != null);
        if (isUpdateMode){
            btnSave.setText("Update");
            etTitle.setText(title);
            etMessage.setText(message);

            if (imageBase64 != null) {
                byte[] decoded = Base64.decode(imageBase64, Base64.DEFAULT);
                imageBitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                imageView.setImageBitmap(imageBitmap);
            }
        }

        //pilih gambar
        imageView.setOnClickListener(v -> {
            ImageHelper.showImagePickerDialog(NoteActivity.this);
        });

        //save notes
        btnSave.setOnClickListener(v -> {
            AuthHelper.loginChecker(NoteActivity.this, user);
            userId = user.getUid();

            String inputTitle = etTitle.getText().toString().trim();
            String inputMessage = etMessage.getText().toString().trim();

            // Fallback ke hint jika kosong
            if (inputTitle.isEmpty()) {
                inputTitle = etTitle.getHint() != null ? etTitle.getHint().toString() : "";
            }
            if (inputMessage.isEmpty()) {
                inputMessage = etMessage.getHint() != null ? etMessage.getHint().toString() : "";
            }

            if (imageBitmap == null) {
                Toast.makeText(NoteActivity.this, "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isUpdateMode && noteId != null) {
                String[] options = {"Yes", "No"};
                String finalInputTitle = inputTitle;
                String finalInputMessage = inputMessage;
                new AlertDialog.Builder(this)
                        .setTitle("Update Note ?")
                        .setItems(options, (dialog, which) -> {
                            if (which == 0){
                                // Update mode
                                NoteHelper.getLastLocationAndSaveNote(
                                        fusedLocationClient, noteId, finalInputTitle, finalInputMessage, imageBitmap, userId,
                                        notesRef, 640, 480, NoteActivity.this);
                            }else {
                                Toast.makeText(this, "Update Canceled", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }).show();
            }else{
                String[] options = {"Yes", "No"};
                String finalInputTitle = inputTitle;
                String finalInputMessage = inputMessage;
                new AlertDialog.Builder(this)
                        .setTitle("Post Note ?")
                        .setItems(options, (dialog, which) -> {
                            if (which == 0){
                                NoteHelper.getLastLocationAndSaveNote(fusedLocationClient, null, finalInputTitle, finalInputMessage, imageBitmap, userId, notesRef, 640, 480, NoteActivity.this);
                            }else {
                                Toast.makeText(this, "Add Note Canceled", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }).show();
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);
        if (requestCode == REQUEST_CAMERA){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                String title = etTitle.getText().toString().trim();
                String message  = etMessage.getText().toString().trim();
                NoteHelper.getLastLocationAndSaveNote(fusedLocationClient, noteId, title, message, imageBitmap, userId, notesRef, 640, 480, NoteActivity.this);
            }else{
                Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }else if (requestCode == CAMERA_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImageHelper.openCamera(NoteActivity.this);
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null){
            if (requestCode == REQUEST_CAMERA) {
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                Uri tempUri = ImageHelper.getImageUri(NoteActivity.this, photo);
                ImageHelper.startCrop(NoteActivity.this, tempUri);
            } else if (requestCode == REQUEST_GALLERY) {
                Uri selectedImage = data.getData();
                ImageHelper.startCrop(NoteActivity.this, selectedImage);
            } else if (requestCode == UCrop.REQUEST_CROP) {
                Uri resultUri = UCrop.getOutput(data);

                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    imageView.setImageBitmap(imageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == UCrop.RESULT_ERROR) {
                Throwable cropError = UCrop.getError(data);
                Toast.makeText(this, "Crop Error : " + cropError, Toast.LENGTH_SHORT).show();
            }
        }
    }
}