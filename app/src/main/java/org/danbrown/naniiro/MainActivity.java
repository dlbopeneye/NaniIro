package org.danbrown.naniiro;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.IOException;

import java.text.*;
import java.util.*;

import static android.graphics.Color.colorToHSV;
import static android.provider.AlarmClock.EXTRA_MESSAGE;


public class MainActivity extends AppCompatActivity {
    
    static final int REQUEST_TAKE_PHOTO = 1;
    String mCurrentPhotoPath;

    TextView mRGBText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button mRetakeButton = findViewById(R.id.mRetakeButton);
        // final Button mReviewButton = findViewById(R.id.mReviewButton);
        mRGBText = (TextView) findViewById(R.id.mRGBText);
        mRGBText.setVisibility(View.GONE);
        mRetakeButton.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
               dispatchTakePictureIntent();
           }
        });
        /* mReviewButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                reviewImage();
            }
        }); */

        dispatchTakePictureIntent();
    }

    public static void slideUp(Context ctx, View v) {
        Animation a = AnimationUtils.loadAnimation(ctx, R.anim.slide_up);
        if (a != null) {
            a.reset();
            if (v != null) {
                v.clearAnimation();
                v.startAnimation(a);
            }
        }
    }

    public static void slideDown(Context ctx, View v) {
        Animation a = AnimationUtils.loadAnimation(ctx, R.anim.slide_down);
        if (a != null) {
            a.reset();
            if (v != null) {
                v.clearAnimation();
                v.startAnimation(a);
            }
        }
    }

    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "org.danbrown.naniiro.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            TextView mRGBColor = findViewById(R.id.mRGBText);
            ImageView mImageView = findViewById(R.id.mImageView);

            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            int midW = bitmap.getWidth() / 2;
            int midH = bitmap.getHeight() / 2;

            // float[] mHSV = new float[3];

            int mRGB = bitmap.getPixel(midW, midH);
            int r = (mRGB >> 16) & 0xff;
            int g = (mRGB >> 8) & 0xff;
            int b = mRGB & 0xff;

            Bitmap mBlock = Bitmap.createBitmap(250, 250, Bitmap.Config.ARGB_8888);
            mBlock.eraseColor(mRGB);

            // colorToHSV(mRGB, mHSV);

            String mRGBString = "[" + r + ", " + g + ", " + b + "]";

            mRGBColor.setText(mRGBString);
            mImageView.setImageBitmap(mBlock);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_" ;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg",  /* suffix */
                storageDir     /* directory */
        );

        // Save a file: pathfor use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void reviewImage(View v) {
        Intent intent = new Intent(this, ImageViewActivity.class);
        intent.putExtra(EXTRA_MESSAGE, mCurrentPhotoPath);
        startActivity(intent);
    }

    public void toggleContents (View v) {
        if (mRGBText.isShown()) {
            slideUp(this, mRGBText);
            mRGBText.setVisibility(View.GONE);
        } else {
            mRGBText.setVisibility(View.VISIBLE);
            slideDown(this, mRGBText);
        }
    }
}
