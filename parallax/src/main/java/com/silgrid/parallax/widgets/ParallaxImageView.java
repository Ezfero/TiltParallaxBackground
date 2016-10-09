package com.silgrid.parallax.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.silgrid.parallax.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ParallaxImageView extends ImageView {

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({DIRECTION_HORIZONTAL, DIRECTION_VERTICAL, DIRECTION_BOTH})
	public @interface ParallaxDirection {}

	public static final int DIRECTION_HORIZONTAL = 0x01;
	public static final int DIRECTION_VERTICAL = 0x10;
	public static final int DIRECTION_BOTH = 0x11;

	private int mParallaxSpeed = 2;
	private int mDirection = DIRECTION_HORIZONTAL;
	private boolean mAllowOutsideParent = false;

	private float mHorizontalTiltLevel;
	private float mVerticalTiltLevel;

	public ParallaxImageView(Context context) {
		super(context);
	}

	public ParallaxImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ParallaxImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.ParallaxImageView,
				0, 0);

		try {
			mParallaxSpeed = a.getInt(R.styleable.ParallaxImageView_parallaxSpeed, mParallaxSpeed);
			mDirection = a.getInt(R.styleable.ParallaxImageView_parallaxDirection, mDirection);
			mAllowOutsideParent = a.getBoolean(R.styleable.ParallaxImageView_allowOutside, mAllowOutsideParent);
		} finally {
			a.recycle();
		}
	}

	public int getParallaxSpeed() {
		return mParallaxSpeed;
	}

	public void setParallaxSpeed(int parallaxSpeed) {
		mParallaxSpeed = parallaxSpeed;
	}

	public boolean isAllowOutsideParent() {
		return mAllowOutsideParent;
	}

	public void setAllowOutsideParent(boolean allowOutsideParent) {
		mAllowOutsideParent = allowOutsideParent;
	}

	public void setHorizontalTiltLevel(float tiltLevel) {
		mHorizontalTiltLevel = tiltLevel;
	}

	public void setVerticalTiltLevel(float tiltLevel) {
		mVerticalTiltLevel = tiltLevel;
	}

	@Override
	public void draw(Canvas canvas) {
		if ((mDirection & DIRECTION_HORIZONTAL) != 0) {
			translateX();
		}
		if ((mDirection & DIRECTION_VERTICAL) != 0) {
			translateY();
		}

		super.draw(canvas);
	}

	private void translateX() {
		int left = ((View) getParent()).getLeft();
		int right = ((View) getParent()).getRight() - getWidth();
		float ratio = right / 180f + mParallaxSpeed;

		float translation = mHorizontalTiltLevel * ratio;
		if (!mAllowOutsideParent) {
			if (translation + getLeft() < left) {
				translation = -getLeft();
			} else if (translation + getLeft() > right) {
				translation = right - getLeft();
			}
		}

		setTranslationX(translation);
	}

	private void translateY() {
		int top = ((View) getParent()).getTop();
		int bottom = ((View) getParent()).getBottom() - getHeight();
		float ratio = bottom / 180f + mParallaxSpeed;

		float translation = mVerticalTiltLevel * ratio;
		if (!mAllowOutsideParent) {
			if (translation + getTop() < top) {
				translation = -getTop();
			} else if (translation + getTop() > bottom) {
				translation = bottom - getTop();
			}
		}

		setTranslationY(translation);
	}

}
