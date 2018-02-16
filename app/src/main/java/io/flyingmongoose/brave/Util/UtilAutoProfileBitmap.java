package io.flyingmongoose.brave.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

/**
 * Created by Acinite on 2018/02/05.
 */

public class UtilAutoProfileBitmap
{
    public static Bitmap drawStringonBitmap(Bitmap src, String string, Point location, int color, int alpha, int size, boolean underline, int width , int height) {

        Bitmap result = Bitmap.createBitmap(width, height, src.getConfig());

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setTextSize(size);
        paint.setAntiAlias(true);
        paint.setUnderlineText(underline);
        canvas.drawText(string, location.x, location.y, paint);

        return result;
    }

    public static Bitmap textAsBitmap(String text, float textSize, int textColor, int circleColor) {
        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        Paint paintCircle = new Paint(ANTI_ALIAS_FLAG);
        //Draw bg circle
        paintCircle.setColor(circleColor);
        paintCircle.setStyle(Paint.Style.FILL);


        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 20.0f); // round
        int height = (int) (baseline + paint.descent() + 20.0f);

        int trueWidth = width;
        if(width>height)height=width; else width=height;
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(image);
        canvas.drawCircle(50,50, 60, paintCircle);
        canvas.drawText(text, width/2-trueWidth/2 +10, baseline + 13, paint);
        return image;
    }
}
