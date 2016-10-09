package com.silgrid.parallax.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.silgrid.parallax.sensor.TiltSensor;

public class ParallaxLayout extends FrameLayout implements TiltSensor.SensorCallback {

	private float mRotation;
	private float mBackgroundInitPosition;

	private Drawable mBackground;
	private TiltSensor mTiltSensor;

	public ParallaxLayout(Context context) {
		this(context, null);
	}

	public ParallaxLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ParallaxLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		mTiltSensor = new TiltSensor(context, this);

		setWillNotDraw(false);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		mTiltSensor.onResume();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		mTiltSensor.onPause();
	}

	@Override
	public void draw(Canvas canvas) {
		if (mBackground != null) {
			canvas.save();
			canvas.translate(mBackgroundInitPosition + mRotation, 0);
			mBackground.draw(canvas);
			canvas.restore();
		}

		super.draw(canvas);

	}

	@Override
	public void setBackground(Drawable background) {
		mBackground = background;
	}

	@Override
	public void setBackgroundResource(int resid) {
		mBackground = ContextCompat.getDrawable(getContext(), resid);
	}

	@Override
	public void setBackgroundDrawable(Drawable background) {
		mBackground = background;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		updateBackgroundBounds();
	}

	@Override
	public void onRotationChanged(float xAngle, float yAngle) {
		mRotation = xAngle;
		invalidate();

		for (int i = 0; i < getChildCount(); ++i) {
			View child = getChildAt(i);
			if (child instanceof ParallaxImageView) {
				((ParallaxImageView) child).setTiltLevel(xAngle);
				child.invalidate();
			}
		}
	}

	private void updateBackgroundBounds() {
		float coef = (float) getBottom() / mBackground.getIntrinsicHeight();
		int backgroundWidth = (int) (mBackground.getIntrinsicWidth() * coef);
		int viewWidth = getRight() - getLeft();

		mBackground.setBounds(0, 0, backgroundWidth, getBottom());
		mBackgroundInitPosition = -(backgroundWidth - viewWidth) / 2;
	}
}
