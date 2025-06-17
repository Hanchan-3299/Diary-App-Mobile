package com.example.diaryapp.note;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.diaryapp.MainActivity;
import com.example.diaryapp.R;

public class DetailActivity extends AppCompatActivity {
    ImageView detailImageView;
    TextView detailTitle, detailMessage, detailDate;

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

        //ambil value dari intent
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        String date = intent.getStringExtra("date");
        String imageBase64 = intent.getStringExtra("imageBase64");

        //set ke view detail
        detailTitle.setText(title);
        detailMessage.setText(message);
        detailDate.setText(date);
        detailImageView.setImageBitmap(ImageHelper.decodeBase64ToBitmap(imageBase64));

        //scrollbar
        detailMessage.setMovementMethod(new android.text.method.ScrollingMovementMethod());

        //back button
        ImageView btnBack = findViewById(R.id.btnDetailExit);
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(DetailActivity.this, MainActivity.class));
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}