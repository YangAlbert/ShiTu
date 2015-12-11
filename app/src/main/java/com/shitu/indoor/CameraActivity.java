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

import com.shitu.indoor.R;

import org.osmdroid.views.MapController;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
//        if (null == savedInstanceState) {
//            getFragmentManager().beginTransaction()
//                    .replace(R.id.container, Camera2BasicFragment.newInstance())
//                    .commit();
//        }

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

}