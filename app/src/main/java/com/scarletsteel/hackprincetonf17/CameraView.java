package com.scarletsteel.hackprincetonf17;

import java.io.IOException;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

public class CameraView extends TextureView implements TextureView.SurfaceTextureListener
{
    private CameraActivity mCameraActivity;
    private Camera mCamera;
    private Camera.PreviewCallback mPreviewCallback;
    private TextureView mTextureView;
    private byte[] cameraData;

    public CameraView(Context context, Camera camera, TextureView textureView, final CameraActivity cameraActivity, Camera.PreviewCallback previewCallback) {
        super(context);
        mCamera = camera;
        mCameraActivity = cameraActivity;
        mPreviewCallback = previewCallback;
        mTextureView = textureView;
        mTextureView.setSurfaceTextureListener(this);

        if (this.mTextureView.isAvailable()) {
            onSurfaceTextureAvailable(mTextureView.getSurfaceTexture(), mTextureView.getWidth(), mTextureView.getHeight());
        }
    }

    private byte[] getPreviewBuffer() {
        int bufferSize;
        Camera.Parameters parameters = this.mCamera.getParameters();
        int previewFormat = parameters.getPreviewFormat();
        Camera.Size previewSize = parameters.getPreviewSize();
        if (previewFormat != 842094169) {
            bufferSize = (int) (((float) (previewSize.width * previewSize.height)) * (((float) ImageFormat.getBitsPerPixel(previewFormat)) / 8.0f));
        } else {
            int yStride = ((int) Math.ceil(((double) previewSize.width) / 16.0d)) * 16;
            bufferSize = (yStride * previewSize.height) + (((previewSize.height * (((int) Math.ceil(((double) (yStride / 2)) / 16.0d)) * 16)) / 2) * 2);
        }
        return new byte[bufferSize];
    }

    public void previewCallbackCompleted()
    {
        mCamera.addCallbackBuffer(cameraData);
    }

    public void onSurfaceTextureAvailable(SurfaceTexture paramSurfaceTexture, int width, int height)
    {
        try
        {
            // Temp orientation hack
            mCamera.setDisplayOrientation(90);

            mCamera.setPreviewTexture(paramSurfaceTexture);
            cameraData = getPreviewBuffer();
            mCamera.addCallbackBuffer(cameraData);
            mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);
            mCamera.startPreview();
        }
        catch (IOException e)
        {
            // Exception
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture paramSurfaceTexture, int paramInt1, int paramInt2)
    {
        //
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture)
    {
        mCamera.stopPreview();
        mCamera.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface)
    {
        //
    }
}
