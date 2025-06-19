package com.example.diaryapp.note;

import static com.example.diaryapp.note.NoteActivity.REQUEST_CAMERA;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.diaryapp.auth.AuthHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteHelper {

    //save note ke database
    public static void saveNoteToDatabase(String noteId, String title, String message, Bitmap imageBitmap, String userId, DatabaseReference notesRef, int dstWidth, int dstHeight, double latitude, double longitude, String locationName, boolean isUpdate, Activity activity){
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());


        Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, dstWidth, dstHeight, true);
        String encodedImage = ImageHelper.encodeImageToBase64(scaledBitmap, 50);

        Note note = new Note(noteId, title, message, date, encodedImage, userId, latitude, longitude, locationName);
        notesRef.child(userId).child(noteId).setValue(note)
                .addOnSuccessListener(unused -> {
                    if (isUpdate) {
                        Toast.makeText(activity, "Note updated", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(activity, "Note Saved", Toast.LENGTH_SHORT).show();
                    }
                    activity.finish();
                })
                .addOnFailureListener(e -> {
                    if (isUpdate){
                        Toast.makeText(activity, "Failed to update Note", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(activity, "Failed to save Note", Toast.LENGTH_SHORT).show();
                    }
                });

    }


    //ambil new location dan save ke database
    public static void getLastLocationAndSaveNote(FusedLocationProviderClient fusedLocationClient, String noteId, String title, String message, Bitmap imageBitmap, String userId, DatabaseReference notesRef, int dstWidth, int dstHeight, Activity activity){

        if (title.isEmpty() || message.isEmpty()){
            Toast.makeText(activity, "Title & Message must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        if(imageBitmap == null) {
            Toast.makeText(activity, "Pilih gambar dulu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
            return;
        }

//        String noteId = notesRef.push().getKey();
        boolean isUpdate = noteId != null && !noteId.trim().isEmpty();
        String noteIdToUse = noteId != null && !noteId.trim().isEmpty()? noteId : notesRef.child(userId).push().getKey();
        Handler timeOutHandler = new Handler(Looper.getMainLooper());

        // karena variable ini ada di scope fungsi, maka di lambda gabisa di edit, kecuali diakali pakai final array,
        // beda seperti field pada class yang bisa di set langsung di dalam lambda
        boolean[] locationResolved = {false};

        Runnable timeOutRunnable = () -> {
            if (!locationResolved[0]){
                saveNoteToDatabase(noteIdToUse, title, message, imageBitmap, userId, notesRef, dstWidth, dstHeight, 0.0, 0.0, "Unknown Location", isUpdate, activity);
            }
        };
        timeOutHandler.postDelayed(timeOutRunnable, 3000);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null){
                locationResolved[0] = true;
                timeOutHandler.removeCallbacks(timeOutRunnable); // batalin timeout

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                String locationName = "Unknown Location";
                if(latitude != 0.0 || longitude != 0.0){
                    Geocoder geocoder = new Geocoder(activity, Locale.getDefault());

                    try {
                        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if (!addresses.isEmpty()) {
                            locationName = addresses.get(0).getAddressLine(0);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                saveNoteToDatabase(noteIdToUse, title, message, imageBitmap, userId, notesRef, dstWidth, dstHeight, latitude, longitude, locationName, isUpdate, activity);
            } else {
                requestNewLocationData(fusedLocationClient, locationResolved, noteIdToUse, title, message, imageBitmap, userId, notesRef, dstWidth, dstHeight, isUpdate, activity);  // fallback, jika null minta new location
            }
        }).addOnFailureListener(e -> {
            if (!locationResolved[0]){
                locationResolved[0] = true;
                timeOutHandler.removeCallbacks(timeOutRunnable);
                Toast.makeText(activity, "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show();
                saveNoteToDatabase(noteIdToUse, title, message, imageBitmap, userId, notesRef, dstWidth, dstHeight,0.0, 0.0, "Unknown Location", isUpdate, activity);
            }
        });
    }

    //ambil lokasi
    private static void requestNewLocationData(FusedLocationProviderClient fusedLocationClient, boolean[] locationResolved, String noteId, String title, String message, Bitmap imageBitmap, String userId, DatabaseReference notesRef, int dstWidth, int dstHeight, boolean isUpdate, Activity activity) {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdates(1)
                .build();

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        // Inisialisasi timeout handler dan runnable lokal
        Handler timeoutHandler = new Handler(Looper.getMainLooper());
        Runnable timeoutRunnable = () -> {
            if (!locationResolved[0]) {
                locationResolved[0] = true;
                saveNoteToDatabase(noteId, title, message, imageBitmap, userId, notesRef,
                        dstWidth, dstHeight, 0.0, 0.0, "Unknown Location", isUpdate, activity);
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 3000); // timeout 3 detik

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                fusedLocationClient.removeLocationUpdates(this);
                if (!locationResolved[0]) {
                    locationResolved[0] = true;
                    timeoutHandler.removeCallbacks(timeoutRunnable); // <-- penting

                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        String locationName = "Unknown Location";
                        if (latitude != 0.0 || longitude != 0.0){
                            Geocoder geocoder = new Geocoder(activity, Locale.getDefault());

                            try {
                                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                if (!addresses.isEmpty()) {
                                    locationName = addresses.get(0).getAddressLine(0);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        saveNoteToDatabase(noteId, title, message, imageBitmap, userId, notesRef, dstWidth, dstHeight, latitude, longitude, locationName, isUpdate, activity);
                    } else {
                        saveNoteToDatabase(noteId, title, message, imageBitmap, userId, notesRef, dstWidth, dstHeight, 0.0, 0.0, "Unknown Location", isUpdate, activity);
                        Toast.makeText(activity, "Locations is not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, Looper.getMainLooper());
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
                                noNoteText.setText("MyDiary");
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(activity, "Failed to Load Data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
