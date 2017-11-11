package com.scarletsteel.hackprincetonf17;

import android.Manifest;
import android.content.pm.PackageManager;
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

public class CameraActivity extends AppCompatActivity {

    private Camera mCamera;
    private CameraView mCameraView;
    private TextureView previewTextureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 1337 );

        previewTextureView = ((TextureView)findViewById(R.id.camera_textureview_preview));

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

        mCameraView = new CameraView(this, mCamera, previewTextureView, this, processFrame);
    }
}
