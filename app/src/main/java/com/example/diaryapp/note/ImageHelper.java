package com.example.diaryapp.note;

import static androidx.core.app.ActivityCompat.startActivityForResult;
import static com.example.diaryapp.note.NoteActivity.CAMERA_PERMISSION_CODE;
import static com.example.diaryapp.note.NoteActivity.REQUEST_CAMERA;
import static com.example.diaryapp.note.NoteActivity.REQUEST_GALLERY;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class ImageHelper {

    //nampilin dialog camera pr gallery
    public static void showImagePickerDialog(Activity activity){
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(activity)
                .setTitle("Select Image From :")
                .setItems(options, (dialog, which) -> {
                    if (which == 0){
                        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                        }else{
                            openCamera(activity);
                        }
                    }else {
                        openGallery(activity);
                    }
                }).show();
    }

    //open kamera
    public static void openCamera(Activity activity){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(activity.getPackageManager()) != null){
            activity.startActivityForResult(cameraIntent, REQUEST_CAMERA);
        }
    }

    //open galeri
    public static void openGallery(Activity activity) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(galleryIntent, REQUEST_GALLERY);
    }

    //ambil image uri
    public static Uri getImageUri(Activity activity, Bitmap bitmap){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(activity.getContentResolver(), bitmap, "TempImage", null);
        return Uri.parse(path);
    }

    //crop image
    public static void startCrop(Activity activity, Uri sourceUri){
        Uri destinationUri = Uri.fromFile(new File(activity.getCacheDir(), "cropped_" + System.currentTimeMillis() + ".jpg"));
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(100);
//        options.setFreeStyleCropEnabled(true); //artinya bisa freestyle crop, alias gak kotak
        UCrop.of(sourceUri, destinationUri)
                .withOptions(options)
                .withAspectRatio(1, 1)
                .withMaxResultSize(1000,1000)
                .start(activity);
    }


    //encode bitmap ke base64
    public static String encodeImageToBase64(Bitmap bitmap, int quality){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }


    //decode base64 ke bitmap
    public static Bitmap decodeBase64ToBitmap(String base64Str){
        if (base64Str == null || base64Str.isEmpty()) {
            return null; // atau Bitmap default
        }
        byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        return bitmap;
    }

}
