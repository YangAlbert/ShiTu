package com.shitu.orientation.utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.shitu.indoor.MapActivity;
import com.shitu.orientation.sensors.Orientation;

import java.security.Key;

/**
 * Created by yangrj on 2015/12/11.
 */
public class TargetingDetector implements OrientationSensorInterface {

    public enum Target {
        FACING_UP,
        FACING_USER,
    }

    Target mTarget = Target.FACING_UP;
    Class<?> mInvokeClass = null;
    Activity mSourceActivity = null;
    boolean mFinishSource = false;

    Orientation mOrientSensor = null;
    boolean mGestureTiggered = false;
    int mConditionCnt = 0;

    String mExtraKey = "";
    String mExtraValue = "";

    public TargetingDetector(Target tar, Class<?> cls, Activity srcActivity, boolean bFinish) {
        mTarget = tar;
        mInvokeClass = cls;
        mSourceActivity = srcActivity;
        mFinishSource = bFinish;

        mOrientSensor = new Orientation(mSourceActivity.getApplicationContext(), this);
        mOrientSensor.on(1);
    }

    public void SetExtra(String key, String value) {
        mExtraKey = key;
        mExtraValue = value;
    }

    public void Stop() {
        mOrientSensor.off();
    }

    public void Start() {
        mOrientSensor.on(1);
    }

    @Override
    public void orientation(Double azimuth, Double pitch, Double roll) {
        boolean bIsFacingUp = Math.abs(pitch) < 20.0f && Math.abs(roll) < 20.0f;

        if ((mTarget==Target.FACING_USER) ^ bIsFacingUp) {
            if (!mGestureTiggered && mConditionCnt>=10) {
                // stop sensor.
                mOrientSensor.off();
                mGestureTiggered = true;

                Intent intent = new Intent(mSourceActivity.getApplicationContext(), mInvokeClass);
                if (!mExtraKey.isEmpty()) {
                    intent.putExtra(mExtraKey, mExtraValue);
                }

                mSourceActivity.startActivity(intent);
                if (mFinishSource) {
                    mSourceActivity.finish();
                }
            }

            ++mConditionCnt;
        }
    }
}
