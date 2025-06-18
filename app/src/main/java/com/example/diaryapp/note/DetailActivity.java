package com.example.diaryapp.note;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.diaryapp.MainActivity;
import com.example.diaryapp.R;
import com.google.android.play.core.integrity.IntegrityTokenRequest;

public class DetailActivity extends AppCompatActivity {
    ImageView detailImageView;
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}