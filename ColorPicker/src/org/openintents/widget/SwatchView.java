package org.openintents.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class SwatchView extends View {

	public static final String TAG = "SwatchView";

	static final int defaultWidth = 32;
	static final int defaultHeight = 32;

	int color;
	Paint paint = new Paint();
	
	boolean grow_to_fit = false;
	
	// ========================================================================
	public SwatchView(Context context) {
		super(context);
		init();
	}

	public SwatchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SwatchView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}


	/**
	 * @see android.view.View#measure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		 int max_width = MeasureSpec.getSize(widthMeasureSpec);
         int max_height = MeasureSpec.getSize(heightMeasureSpec);
		 Log.d(TAG, "Swatch measured size: (" + max_width + "x" + max_height + ")");

		 int size = grow_to_fit ? Math.max(max_width, max_height) : Math.min(max_width, max_height);

         setMeasuredDimension(size, size);
	}

	// ========================================================================
	void init() {
		this.paint.setAntiAlias(true);
		this.paint.setStrokeJoin(Join.ROUND);
	}

	// ========================================================================
	public void setGrow(boolean grow) {
		this.grow_to_fit = grow;
	}
	
	// ========================================================================
	public void setColor(int color) {
		this.color = color;
	}

	// ========================================================================
	@Override
	protected void onDraw(Canvas canvas) {

		int size = Math.min(getWidth(), getHeight());
		float stroke_width = size/8f;
		this.paint.setStrokeWidth(stroke_width);
		
		RectF rect = new RectF();
		float inset_distatnce = -(size - stroke_width)/2;
		rect.inset(inset_distatnce, inset_distatnce);
		
		this.paint.setColor(this.color);
		this.paint.setStyle(Style.FILL);
		drawCenteredRect(canvas, rect, this.paint);
		
		this.paint.setColor(Color.BLACK);
		this.paint.setStyle(Style.STROKE);
		
		canvas.save();
		canvas.clipRect(0, 0, getWidth()/2, getHeight());
		drawCenteredRect(canvas, rect, this.paint);
		canvas.restore();

		canvas.clipRect(getWidth()/2, 0, getWidth(), getHeight());
		this.paint.setColor(Color.WHITE);
		drawCenteredRect(canvas, rect, this.paint);
	}

	// ========================================================================
	void drawCenteredRect(Canvas canvas, RectF rect, Paint paint) {

		canvas.save();
		canvas.translate(getWidth()/2, getHeight()/2);
		float r = paint.getStrokeWidth()*2;
		canvas.drawRoundRect(rect, r, r, this.paint);
		canvas.restore();
	}
} 