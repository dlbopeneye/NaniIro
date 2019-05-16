package org.danbrown.naniiro;

import org.danbrown.naniiro.ColorSets;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button mRetakeButton = findViewById(R.id.mRetakeButton);
        findViewById(R.id.mRGBText).setVisibility(View.GONE);
        findViewById(R.id.mHSVText).setVisibility(View.GONE);
        findViewById(R.id.mClosestColorText).setVisibility(View.GONE);
        mRetakeButton.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
               dispatchTakePictureIntent();
           }
        });

        dispatchTakePictureIntent();
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
            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
            int mRGB = bitmap.getPixel((bitmap.getWidth() / 2), (bitmap.getHeight() / 2));

            float[] mHSV = new float[3];
            colorToHSV(mRGB, mHSV);

            setColorDisplay(mRGB);
            setRGBText(mRGB);
            setHSVText(mHSV);
            findClosestColor(mHSV);

            // TextView mClosestColorText = findViewById(R.id.mClosestColorText);
            // mClosestColorText.setText(ColorSets.X11Colors[0].ColorName);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        // String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String timeStamp = DateFormat.getDateTimeInstance().format(new Date());
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

    private void setColorDisplay(int mRGB) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Bitmap mBlock = Bitmap.createBitmap(displayMetrics.widthPixels, 250, Bitmap.Config.ARGB_8888);
        mBlock.eraseColor(mRGB);

        ImageView mImageView = findViewById(R.id.mColorDisplay);
        mImageView.setImageBitmap(mBlock);
    }

    private void setRGBText(int mRGB) {
        int r = (mRGB >> 16) & 0xff;
        int g = (mRGB >> 8) & 0xff;
        int b = mRGB & 0xff;
        String mRGBString = "R " + r + "\nG " + g + "\nB " + b;

        TextView mRGBText = findViewById(R.id.mRGBText);
        mRGBText.setText(mRGBString);
    }

    private void setHSVText(float[] mHSV) {
        String mHSVString = "H " + mHSV[0] + "\nS " + mHSV[1] + "\nV " + mHSV[2];

        TextView mHSVText = findViewById(R.id.mHSVText);
        mHSVText.setText(mHSVString);
    }

    private void findClosestColor(float[] mHSV) {
        float distance = Float.MAX_VALUE;
        int closestIndex = 0;
        for (int i = 0; i < ColorSets.X11Colors.length; i++) {
            float[] X11HSV = new float[3];
            colorToHSV(ColorSets.X11Colors[i].ColorRGB, X11HSV);
            float tryDistance = findHSVDist(mHSV, X11HSV);
            if (tryDistance < distance) {
                distance = tryDistance;
                closestIndex = i;
            }
        }

        TextView mClosestColorText = findViewById(R.id.mClosestColorText);
        mClosestColorText.setText(ColorSets.X11Colors[closestIndex].ColorName);
    }

    private float findHSVDist(float[] mHSVOne, float[] mHSVTwo) {
        return (mHSVOne[0] - mHSVTwo[0])*(mHSVOne[0] - mHSVTwo[0]) +
                (mHSVOne[1] - mHSVTwo[1])*(mHSVOne[1] - mHSVTwo[1]) +
                (mHSVOne[2] - mHSVTwo[2])*(mHSVOne[2] - mHSVTwo[2]);
    }

    public void reviewImage(View v) {
        Intent intent = new Intent(this, ImageViewActivity.class);
        intent.putExtra(EXTRA_MESSAGE, mCurrentPhotoPath);
        startActivity(intent);
    }

    /* Content Toggle Controls */

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

    public void toggleContents (View v) {
        String mContentName = v.getTag().toString();
        int id = getResources().getIdentifier(mContentName, "id", this.getPackageName());
        View mContent = findViewById(id);
        if (mContent.isShown()) {
            slideUp(this, mContent);
            mContent.setVisibility(View.GONE);
        } else {
            mContent.setVisibility(View.VISIBLE);
            slideDown(this, mContent);
        }
    }
}
