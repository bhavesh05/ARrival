package com.p532.arbusschedules;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import eu.kudan.kudan.ARActivity;
import eu.kudan.kudan.ARGyroPlaceManager;
import eu.kudan.kudan.ARImageTrackable;
import eu.kudan.kudan.ARImageTrackableListener;
import eu.kudan.kudan.ARImageTracker;
import eu.kudan.kudan.ARLightMaterial;
import eu.kudan.kudan.ARMeshNode;
import eu.kudan.kudan.ARModelImporter;
import eu.kudan.kudan.ARModelNode;
import eu.kudan.kudan.ARTexture2D;
import eu.kudan.kudan.ARView;

public class ARMainActivity extends ARActivity implements GestureDetector.OnGestureListener{

    public static final String TAG = "ARMainActivity";
    private RecyclerView recyclerView;
    private BusDetailAdapter busDetailAdapter;
    private List<BusDetail> busDetailList;
    private LocationManager locationManager;
    private static boolean ROTATE = false;
    private GestureDetectorCompat gestureDetect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_armain);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        busDetailList = new ArrayList<>();
        busDetailAdapter = new BusDetailAdapter(this, busDetailList);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.contentmain_rellayout);
        relativeLayout.setVisibility(View.GONE);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(busDetailAdapter);

        final Map<String,String> timeMap = new HashMap<>();
        BusDataRetrieval busDataRetrieval = new BusDataRetrieval(this,locationManager,timeMap, busDetailList, busDetailAdapter);
        busDataRetrieval.getBusData();

        gestureDetect = new GestureDetectorCompat(this,this);
    }
    @Override
    public void setup() {
        super.setup();
        ARImageTrackable arImageTrackable = new ARImageTrackable("Bloomington Transit");
        arImageTrackable.loadFromAsset("ARrival_Marker.jpg");

        ARImageTracker arImageTracker = ARImageTracker.getInstance();
        arImageTracker.initialise();
        ARModelNode modelNode = prepareModel();
        ARGyroPlaceManager gyroPlaceManager = ARGyroPlaceManager.getInstance();
        gyroPlaceManager.initialise();
        gyroPlaceManager.getWorld().addChild(modelNode);
        gyroPlaceManager.getWorld().findChildByName("Car").setPosition(0.0f, -100f, 0.0f);
        gyroPlaceManager.getWorld().findChildByName("Car").setVisible(false);
        rotateModel();

        arImageTracker.addTrackable(arImageTrackable);

        arImageTrackable.addListener(new ARImageTrackableListener() {
            @Override
            public void didDetect(ARImageTrackable arImageTrackable) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.contentmain_rellayout);
                        relativeLayout.setVisibility(View.VISIBLE);
                    }
                });
                makeModelVisible(false);
            }

            @Override
            public void didTrack(ARImageTrackable arImageTrackable) {

            }

            @Override
            public void didLose(ARImageTrackable arImageTrackable) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.contentmain_rellayout);
                        relativeLayout.setVisibility(View.GONE);
                    }
                });
                makeModelVisible(true);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        gestureDetect.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        ARView arView = this.getARView();
        ARGyroPlaceManager arGyroPlaceManager = ARGyroPlaceManager.getInstance();
        ARModelNode arModelNode = (ARModelNode) arGyroPlaceManager.getWorld().findChildByName("Car");

        if(ARHelper.isNodeSelected(arView, arModelNode, motionEvent, 300)){
            Log.d(TAG, "onSingleTapUp: Object Clicked" );
            launchLyft();
        }
        return false;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    private int dpToPx(int dp) {
        Resources resources = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics()));
    }

    private ARModelNode prepareModel(){
        ARModelImporter modelImporter = new ARModelImporter();
        modelImporter.loadFromAsset("delorean.jet");
        ARModelNode modelNode = modelImporter.getNode();
        modelNode.setName("Car");

        ARTexture2D texture2D = new ARTexture2D();
        texture2D.loadFromAsset("delorean.png");

        ARLightMaterial material = new ARLightMaterial();
        material.setTexture(texture2D);
        material.setAmbient(0.8f,0.8f,0.8f);
        for (ARMeshNode meshNode : modelImporter.getMeshNodes()) {
            meshNode.setMaterial(material);
        }

        modelNode.scaleByUniform(2.25f);
        return modelNode;
    }

    private void launchLyft(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Get Lyft Instead?");
        alertBuilder.setMessage("Tired of waiting? Want to get a Lyft instead?");
        alertBuilder.setNeutralButton("Get Me a Lyft", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                Intent lyftIntent = getPackageManager().getLaunchIntentForPackage("me.lyft.android");
                startActivity(lyftIntent);
            }
        });
        alertBuilder.setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog launchLyft = alertBuilder.create();
        launchLyft.show();
    }

    private void rotateModel(){
        new Thread(new Runnable() {

            ARGyroPlaceManager gyroPlaceManager = ARGyroPlaceManager.getInstance();
            @Override
            public void run() {
                while(true){
                    if(ROTATE) {
                        gyroPlaceManager.getWorld().findChildByName("Car").rotateByDegrees(1, 0, 1, 0);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    private void makeModelVisible(boolean modelVisible){
        ARGyroPlaceManager gyroPlaceManager = ARGyroPlaceManager.getInstance();
        gyroPlaceManager.getWorld().findChildByName("Car").setVisible(modelVisible);
        ROTATE = modelVisible;
    }
}
