/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shitu.indoor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.shitu.orientation.sensors.Orientation;
import com.shitu.orientation.utils.OrientationSensorInterface;
import com.shitu.orientation.utils.TargetingDetector;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivity extends Activity {

    Orientation mOrientSensor = null;
    boolean mGestureTiggered = false;
    int mConditionCnt = 0;

    TargetingDetector mDetector = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
//        if (null == savedInstanceState) {
//            getFragmentManager().beginTransaction()
//                    .replace(R.id.container, Camera2BasicFragment.newInstance())
//                    .commit();
//        }

//        mDetector = new TargetingDetector(TargetingDetector.Target.FACING_UP, MapActivity.class, this, true);

        locationLocked();
    }

    void locationLocked() {
        Timer tm = new Timer("welcomeTimer");
        tm.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent mapActivity = new Intent(getApplicationContext(), MapActivity.class);
                mapActivity.putExtra(MapActivity.ROOM_NUMBER_TOKEN, 631);
                startActivity(mapActivity);

                finish();
            }
        }, new Date(System.currentTimeMillis() + 3000));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (null != mDetector) {
            mDetector.Start();
        }
    }

    @Override
    protected void onPause() {
        // turn orientation sensor off
        if (null != mDetector) {
            mDetector.Stop();
        }

        super.onPause();;
    }
}