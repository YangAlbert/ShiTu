package com.shitu.indoor;

import android.app.Activity;
import android.os.Bundle;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

public class MapActivity extends Activity implements MapEventsReceiver {

    public static final GeoPoint GLODON = new GeoPoint(40.044771, 116.277071);

    private MapView mapView = null;

    private GeoPoint start = null;
    private GeoPoint end = null;

    org.osmdroid.bonuspack.overlays.Polyline mRoadLay = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapView.setUseDataConnection(false);
        mapView.setMaxZoomLevel(21);
        // attention!!: need to rename tile folder in .zip file to "CycleMap";
        mapView.setTileSource(TileSourceFactory.CYCLEMAP);

        IMapController mapViewController = mapView.getController();
        mapViewController.setZoom(15);
        mapViewController.setCenter(GLODON);

        start = new GeoPoint(40.0443964, 116.2776609);
        end = new GeoPoint(40.0449063, 116.2768361);
        showRouting();
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        if (start == null) {
            start = p;
        }
        else {
            end = p;
            showRouting();
        }

        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
//        return super.longPressHelper(p);
        return true;
    }

    private void showRouting()
    {
        if (mRoadLay != null) {
            mapView.getOverlays().remove(mRoadLay);
        }

        RoadManager rm = new OSRMRoadManager();

        ArrayList<GeoPoint> ptArray = new ArrayList<GeoPoint>();
        ptArray.add(start);
        ptArray.add(end);
        Road road = rm.getRoad(ptArray);

        mRoadLay = RoadManager.buildRoadOverlay(road, this);
        mapView.getOverlays().add(mRoadLay);

        mapView.invalidate();

        // invalidate start point.
        start = null;
    }
}
