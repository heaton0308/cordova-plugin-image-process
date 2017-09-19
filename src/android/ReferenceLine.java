package imagecrop.kevin.com.myapplication3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;


public class ReferenceLine extends View {

    private Paint mLinePaint;

    public ReferenceLine(Context context) {
        super(context);
        init();
    }

    public ReferenceLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ReferenceLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(Color.parseColor("#45e0e0e0"));
        mLinePaint.setStrokeWidth(3);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int screenWidth = CameraUtils.getScreenWH(getContext()).widthPixels;
        int screenHeight = CameraUtils.getScreenWH(getContext()).heightPixels;

        int width = screenWidth / 4;
        int height = screenHeight / 5;
        int widths = screenWidth / 8;

        canvas.drawLine(width, 0, width, screenHeight, mLinePaint);
        canvas.drawLine(screenWidth - width, 0, screenWidth - width, screenHeight, mLinePaint);
        canvas.drawLine(0, height - widths, screenWidth, height - widths, mLinePaint);
        canvas.drawLine(0, screenHeight - height + widths, screenWidth, screenHeight - height + widths, mLinePaint);

    }


}
