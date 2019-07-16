package org.danbrown.naniiro;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
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
        findViewById(R.id.mRGBContent).setVisibility(View.GONE);
        findViewById(R.id.mHSVContent).setVisibility(View.GONE);
        findViewById(R.id.mClosestColorContent).setVisibility(View.GONE);
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

            // Color Conversions
            float[] mHSV = new float[3];
            colorToHSV(mRGB, mHSV);
            float[] mRGBArray = RGBIntToArray(mRGB);

            // Setting Displays
            setColorDisplay(mRGB);
            setRGBText(mRGB);
            setHSVText(mHSV);
            findClosestColor(mRGBArray);
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

    /* Top Banner Color Display */
    private void setColorDisplay(int mRGB) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Bitmap mBlock = Bitmap.createBitmap(displayMetrics.widthPixels, 250, Bitmap.Config.ARGB_8888);
        mBlock.eraseColor(mRGB);

        ImageView mImageView = findViewById(R.id.mColorDisplay);
        mImageView.setImageBitmap(mBlock);
    }

    /* RGB Drop Down */
    private void setRGBText(int mRGB) {
        int r = (mRGB >> 16) & 0xff;
        int g = (mRGB >> 8) & 0xff;
        int b = mRGB & 0xff;

        String mRGBString = "R " + r + "\nG " + g + "\nB " + b;
        TextView mRGBText = findViewById(R.id.mRGBText);
        mRGBText.setText(mRGBString);
    }

    /* HSV Drop Down */
    private void setHSVText(float[] mHSV) {
        String mHSVString = "H " + mHSV[0] + "\nS " + mHSV[1] + "\nV " + mHSV[2];

        TextView mHSVText = findViewById(R.id.mHSVText);
        mHSVText.setText(mHSVString);
    }

    /* Closest Color Drop Down */
    private void findClosestColor(float[] mRGBArray) {
        float distance = Float.MAX_VALUE;
        int closestIndex = 0;
        ColorSpace.Connector c = ColorSpace.connect(
                ColorSpace.get(ColorSpace.Named.SRGB),
                ColorSpace.get(ColorSpace.Named.CIE_LAB));
        float[] mCIELab = c.transform(mRGBArray);
        for (int i = 0; i < ColorSets.X11Colors.length; i++) {
            float[] X11CIELab = c.transform(RGBIntToArray(ColorSets.X11Colors[i].ColorRGB));
            float tryDistance = findCIELabDist(mCIELab, X11CIELab);
            if (tryDistance < distance) {
                distance = tryDistance;
                closestIndex = i;
            }
        }
        TextView mClosestColorText = findViewById(R.id.mClosestColorText);
        mClosestColorText.setText(ColorSets.X11Colors[closestIndex].ColorName);

        setClosestColorRGB(ColorSets.X11Colors[closestIndex].ColorRGB);
        setClosestColorDisplay(ColorSets.X11Colors[closestIndex].ColorRGB);
    }

    private void setClosestColorRGB (int mRGB) {
        int r = (mRGB >> 16) & 0xff;
        int g = (mRGB >> 8) & 0xff;
        int b = mRGB & 0xff;
        String mRGBString = "R " + r + "\nG " + g + "\nB " + b;

        TextView mRGBText = findViewById(R.id.mClosestColorRGB);
        mRGBText.setText(mRGBString);
    }

    private void setClosestColorDisplay (int mRGB) {
        Log.d("CCDNOTWORKING", Integer.toString(mRGB));
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Bitmap mBlock = Bitmap.createBitmap(250, 250, Bitmap.Config.ARGB_8888);
        mBlock.eraseColor(mRGB);

        ImageView mImageView = findViewById(R.id.mClosestColorDisplay);
        mImageView.setImageBitmap(mBlock);
    }

    private float findCIELabDist(float[] mCIELabOne, float[] mCIELabTwo) {
        return (mCIELabOne[0] - mCIELabTwo[0])*(mCIELabOne[0] - mCIELabTwo[0]) +
                (mCIELabOne[1] - mCIELabTwo[1])*(mCIELabOne[1] - mCIELabTwo[1]) +
                (mCIELabOne[2] - mCIELabTwo[2])*(mCIELabOne[2] - mCIELabTwo[2]);
    }

    // convert RGB value from #rrggbb (00-ff) to [r,g,b] (0.0-1.0)
    private float[] RGBIntToArray(int mRGB) {
        int r = (mRGB >> 16) & 0xff;
        int g = (mRGB >> 8) & 0xff;
        int b = mRGB & 0xff;

        float[] mRGBArray = new float[3];
        mRGBArray[0] = (float)r / 255.0f;
        mRGBArray[1] = (float)g / 255.0f;
        mRGBArray[2] = (float)b / 255.0f;

        return mRGBArray;
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
