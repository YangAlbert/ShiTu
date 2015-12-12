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

import com.shitu.routing.Edge3d;
import com.shitu.routing.OsmWaysParser;
import com.shitu.routing.Point3d;
import com.shitu.routing.Point3dList;
import com.shitu.routing.ProjectPoint;
import com.shitu.routing.Room;
import com.shitu.routing.SimpleEdge3d;

import junit.framework.Assert;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
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

import java.util.ArrayList;

public class MapActivity extends Activity implements MapEventsReceiver {

    public static final GeoPoint GLODON = new GeoPoint(40.044771, 116.277071);

    public static final String ROOM_NUMBER_TOKEN = "RoomNo.";
    public static final String SHOW_ROUTE_TOKEN = "ShowRoute";

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

    LinearLayout mEndInfoLayout = null;
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
        mapView.setMaxZoomLevel(22);

        // setup map event overlay.
        MapEventsOverlay eventOverlay = new MapEventsOverlay(this, this);
        mapView.getOverlays().add(0, eventOverlay);

        initMapResource();

        if (null == mRoadManager) {
            initRoadManager();
        }
//        testRouting();

        tryStartRoutingFromIntent();
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        // remove existing overlay first.
        if (null != mEndOverlay) {
            mapView.getOverlays().remove(mEndOverlay);
        }

        mEndOverlay = createMarkerOverlay2(p, R.drawable.dest);
        mEndPoint.setValue(p.getLatitude(), p.getLongitude(), 6);

        mEndInfo.setText("To: \n" + p.toString());
        mEndInfoLayout.setVisibility(View.VISIBLE);

        return true;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return true;
    }

    void initViewElements() {
        // hide nav info panel at first;
        mEndInfoLayout = (LinearLayout) findViewById(R.id.navInfoLayout);
        mEndInfoLayout.setVisibility(View.INVISIBLE);

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
        final ITileSource tileSource = new XYTileSource("GlodonMap", 19, 22, 256, ".png", null);
        MapTileModuleProviderBase tileModuleProvider = new MapTileFileArchiveProvider(
                new SimpleRegisterReceiver(getApplicationContext()),
                tileSource, null);

        MapTileProviderBase mapProvider = new MapTileProviderArray(tileSource, null,
                new MapTileModuleProviderBase[] { tileModuleProvider });

        mapView.setTileProvider(mapProvider);

        IMapController mapViewController = mapView.getController();
        mapViewController.setZoom(19);
        mapViewController.setCenter(GLODON);
    }

    private void initRoadManager() {
        String fileName = "osmdroid/Glodon_Outdoor.osm";// test.
        String path = Environment.getExternalStorageDirectory() + "/" + fileName;
        OsmWaysParser osmParser = new OsmWaysParser(path);

        ArrayList<SimpleEdge3d> edgeList = osmParser.GetRawWays();
        Assert.assertNotNull(edgeList);

        ArrayList<Room> roomList = osmParser.GetRawRooms();
        Assert.assertNotNull(roomList);

        mRoadManager = new com.shitu.routing.RoadManager(edgeList, roomList);

//        showAllRoads();
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
        Assert.assertNotNull(mRoadManager);
        Point3d pos = mRoadManager.GetRoomPosition(roomId);
        if (null != pos) {
            roomCoord.setValue(pos.Lat(), pos.Lon(), pos.Floor());
            return true;
        }
        else {
            return false;
        }
    }

    org.osmdroid.views.overlay.Overlay createMarkerOverlay2(Point3d pos, int markerId) {
        return createMarkerOverlay2(new GeoPoint(pos.Lat(), pos.Lon()), markerId);
    }

    org.osmdroid.views.overlay.Overlay createMarkerOverlay2(GeoPoint pos, int markerId) {
        Marker marker = new Marker(mapView);
        marker.setPosition(pos);
        marker.setIcon(getResources().getDrawable(markerId));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        marker.setTitle("Point Marker");
        marker.setDraggable(false);
        mapView.getOverlays().add(marker);
        mapView.invalidate();

        return marker;
    }

    void removeOverlay(org.osmdroid.views.overlay.Overlay overlay) {
        mapView.getOverlays().remove(overlay);
        mapView.invalidate();
    }

    void hideNavInfoLayout() {
        mEndInfoLayout.setVisibility(View.INVISIBLE);
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

                mEndInfo.setText("Room: " + mStartRoom);
                mEndInfoLayout.setVisibility(View.VISIBLE);
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
            Intent intent = new Intent(getApplicationContext(), nftActivity.class);
            startActivity(intent);
            finish();
        }
    };

    @Override
    public void onBackPressed() {
        // just chose the destination point.
        if (mEndInfoLayout.getVisibility() == View.VISIBLE) {
            mEndInfoLayout.setVisibility(View.INVISIBLE);

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

    public void showAllRoads() {

        ArrayList<Edge3d> edgeList = mRoadManager.getEdgeList();

        int count = 0;
        ArrayList<GeoPoint> geoPtArray = new ArrayList<>();
        for (Edge3d e : edgeList) {
            geoPtArray.clear();

            Point3d startPt = e.getStartPt();
            geoPtArray.add(new GeoPoint(startPt.Lat(), startPt.Lon()));

            Point3d endPt = e.getEndPt();
            geoPtArray.add(new GeoPoint(endPt.Lat(), endPt.Lon()));

            Polyline wayOverlay = new Polyline(this);
            wayOverlay.setPoints(geoPtArray);
            wayOverlay.setColor(0xfffb0000);
            wayOverlay.setWidth(4.0f);
            mapView.getOverlays().add(wayOverlay);

//            if (++count >= 50) break;
        }

        // create osm route overlay.
//        Polyline wayOverlay = new Polyline(this);
//        wayOverlay.setPoints(geoPtArray);
//        wayOverlay.setColor(0xfffb0000);
//        wayOverlay.setWidth(4.0f);
//
//        mapView.getOverlays().add(wayOverlay);
        mapView.invalidate();
    }
}
