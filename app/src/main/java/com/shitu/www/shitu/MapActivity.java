package com.shitu.www.shitu;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MapActivity extends Activity {

    RelativeLayout mNavigateLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mNavigateLayout = (RelativeLayout) findViewById(R.id.navigate_layout);
        final TextView clickContent_tv = (TextView) findViewById(R.id.clickCotent_textView);


        // add listener to content clicker.
        TextView.OnClickListener tvListener = new TextView.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView tv = (TextView)view;

                clickContent_tv.setText("Destination: \n" + tv.getText());
                mNavigateLayout.setVisibility(View.VISIBLE);
            }
        };

        final TextView mapView_tv = (TextView) findViewById(R.id.mapView_textView);
        mapView_tv.setOnClickListener(tvListener);

        final TextView secondLine_tv = (TextView) findViewById(R.id.secondLine_textView);
        secondLine_tv.setOnClickListener(tvListener);
    }

    @Override
    public void onBackPressed() {
        assert mNavigateLayout != null;
        if (mNavigateLayout.getVisibility() == View.VISIBLE) {
            mNavigateLayout.setVisibility(View.INVISIBLE);
        }
        else {
            super.onBackPressed();
        }
    }
}
