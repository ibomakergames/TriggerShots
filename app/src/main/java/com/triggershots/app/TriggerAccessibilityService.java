package com.triggershots.app;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class TriggerAccessibilityService extends AccessibilityService {
    private static TriggerAccessibilityService instance;
    private boolean gesturePending=false;
    public static TriggerAccessibilityService get(){return instance;}
    @Override public void onServiceConnected(){instance=this;}
    @Override public void onAccessibilityEvent(AccessibilityEvent e){}
    @Override public void onInterrupt(){gesturePending=false;}
    @Override public void onDestroy(){if(instance==this)instance=null;gesturePending=false;super.onDestroy();}
    public void tap(float x,float y){
        if(gesturePending)return;
        Path p=new Path();p.moveTo(x,y);
        // Keep each injected touch extremely short so normal screen interaction
        // is interrupted for the smallest possible interval.
        GestureDescription g=new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(p,0,10)).build();
        gesturePending=true;
        boolean accepted=dispatchGesture(g,new GestureResultCallback(){
            @Override public void onCompleted(GestureDescription gesture){gesturePending=false;}
            @Override public void onCancelled(GestureDescription gesture){gesturePending=false;}
        },null);
        if(!accepted)gesturePending=false;
    }
}
