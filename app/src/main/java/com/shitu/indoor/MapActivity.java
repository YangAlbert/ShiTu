package com.shitu.indoor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shitu.routing.OsmWaysParser;
import com.shitu.routing.Point3d;
import com.shitu.routing.Point3dList;
import com.shitu.routing.ProjectPoint;
import com.shitu.routing.Room;
import com.shitu.routing.SimpleEdge3d;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
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

public class MapActivity extends Activity/* implements MapEventsReceiver*/ {

    public static final GeoPoint GLODON = new GeoPoint(40.044771, 116.277071);

    public static final String ROOM_NUMBER_TOKEN = "RoomNo.";

    private MapView mapView = null;

    org.osmdroid.bonuspack.overlays.Polyline mRoadLay = null;

    // routing module.
    static com.shitu.routing.RoadManager mRoadManager = null;

    org.osmdroid.views.overlay.Overlay mStartOverlay = null;
    static Point3d mStartPoint = new Point3d();
    static String mStartRoom;
    org.osmdroid.views.overlay.Overlay mEndOverlay = null;
    static Point3d mEndPoint = new Point3d();

    org.osmdroid.views.overlay.Overlay mWayOverlay = null;

    LinearLayout mNavInfoLayout = null;
    TextView mEndInfo = null;

    LinearLayout mRoutingInfoLayout = null;
    TextView mRoutingInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initViewElements();

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapView.setUseDataConnection(false);
        mapView.setMaxZoomLevel(21);

        initMapResource();

        if (null == mRoadManager) {
            initRoadManager();
        }
//        testRouting();

        tryStartRoutingFromIntent();
    }

//    @Override
//    public boolean singleTapConfirmedHelper(GeoPoint p) {
//        mapView.requestLayout();
//        mapView.invalidate();
//        return true;
//    }
//
//    @Override
//    public boolean longPressHelper(GeoPoint p) {
//        return true;
//    }

    void initViewElements() {
        // hide nav info panel at first;
        mNavInfoLayout = (LinearLayout) findViewById(R.id.navInfoLayout);
        mNavInfoLayout.setVisibility(View.INVISIBLE);

        mEndInfo = (TextView) findViewById(R.id.destInfoText);

        mRoutingInfoLayout = (LinearLayout) findViewById(R.id.routingInfoLayout);
        mRoutingInfoLayout.setVisibility(View.INVISIBLE);

        mRoutingInfo = (TextView) findViewById(R.id.routingInfoText);

        // install button listener;
        Button searchBttn = (Button) findViewById(R.id.searchButton);
        searchBttn.setOnClickListener(mSearchButtonListener);

        Button navButton = (Button) findViewById(R.id.navButton);
        navButton.setOnClickListener(mNavButtonListener);
    }

    private void initMapResource() {
        final ITileSource tileSource = new XYTileSource("GlodonMap", 15, 21, 256, ".png", null);
        MapTileModuleProviderBase tileModuleProvider = new MapTileFileArchiveProvider(
                new SimpleRegisterReceiver(getApplicationContext()),
                tileSource, null);

        MapTileProviderBase mapProvider = new MapTileProviderArray(tileSource, null,
                new MapTileModuleProviderBase[] { tileModuleProvider });
//        final TilesOverlay tileOverlay = new TilesOverlay(mapProvider, getBaseContext());
//        mapView.getOverlays().add(tileOverlay);

        mapView.setTileProvider(mapProvider);

        class OverlayMapListener implements MapListener {
            @Override
            public boolean onScroll(ScrollEvent e) {
                e.getSource().invalidate();

                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent e) {
//                e.getSource().invalidate();
                mapView.requestLayout();
                mapView.invalidate();

                return true;
            }
        }
        mapView.setMapListener(new OverlayMapListener());

        IMapController mapViewController = mapView.getController();
        mapViewController.setZoom(19);
        mapViewController.setCenter(GLODON);
    }

    private void initRoadManager() {
        String fileName = "osmdroid/Glodon_Outdoor.osm";// test.
        String path = Environment.getExternalStorageDirectory() + "/" + fileName;
        OsmWaysParser ways_parser = new OsmWaysParser(path);
        ArrayList<SimpleEdge3d> edgeList = ways_parser.GetRawWays();
        assert edgeList != null;
        ArrayList<Room> roomList = ways_parser.GetRawRooms();

        mRoadManager = new com.shitu.routing.RoadManager(edgeList, roomList);
    }

    private void testRouting() {
        Point3d startPt = new Point3d(40.0450098982271, 116.27752735798, 6);
        Point3d endPt = new Point3d(40.0446070055428, 116.277199421868, 6);
        mRoadManager.SetStartPoint(startPt);
        mRoadManager.SetEndPoint(endPt);

        ArrayList<Point3d> way = mRoadManager.GetRoad().getPointList();

        ProjectPoint projectPt = new ProjectPoint(startPt);
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

    void tryStartRoutingFromIntent() {
        int RoomNum = getIntent().getIntExtra(ROOM_NUMBER_TOKEN, -1);
        if (-1 != RoomNum && getRoomLocation(RoomNum, mStartPoint)) {
            boolean bSucceed = Routing();
            if (!bSucceed) {
                Toast.makeText(getApplicationContext(), "Road NOT FOUND. ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    boolean Routing() {
        mRoadManager.SetStartPoint(mStartPoint);
        mRoadManager.SetEndPoint(mEndPoint);

        Point3dList road = mRoadManager.GetRoad();
        ArrayList<Point3d> way = road.getPointList();
        if (way.isEmpty()) {
            return false;
        }

        // create osm:GeoPoint list.
        ArrayList<GeoPoint> ptArray = new ArrayList<>();

        // put start pt.
        ptArray.add(new GeoPoint(mStartPoint.Lat(), mStartPoint.Lon()));
        for (Point3d p : way) {
            ptArray.add(new GeoPoint(p.Lat(), p.Lon()));
        }

        // put end pt.
        ptArray.add(new GeoPoint(mEndPoint.Lat(), mEndPoint.Lon()));

        // create osm route overlay.
        Polyline wayOverlay = new Polyline(this);
        wayOverlay.setPoints(ptArray);
        wayOverlay.setColor(0xff0000fb);
        wayOverlay.setWidth(3.0f);

        mapView.getOverlays().add(wayOverlay);
        mWayOverlay = wayOverlay;

        // create start & end point overlay.
        mStartOverlay = createMarkerOverlay2(mStartPoint, R.drawable.start);
        mEndOverlay = createMarkerOverlay2(mEndPoint, R.drawable.dest);

        // refresh info panel.
        mRoutingInfo.setText("Room: " + mStartRoom + "\nDistance: " + (int)road.GetLength_GeoCoord() + "m away!");
        mRoutingInfoLayout.setVisibility(View.VISIBLE);

        return true;
    }

    boolean getRoomLocation(int roomId, Point3d roomCoord) {
        if (roomId == 603) {
            roomCoord.setValue(40.0443256069883, 116.277749070418, 6);
            return  true;
        }
        else  if (roomId == 631) {
            roomCoord.setValue(40.0446795680168, 116.276742256975, 6);
            return true;
        }
        else {
            return false;
        }
    }

    org.osmdroid.views.overlay.Overlay createMarkerOverlay2(Point3d pos, int markerId) {
        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(pos.Lat(), pos.Lon()));
        marker.setIcon(getResources().getDrawable(markerId));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        marker.setTitle("Point Marker");
        marker.setDraggable(false);
        mapView.getOverlays().add(marker);
        mapView.invalidate();

        return marker;
    }

//    ItemizedIconOverlay<OverlayItem> createMarkerOverlay(Point3d pos, int markerId) {
//        GeoPoint geoPt = new GeoPoint(pos.Lat(), pos.Lon());
//        OverlayItem item = new OverlayItem("Marker", "it's a marker", geoPt);
//
//        Drawable marker = getResources().getDrawable(markerId);
//        item.setMarker(marker);
//
//        ArrayList<OverlayItem> itemArray = new ArrayList<>();
//        itemArray.add(item);
//
//        final ResourceProxy resProxy = new DefaultResourceProxyImpl(getApplicationContext());
//        ItemizedIconOverlay<OverlayItem> iconOverlay = new ItemizedIconOverlay<OverlayItem>(itemArray,
//                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
//                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
//                        return true;
//                    }
//                    public boolean onItemLongPress(final int index, final OverlayItem item) {
//                        return true;
//                    }
//                }, resProxy);
//        mapView.getOverlays().add(iconOverlay);
//
//        mapView.getController().setZoom(20);
//        mapView.getController().setCenter(geoPt);
//
//        mapView.invalidate();
//
//        return iconOverlay;
//    }

    void removeOverlay(org.osmdroid.views.overlay.Overlay overlay) {
        mapView.getOverlays().remove(overlay);
        mapView.invalidate();
    }

    void hideNavInfoLayout() {
        mNavInfoLayout.setVisibility(View.INVISIBLE);
    }

    Button.OnClickListener mSearchButtonListener = new Button.OnClickListener() {
        public void onClick(View v) {
            // on clicking the button, hide the keyboard first.
            InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

            // remove previously created destination mark.
            if (null != mEndOverlay) {
                removeOverlay(mEndOverlay);
                mEndOverlay = null;
            }

            EditText text = (EditText)findViewById(R.id.searchText);
            mStartRoom = text.getText().toString();

            if (getRoomLocation(Integer.parseInt(mStartRoom), mEndPoint)) {
                mEndOverlay = createMarkerOverlay2(mEndPoint, R.drawable.dest);
                mapView.getController().setCenter(new GeoPoint(mEndPoint.Lat(), mEndPoint.Lon()));
                mapView.getController().setZoom(20);

                mEndInfo.setText("Target: " + mStartRoom);
                mNavInfoLayout.setVisibility(View.VISIBLE);
            }
            else {
                Toast.makeText(getApplicationContext(), "Room #" + mStartRoom + "# NOT FOUND!",
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    Button.OnClickListener mNavButtonListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
            startActivity(intent);
            finish();
        }
    };

    @Override
    public void onBackPressed() {
        // just chose the destination point.
        if (mNavInfoLayout.getVisibility() == View.VISIBLE) {
            mNavInfoLayout.setVisibility(View.INVISIBLE);

            removeOverlay(mEndOverlay);
            mEndOverlay = null;
        }
        // showing the route.
        else if (mRoutingInfoLayout.getVisibility() == View.VISIBLE) {
            mRoutingInfoLayout.setVisibility(View.INVISIBLE);

            removeOverlay(mWayOverlay);
            mWayOverlay = null;

            removeOverlay(mStartOverlay);
            mStartOverlay = null;

            removeOverlay(mEndOverlay);
            mEndOverlay = null;
        }
        // no previous active event, return directly.
        else {
            super.onBackPressed();
        }
    }
}
