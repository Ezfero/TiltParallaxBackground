package com.silgrid.parallax.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.silgrid.parallax.R;
import com.silgrid.parallax.sensor.TiltSensor;

public class ParallaxLayout extends FrameLayout implements TiltSensor.SensorCallback {

	private int mParallaxSpeed = 3;
	private float mTranslation;
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
	public void draw(Canvas canvas) {
		if (mBackground != null) {
			canvas.save();
			canvas.translate(mBackgroundInitPosition + mTranslation, 0);
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
		mTranslation = angleToTranslation(xAngle);
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

	private void updateBackgroundBounds() {
		float ratio = (float) getBottom() / mBackground.getIntrinsicHeight();
		int backgroundWidth = (int) (mBackground.getIntrinsicWidth() * ratio);
		int viewWidth = getRight() - getLeft();

		mBackground.setBounds(0, 0, backgroundWidth, getBottom());
		mBackgroundInitPosition = -(backgroundWidth - viewWidth) / 2;
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
