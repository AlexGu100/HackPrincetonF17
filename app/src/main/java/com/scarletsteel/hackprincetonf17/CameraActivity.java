package com.scarletsteel.hackprincetonf17;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Camera;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

public class CameraActivity extends AppCompatActivity {

    private Camera mCamera;
    private CameraView mCameraView;
    private TextureView mTextureView;
    private ImageView mImageView;

    void processFrame()
    {
        Bitmap iImage;
        int width = mTextureView.getWidth();
        int height = mTextureView.getHeight();

        iImage = mTextureView.getBitmap(width/4, height/8);

        Bitmap oImage = processImage(iImage, height/8, width/4);

        mImageView.setImageBitmap(oImage);
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap iImage;
                int width = mTextureView.getWidth();
                int height = mTextureView.getHeight();

                iImage = mTextureView.getBitmap(width/4, height/4);

                final Bitmap oImage = processImage(iImage, height/4, width/4);

                CameraActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImageView.setImageBitmap(oImage);
                    }
                });
            }
        }).start();*/
    }

    Bitmap processImage(Bitmap bmp, int height, int width)
    {
        Bitmap output = null;
        int[] pixels = new int[(width * height)];
        int[] newPixels = new int[(width * height)];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);

        //double[] protanopia = {0.567, 0.433, 0.0, 0.558, 0.442, 0.0  ,  0.0  , 0.242, 0.758};
        //double[] protanopia = {0.1121, 0.8853, -0.0002, 0.1127, 0.8897, 0.0002, 0.0045, 0.0001, 1.0003};
        double[] protanopia = {0.1, 0.9, -0, 0.1, 0.9, 0, 0, 0, 1};
        double[] err2mod = {0,0,0,.7/1.7,1/1.7,0,.7/1.7,0,1/1.7};

        for (int i = 0; i < pixels.length; i++) {
            int[] ARGB = new int[4];
            int[] ARGBmod = new int[4];
            int[] err = new int[3];
            int[] fix = new int[3];
            int[] result = new int[4];
            ARGB[0] = (pixels[i] >> 24) & 0xff;
            ARGB[1] = (pixels[i] >> 16) & 0xff;
            ARGB[2] = (pixels[i] >> 8) & 0xff;
            ARGB[3] = (pixels[i]) & 0xff;

            //ARGB[1] *= 0; //delete all red color

            //Convert RGB values to protanopia
            ARGBmod[0] = ARGB[0];
            ARGBmod[1] = (int)(ARGB[1]*protanopia[0]  +  ARGB[2]*protanopia[1]  + ARGB[3]*protanopia[2]);
            ARGBmod[2] = (int)(ARGB[1]*protanopia[3]  +  ARGB[2]*protanopia[4]  + ARGB[3]*protanopia[5]);
            ARGBmod[3] = (int)(ARGB[1]*protanopia[6]  +  ARGB[2]*protanopia[7]  + ARGB[3]*protanopia[8]);

            //Calculate difference
            err[0] = ARGB[0] - ARGBmod[0];
            err[1] = ARGB[1] - ARGBmod[1];
            err[2] = ARGB[2] - ARGBmod[2];

            //Calculate Error
            fix[0] = (int)(err2mod[0]*err[0] + err2mod[1]*err[1] + err2mod[2]*err[2]);
            fix[1] = (int)(err2mod[3]*err[0] + err2mod[4]*err[1] + err2mod[5]*err[2]);
            fix[2] = (int)(err2mod[6]*err[0] + err2mod[7]*err[1] + err2mod[8]*err[2]);

            //Calculate result
            result[0] = ARGB[0];
            result[1] = ARGB[1]+fix[0];
            result[2] = ARGB[2]+fix[1];
            result[3] = ARGB[3]+fix[2];

            pixels[i] = (result[0] << 24) | (result[1] << 16) | (result[2] << 8) | (result[3]);
        }
        if (output == null)
            output = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
        output.setPixels(pixels, 0, width, 0, 0, width, height);
        return output;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 1337 );

        mTextureView = ((TextureView)findViewById(R.id.camera_textureview_preview));
        mImageView = (ImageView)findViewById(R.id.camera_imageview_output);

        /*//btn to close the application
        ImageButton imgClose = (ImageButton)findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.exit(0);
            }
        });*/
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        try{
            mCamera = Camera.open();//you can use open(int) to use different cameras
        } catch (Exception e){
            Log.d("ERROR", "Failed to open camera: " + e.getMessage());
        }

        Camera.PreviewCallback processFrame = new Camera.PreviewCallback()
        {
            public void onPreviewFrame(byte[] previewData, Camera previewCamera)
            {
                mCameraView.previewCallbackCompleted();
            }
        };

        mCameraView = new CameraView(this, mCamera, mTextureView, this, processFrame);
    }
}
