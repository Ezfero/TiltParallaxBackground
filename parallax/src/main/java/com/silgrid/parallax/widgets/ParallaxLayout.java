package com.silgrid.parallax.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.silgrid.parallax.R;
import com.silgrid.parallax.sensor.TiltSensor;

public class ParallaxLayout extends FrameLayout implements TiltSensor.SensorCallback {

	private int mParallaxSpeed = 3;
	private float mTranslationX;
	private float mTranslationY;
	private float mBackgroundInitXPosition;
	private float mBackgroundInitYPosition;

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

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.ParallaxImageView,
				0, 0);

		try {
			mParallaxSpeed = a.getInt(R.styleable.ParallaxLayout_parallaxBackgroundSpeed, mParallaxSpeed);
		} finally {
			a.recycle();
		}

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
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);

		if (hasWindowFocus) {
			mTiltSensor.onResume();
		} else {
			mTiltSensor.onPause();
		}
	}

	@Override
	public void draw(Canvas canvas) {
		if (mBackground != null) {
			canvas.save();
			canvas.translate(mBackgroundInitXPosition + mTranslationX, mBackgroundInitYPosition + mTranslationY);
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
		updateBackgroundBounds(w, h);
	}

	@Override
	public void onRotationChanged(float xAngle, float yAngle) {
		mTranslationX = angleToTranslation(xAngle);
		invalidate();

		for (int i = 0; i < getChildCount(); ++i) {
			View child = getChildAt(i);
			if (child instanceof ParallaxImageView) {
				((ParallaxImageView) child).setHorizontalTiltLevel(xAngle);
				((ParallaxImageView) child).setVerticalTiltLevel(yAngle);
				child.invalidate();
			}
		}
	}

	public int getParallaxSpeed() {
		return mParallaxSpeed;
	}

	public void setParallaxSpeed(int parallaxSpeed) {
		mParallaxSpeed = parallaxSpeed;
	}

	private void updateBackgroundBounds(int width, int height) {
		boolean isLandscapeImage = mBackground.getIntrinsicWidth() > mBackground.getIntrinsicHeight();

		int bgWidth = width;
		int bgHeight = height;
		if (isLandscapeImage) {
			float ratio = mBackground.getIntrinsicWidth() > width
					? (float) height / mBackground.getIntrinsicHeight()
					: (float) width / mBackground.getIntrinsicWidth();
			bgWidth = (int) (mBackground.getIntrinsicWidth() * ratio);
		} else {
			float ratio = (float) width / mBackground.getIntrinsicWidth();
			bgHeight = (int) (mBackground.getIntrinsicHeight() * ratio);
		}

		mBackground.setBounds(0, 0, bgWidth, bgHeight);
		mBackgroundInitXPosition = -(bgWidth - width) / 2;
		mBackgroundInitYPosition = -(bgHeight - height) / 2;
	}

	private float angleToTranslation(float degrees) {
		int bgWidth = mBackground.getBounds().right - mBackground.getBounds().left;
		int diff = Math.abs((getRight() - getLeft()) - bgWidth);
		float translation = mParallaxSpeed * degrees;

		if (translation > diff / 2) {
			translation = diff / 2;
		} else if (translation < -diff / 2) {
			translation = -diff / 2;
		}

		return translation;
	}
}
