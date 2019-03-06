package org.danbrown.naniiro;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        ImageView mReviewImage = findViewById(R.id.mReviewImage);

        Intent intent = getIntent();
        String mCurrentPhotoPath =  intent.getStringExtra(EXTRA_MESSAGE);

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        mReviewImage.setImageBitmap(bitmap);
    }
}
