package com.triggershots.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    TextView speedText;
    SeekBar speed;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout page=new LinearLayout(this); page.setOrientation(1); page.setPadding(32,40,32,24);
        page.setBackgroundColor(0xff202328);
        TextView title=new TextView(this); title.setText("TRIGGER SHOTS"); title.setTextSize(27); title.setTextColor(0xffeeeeee); title.setGravity(Gravity.CENTER);
        page.addView(title,new LinearLayout.LayoutParams(-1,90));
        TextView desc=new TextView(this); desc.setText("Hold the large trigger to click at the small target. Release to stop."); desc.setTextColor(0xffc3c7cc); desc.setTextSize(16);
        page.addView(desc);
        speedText=new TextView(this); speedText.setTextColor(-1); speedText.setTextSize(18); speedText.setPadding(0,30,0,8); page.addView(speedText);
        speed=new SeekBar(this); speed.setMax(29); speed.setProgress(9); page.addView(speed);
        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean u){ int n=p+1; speedText.setText("Click speed: "+n+" clicks/sec"); getSharedPreferences("settings",0).edit().putInt("speed",n).apply(); }
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
            Toast.makeText(this,"Floating buttons shown. Move the small target to the tap location.",Toast.LENGTH_LONG).show();
        });
        TextView note=new TextView(this); note.setTextColor(0xffaeb4bb); note.setTextSize(13); note.setPadding(0,24,0,0);
        note.setText("Accessibility permission is required for touch gestures. Use only where permitted by the game/app. Android or a game may limit automated input.");
        page.addView(note);
        setContentView(page);
        int saved=getSharedPreferences("settings",0).getInt("speed",10); speed.setProgress(saved-1);
    }
}
