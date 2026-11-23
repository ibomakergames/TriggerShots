package com.triggershots.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {
    TextView speedText,sizeText,opacityText;
    SeekBar speed,size,opacity;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout page=new LinearLayout(this); page.setOrientation(1); page.setPadding(32,24,32,24);
        page.setBackgroundColor(0xff202328);
        TextView title=new TextView(this); title.setText("TRIGGER SHOTS"); title.setTextSize(27); title.setTextColor(0xffeeeeee); title.setGravity(Gravity.CENTER);
        page.addView(title,new LinearLayout.LayoutParams(-1,75));
        TextView desc=new TextView(this); desc.setText("Hold A to tap repeatedly. Drag the large A to move it; release to stop."); desc.setTextColor(0xffc3c7cc); desc.setTextSize(16); page.addView(desc);
        speedText=label(page); speed=new SeekBar(this); speed.setMax(29); page.addView(speed);
        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean u){ int n=p+1; speedText.setText("Tap rate: "+n+" per second"); getSharedPreferences("settings",0).edit().putInt("speed",n).apply(); }
            public void onStartTrackingTouch(SeekBar s){} public void onStopTrackingTouch(SeekBar s){}
        });
        sizeText=label(page); size=new SeekBar(this); size.setMax(110); page.addView(size);
        size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean u){ int dp=70+p; sizeText.setText("A button size: "+dp+" dp"); getSharedPreferences("settings",0).edit().putInt("trigger_size",dp).apply(); broadcast("SIZE_CHANGED","size",dp); }
            public void onStartTrackingTouch(SeekBar s){} public void onStopTrackingTouch(SeekBar s){}
        });
        opacityText=label(page); opacity=new SeekBar(this); opacity.setMax(95); page.addView(opacity);
        opacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean u){ int a=40+p*215/95; opacityText.setText("Button opacity: "+(a*100/255)+"%"); getSharedPreferences("settings",0).edit().putInt("button_alpha",a).apply(); broadcast("ALPHA_CHANGED","alpha",a); }
            public void onStartTrackingTouch(SeekBar s){} public void onStopTrackingTouch(SeekBar s){}
        });
        Button overlay=new Button(this); overlay.setText("1. Allow display over other apps"); page.addView(overlay);
        overlay.setOnClickListener(v->{ if(!Settings.canDrawOverlays(this)) startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()))); });
        Button access=new Button(this); access.setText("2. Enable Trigger Shots accessibility"); page.addView(access);
        access.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        Button start=new Button(this); start.setText("3. Show floating buttons"); page.addView(start);
        start.setOnClickListener(v->{ if(!Settings.canDrawOverlays(this)){ Toast.makeText(this,"Allow overlay permission first",Toast.LENGTH_LONG).show(); return; } startForegroundService(new Intent(this,OverlayService.class)); Toast.makeText(this,"Hold A to tap. Drag the large A to move it.",Toast.LENGTH_LONG).show(); });
        TextView note=new TextView(this); note.setTextColor(0xffaeb4bb); note.setTextSize(13); note.setPadding(0,18,0,0); note.setText("Enable the accessibility service. Some apps/games may block automated gestures."); page.addView(note);
        setContentView(page);
        android.content.SharedPreferences prefs=getSharedPreferences("settings",0);
        speed.setProgress(Math.max(0,Math.min(29,prefs.getInt("speed",10)-1)));
        size.setProgress(Math.max(0,Math.min(110,prefs.getInt("trigger_size",104)-70)));
        opacity.setProgress(Math.max(0,Math.min(95,(prefs.getInt("button_alpha",170)-40)*95/215)));
    }
    TextView label(LinearLayout page){ TextView t=new TextView(this); t.setTextColor(-1); t.setTextSize(17); t.setPadding(0,18,0,6); page.addView(t); return t; }
    void broadcast(String action,String key,int value){ Intent i=new Intent("com.triggershots.app."+action); i.setPackage(getPackageName()); i.putExtra(key,value); sendBroadcast(i); }
}
