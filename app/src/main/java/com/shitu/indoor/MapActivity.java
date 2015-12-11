package com.shitu.indoor;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;

import com.shitu.routing.Point3d;
import com.shitu.routing.ProjectPoint;
import com.shitu.routing.SimpleEdge3d;
import com.shitu.routing.OsmWaysParser;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.TilesOverlay;

import java.util.ArrayList;

public class MapActivity extends Activity implements MapEventsReceiver {

    public static final GeoPoint GLODON = new GeoPoint(40.044771, 116.277071);

    private MapView mapView = null;

    private GeoPoint start = null;
    private GeoPoint end = null;

    org.osmdroid.bonuspack.overlays.Polyline mRoadLay = null;

    // routing module.
    com.shitu.routing.RoadManager mRoadManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.setUseDataConnection(false);
        mapView.setMaxZoomLevel(21);

        initMapResource();

        initRoadManager();
//        testRouting();
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

    private void initMapResource() {
        final ITileSource tileSource = new XYTileSource("GlodonMap", 15, 21, 256, ".png", null);
        MapTileModuleProviderBase tileModuleProvider = new MapTileFileArchiveProvider(
                new SimpleRegisterReceiver(getApplicationContext()),
                tileSource, null);

        MapTileProviderBase mapProvider = new MapTileProviderArray(tileSource, null,
                new MapTileModuleProviderBase[] { tileModuleProvider });
        final TilesOverlay tileOverlay = new TilesOverlay(mapProvider, getBaseContext());
        mapView.getOverlays().add(tileOverlay);

        class OverlayMapListener implements MapListener {
            @Override
            public boolean onScroll(ScrollEvent e) {
                e.getSource().invalidate();

                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent e) {
//                e.getSource().invalidate();
                mapView.invalidate();

                return true;
            }
        }
        mapView.setMapListener(new OverlayMapListener());

        IMapController mapViewController = mapView.getController();
        mapViewController.setZoom(18);
        mapViewController.setCenter(GLODON);
    }

    private void initRoadManager() {
        String fileName = "osmdroid/Glodon_Outdoor.osm";// test.
        String path = Environment.getExternalStorageDirectory() + "/" + fileName;
        OsmWaysParser ways_parser = new OsmWaysParser(path);
        ArrayList<SimpleEdge3d> edgeList = ways_parser.GetRawWays();
        assert edgeList != null;

        mRoadManager = new com.shitu.routing.RoadManager(edgeList);
    }

    private void testRouting() {
        Point3d startPt = new Point3d(40.0450098982271, 116.27752735798, 6);
        Point3d endPt = new Point3d(40.0446070055428, 116.277199421868, 6);
        mRoadManager.SetStartPoint(startPt);
        mRoadManager.SetEndPoint(endPt);

        ArrayList<Point3d> way = mRoadManager.GetRoad();

        ProjectPoint projectPt = new ProjectPoint();
        projectPt.SetOriginPt(startPt);
        Point3d projectEndPt = projectPt.GetProjectivePoint(endPt);

        ArrayList<GeoPoint> ptArray = new ArrayList<>();
        ptArray.add(new GeoPoint(startPt.Lat(), startPt.Lon()));
        for (Point3d p : way) {
            ptArray.add(new GeoPoint(p.Lat(), p.Lon()));
        }
        ptArray.add(new GeoPoint(endPt.Lat(), endPt.Lon()));

        Polyline wayOverlay = new Polyline(this);
        wayOverlay.setPoints(ptArray);
        wayOverlay.setColor(0xff0000fb);
        wayOverlay.setWidth(3.0f);

        // start and end point overlay.

        Drawable startMk = getResources().getDrawable(R.drawable.start);
        OverlayItem startItem = new OverlayItem("StartPoint", "where you start", new GeoPoint(startPt.Lat(), startPt.Lon()));
        startItem.setMarker(startMk);

        Drawable endMk = getResources().getDrawable(R.drawable.dest);
        OverlayItem endItem = new OverlayItem("DestinationPoint", "where you go", new GeoPoint(endPt.Lat(), endPt.Lon()));
        endItem.setMarker(endMk);

        ArrayList<OverlayItem> itemArray = new ArrayList<>();
        itemArray.add(startItem);
        itemArray.add(endItem);

        final ResourceProxy resProxy = new DefaultResourceProxyImpl(getApplicationContext());
        ItemizedIconOverlay<OverlayItem> itemOverlay = new ItemizedIconOverlay<OverlayItem>(itemArray,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                return true;
            }
            public boolean onItemLongPress(final int index, final OverlayItem item) {
                return true;
            }
        }, resProxy);

        mapView.getOverlays().add(wayOverlay);
        mapView.getOverlays().add(itemOverlay);
        mapView.invalidate();
    }
}
