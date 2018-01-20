package com.google.android.gms.samples.vision.face.facetracker;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.face.Face;

public class ImageGraphic extends GraphicOverlay.Graphic{
    private Bitmap bmp;
    private Rect src;
    private Rect dest;
    private Paint mBoxPaint;

    ImageGraphic(GraphicOverlay overlay, Bitmap b, Rect s, Rect d) {
        super(overlay);
        bmp = b;
        src = s;
        dest = d;
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(bmp, src, dest, new Paint());
    }
}
