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
	@IntDef({SPEED_SLOW, SPEED_NORMAL, SPEED_FAST})
	public @interface ParallaxSpeed {}

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({ORIENTAION_HORIZONTAL, ORIENTAION_VERTICAL, ORIENTAION_BOTH})
	public @interface ParallaxDirection {}

	public static final int SPEED_SLOW = 0;
	public static final int SPEED_NORMAL = 2;
	public static final int SPEED_FAST = 4;

	public static final int ORIENTAION_HORIZONTAL = 1;
	public static final int ORIENTAION_VERTICAL = 2;
	public static final int ORIENTAION_BOTH = 3;

	private int mParallaxSpeed;
	private boolean mAllowOutsideParent;

	private float mTiltLevel;

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
			mParallaxSpeed = a.getInt(R.styleable.ParallaxImageView_parallaxSpeed, 0);
			mAllowOutsideParent = a.getBoolean(R.styleable.ParallaxImageView_allowOutside, true);
		} finally {
			a.recycle();
		}

		setWillNotDraw(false);
	}

	@ParallaxSpeed
	public int getParallaxSpeed() {
		return mParallaxSpeed;
	}

	public void setParallaxSpeed(@ParallaxSpeed int parallaxSpeed) {
		mParallaxSpeed = parallaxSpeed;
	}

	public boolean isAllowOutsideParent() {
		return mAllowOutsideParent;
	}

	public ParallaxImageView setAllowOutsideParent(boolean allowOutsideParent) {
		mAllowOutsideParent = allowOutsideParent;
		return this;
	}

	public void setTiltLevel(float tiltLevel) {
		mTiltLevel = tiltLevel;
	}

	@Override
	public void draw(Canvas canvas) {
		setTranslationX(getTranslation());
		super.draw(canvas);
	}

	private float getTranslation() {
		int left = ((View) getParent()).getLeft();
		int right = ((View) getParent()).getRight() - getWidth();
		float coef = right / 180f + mParallaxSpeed;

		float translation = mTiltLevel * coef;
		if (!mAllowOutsideParent) {
			if (translation + getLeft() < left) {
				translation = -getLeft();
			} else if (translation + getLeft() > right) {
				translation = right - getLeft();
			}
		}
		return translation;
	}

}
