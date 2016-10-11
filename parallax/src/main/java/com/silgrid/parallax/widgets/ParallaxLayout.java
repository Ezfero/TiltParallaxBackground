package com.silgrid.parallax.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.silgrid.parallax.R;
import com.silgrid.parallax.sensor.TiltSensor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ParallaxLayout extends FrameLayout implements TiltSensor.SensorCallback {

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({DIRECTION_HORIZONTAL, DIRECTION_VERTICAL, DIRECTION_BOTH})
	public @interface ParallaxDirection {}

	public static final int DIRECTION_HORIZONTAL = 0x01;
	public static final int DIRECTION_VERTICAL = 0x10;
	public static final int DIRECTION_BOTH = 0x11;

	private int mDirection = DIRECTION_HORIZONTAL;
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
				R.styleable.ParallaxLayout,
				0, 0);

		try {
			mParallaxSpeed = a.getInt(R.styleable.ParallaxLayout_parallaxBackgroundSpeed, mParallaxSpeed);
			mDirection = a.getInt(R.styleable.ParallaxLayout_parallaxBackgroundDirection, mDirection);
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
		if ((mDirection & DIRECTION_HORIZONTAL) != 0) {
			mTranslationX = angleToTranslation(xAngle, DIRECTION_HORIZONTAL);
		}
		if ((mDirection & DIRECTION_VERTICAL) != 0) {
			Log.d("debug", "angle " + yAngle);
			mTranslationY = angleToTranslation(yAngle, DIRECTION_VERTICAL);
			Log.d("debug", "mTranslationY " + mTranslationY);
		}
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
		int bgWidth = width;
		int bgHeight = height;

		if (mBackground.getIntrinsicWidth() > mBackground.getIntrinsicHeight()) {
			boolean cropWidth = mBackground.getIntrinsicWidth() > width;
			float ratio = cropWidth
					? (float) height / mBackground.getIntrinsicHeight()
					: (float) width / mBackground.getIntrinsicWidth();
			bgWidth = (int) (mBackground.getIntrinsicWidth() * ratio);
			bgHeight = cropWidth
					? bgHeight
					: (int) (mBackground.getIntrinsicHeight() * ratio);
		} else {
			float ratio = (float) width / mBackground.getIntrinsicWidth();
			bgHeight = (int) (mBackground.getIntrinsicHeight() * ratio);
		}

		mBackground.setBounds(0, 0, bgWidth, bgHeight);
		mBackgroundInitXPosition = -(bgWidth - width) / 2;
		mBackgroundInitYPosition = -(bgHeight - height) / 2;
	}

	private float angleToTranslation(float degrees, int direction) {
		int bgWidth = mBackground.getBounds().right - mBackground.getBounds().left;
		int bgHeight = mBackground.getBounds().bottom - mBackground.getBounds().top;
		int diff = direction == DIRECTION_HORIZONTAL
				? Math.abs((getRight() - getLeft()) - bgWidth)
				: Math.abs((getBottom() - getTop()) - bgHeight);
		float translation = mParallaxSpeed * degrees;

		if (translation > diff / 2) {
			translation = diff / 2;
		} else if (translation < -diff / 2) {
			translation = -diff / 2;
		}

		return translation;
	}
}
