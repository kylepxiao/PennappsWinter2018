/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;
    public static Bitmap afro;
    public static Bitmap crosshair;

    private static final int COLOR_CHOICES[] = {
        Color.BLUE,
        Color.CYAN,
        Color.GREEN,
        Color.MAGENTA,
        Color.RED,
        Color.WHITE,
        Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;

    FaceGraphic(GraphicOverlay overlay, Bitmap b1, Bitmap b2) {
        super(overlay);

        afro = b1;
        crosshair = b2;

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        float crosshairsize = 100f;
        if (face == null) {
            canvas.drawBitmap(crosshair, new Rect(0, 0, 400, 400), new Rect(Math.round(canvas.getWidth() / 2.0f - crosshairsize / 2f), Math.round(canvas.getHeight() / 2 - crosshairsize / 2f), Math.round(canvas.getWidth() / 2f + crosshairsize / 2f), Math.round(canvas.getHeight() / 2f + crosshairsize / 2f)), mBoxPaint);
            return;
        }
        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, mFacePositionPaint);
        //canvas.drawText("id: " + mFaceId, x + ID_X_OFFSET, y + ID_Y_OFFSET, mIdPaint);
        //canvas.drawText("happiness: " + String.format("%.2f", face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint);
        //canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint);
        //canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET*2, y - ID_Y_OFFSET*2, mIdPaint);
        canvas.drawText("xpos: " + Float.toString(face.getPosition().x), x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint);


        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, mBoxPaint);
        float scale = 2.0f;
        canvas.drawBitmap(afro, new Rect(0, 0, 830 * 2, 728 * 2), new Rect(Math.round(left + face.getWidth() / 2.0f - scale * face.getWidth() / 2.0f), Math.round(top - 0.3f * (bottom - top) + 0.3f * (bottom - top) / 2.0f - 0.3f * scale * (bottom - top) / 2.0f), Math.round(right - face.getWidth() / 2.0f + scale * face.getWidth() / 2.0f), Math.round(bottom - 0.3f * (bottom - top))), new Paint());
        //canvas.drawBitmap(afro, new Rect(0, 0, 1660, 1456), new Rect(Math.round(left-face.getWidth() / 2.0f), Math.round(2.15f*top-.45f*bottom), Math.round(right + face.getWidth() / 2.0f), Math.round(0.7f * bottom + .3f* top)), mBoxPaint);
        canvas.drawBitmap(crosshair, new Rect(0, 0, 400, 400), new Rect(Math.round(canvas.getWidth() / 2.0f - crosshairsize / 2f), Math.round(canvas.getHeight() / 2 - crosshairsize / 2f), Math.round(canvas.getWidth() / 2f + crosshairsize / 2f), Math.round(canvas.getHeight() / 2f + crosshairsize / 2f)), new Paint());
    }
}
