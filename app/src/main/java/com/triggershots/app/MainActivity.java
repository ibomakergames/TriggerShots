package com.triggershots.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {
    TextView speedText, sizeText;
    SeekBar speed, size;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout page=new LinearLayout(this); page.setOrientation(1); page.setPadding(32,24,32,24);
        page.setBackgroundColor(0xff202328);
        TextView title=new TextView(this); title.setText("TRIGGER SHOTS"); title.setTextSize(27); title.setTextColor(0xffeeeeee); title.setGravity(Gravity.CENTER);
        page.addView(title,new LinearLayout.LayoutParams(-1,75));
        TextView desc=new TextView(this); desc.setText("Hold A to repeat taps at the small target. Release to stop."); desc.setTextColor(0xffc3c7cc); desc.setTextSize(16);
        page.addView(desc);
        speedText=new TextView(this); speedText.setTextColor(-1); speedText.setTextSize(17); speedText.setPadding(0,22,0,6); page.addView(speedText);
        speed=new SeekBar(this); speed.setMax(29); page.addView(speed);
        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean u){ int n=p+1; speedText.setText("Tap rate: "+n+" per second"); getSharedPreferences("settings",0).edit().putInt("speed",n).apply(); }
            public void onStartTrackingTouch(SeekBar s){} public void onStopTrackingTouch(SeekBar s){}
        });
        sizeText=new TextView(this); sizeText.setTextColor(-1); sizeText.setTextSize(17); sizeText.setPadding(0,18,0,6); page.addView(sizeText);
        size=new SeekBar(this); size.setMax(110); page.addView(size);
        size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean u){
                int dp=70+p; sizeText.setText("A button size: "+dp+" dp");
                getSharedPreferences("settings",0).edit().putInt("trigger_size",dp).apply();
                Intent changed=new Intent("com.triggershots.app.SIZE_CHANGED"); changed.putExtra("size",dp); sendBroadcast(changed);
            }
            public void onStartTrackingTouch(SeekBar s){} public void onStopTrackingTouch(SeekBar s){}
        });
        Button overlay=new Button(this); overlay.setText("1. Allow display over other apps"); page.addView(overlay);
        overlay.setOnClickListener(v->{ if(!Settings.canDrawOverlays(this)) startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()))); });
        Button access=new Button(this); access.setText("2. Enable Trigger Shots accessibility"); page.addView(access);
        access.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        Button start=new Button(this); start.setText("3. Show floating buttons"); page.addView(start);
        start.setOnClickListener(v->{
            if(!Settings.canDrawOverlays(this)){ Toast.makeText(this,"Allow overlay permission first",Toast.LENGTH_LONG).show(); return; }
            Intent i=new Intent(this,OverlayService.class); startForegroundService(i);
            Toast.makeText(this,"Hold A to start. Release to stop. Drag the small A marker to choose the target.",Toast.LENGTH_LONG).show();
        });
        TextView note=new TextView(this); note.setTextColor(0xffaeb4bb); note.setTextSize(13); note.setPadding(0,18,0,0);
        note.setText("Enable Trigger Shots in Accessibility settings. If the target does not respond, confirm the accessibility service is switched on. Some apps/games block automated gestures.");
        page.addView(note);
        setContentView(page);
        int saved=getSharedPreferences("settings",0).getInt("speed",10); speed.setProgress(saved-1);
        int savedSize=getSharedPreferences("settings",0).getInt("trigger_size",104); size.setProgress(Math.max(0,Math.min(110,savedSize-70)));
    }
}
