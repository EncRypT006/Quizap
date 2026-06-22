package com.quizapp.webview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.Random;

public class StarFieldView extends View {

    private static final int STAR_COUNT = 80;
    private float[] starX, starY, starRadius, starSpeed, starAlpha;
    private Paint paint;
    private Random random;
    private boolean running = true;

    public StarFieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        random = new Random();
    }

    private void initStars(int width, int height) {
        starX = new float[STAR_COUNT];
        starY = new float[STAR_COUNT];
        starRadius = new float[STAR_COUNT];
        starSpeed = new float[STAR_COUNT];
        starAlpha = new float[STAR_COUNT];
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = random.nextFloat() * width;
            starY[i] = random.nextFloat() * height;
            starRadius[i] = random.nextFloat() * 3f + 0.5f;
            starSpeed[i] = random.nextFloat() * 1.5f + 0.3f;
            starAlpha[i] = random.nextFloat();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initStars(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (starX == null) return;
        int width = getWidth();
        int height = getHeight();
        for (int i = 0; i < STAR_COUNT; i++) {
            starAlpha[i] += starSpeed[i] * 0.02f;
            if (starAlpha[i] > 1f) starAlpha[i] = 0f;
            int alpha = (int) (Math.abs(Math.sin(starAlpha[i] * Math.PI)) * 255);
            paint.setColor(0xFFFFD700);
            paint.setAlpha(alpha);
            canvas.drawCircle(starX[i], starY[i], starRadius[i], paint);
            starY[i] += starSpeed[i] * 0.3f;
            if (starY[i] > height) {
                starY[i] = 0;
                starX[i] = random.nextFloat() * width;
            }
        }
        if (running) postInvalidateDelayed(30);
    }

    public void stopStars() {
        running = false;
    }
}
