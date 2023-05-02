package com.jiangdg.usbcamera.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.SurfaceView;


public class ImageSurfaceView extends SurfaceView {
    private Bitmap mBitmap;

    public ImageSurfaceView(Context context, Bitmap bitmap) {
        super(context);
        mBitmap = bitmap;
    }

    public void updateImage(Bitmap newBitmap) {
        mBitmap = newBitmap;
        invalidate(); // this will cause the onDraw method to be called
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }
}