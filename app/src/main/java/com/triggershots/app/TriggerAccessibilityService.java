package com.triggershots.app;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class TriggerAccessibilityService extends AccessibilityService {
    private static TriggerAccessibilityService instance;
    public static TriggerAccessibilityService get(){ return instance; }
    @Override public void onServiceConnected(){ instance=this; }
    @Override public void onAccessibilityEvent(AccessibilityEvent e){}
    @Override public void onInterrupt(){}
    @Override public void onDestroy(){ if(instance==this) instance=null; super.onDestroy(); }
    public void tap(float x,float y){
        Path p=new Path(); p.moveTo(x,y);
        GestureDescription g=new GestureDescription.Builder()
            .addStroke(new GestureDescription.StrokeDescription(p,0,20)).build();
        dispatchGesture(g,null,null);
    }
}
