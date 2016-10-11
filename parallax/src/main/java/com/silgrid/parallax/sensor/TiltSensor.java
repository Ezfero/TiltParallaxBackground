package com.silgrid.parallax.sensor;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class TiltSensor implements SensorEventListener {

	public interface SensorCallback {
		void onRotationChanged(float xAxis, float yAxis);
	}

	private Sensor mSensor;
	private Context mContext;
	private SensorManager mSensorManager;
	private SensorCallback mCallback;

	public TiltSensor(Context context, SensorCallback callback) {
		mContext = context;
		mCallback = callback;

		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float[]  degrees = rotationVectorToDegrees(event.values);
		mCallback.onRotationChanged(degrees[2], degrees[1]);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onResume() {
		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
	}

	public void onPause() {
		mSensorManager.unregisterListener(this);
	}

	private float[] rotationVectorToDegrees(float[] values) {
		float[] orientation = new float[3];
		float[] rotation = new float[9];

		SensorManager.getRotationMatrixFromVector(rotation, values);
		SensorManager.getOrientation(rotation, orientation);

		orientation[0] = (float) Math.toDegrees(orientation[0]); //Yaw
		orientation[1] = (float) Math.toDegrees(orientation[1]); //Pitch
		orientation[2] = (float) Math.toDegrees(orientation[2]); //Roll
		return transformOrientation(orientation);
	}

	private float[] transformOrientation(float[] values) {
		int orientation = mContext.getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			return values;
		} else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			float temp = values[1];
			values[1] = -values[2];
			values[2] = -temp;
			return values;
		}
		return values;
	}

}
