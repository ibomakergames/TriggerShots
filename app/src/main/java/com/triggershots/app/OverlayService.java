package com.triggershots.app;

import android.app.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public class OverlayService extends Service {
    WindowManager wm; WindowManager.LayoutParams lp;
    View trigger,target; Handler h=new Handler(Looper.getMainLooper());
    boolean running=false; int alpha=170; float targetX=540,targetY=900;
    Runnable clickLoop=new Runnable(){ public void run(){
        if(!running) return;
        TriggerAccessibilityService a=TriggerAccessibilityService.get();
        if(a!=null) a.tap(targetX,targetY);
        int speed=getSharedPreferences("settings",0).getInt("speed",10);
        h.postDelayed(this,Math.max(34,1000/speed));
    }};
    @Override public void onCreate(){ super.onCreate(); wm=(WindowManager)getSystemService(WINDOW_SERVICE); createNotification(); showButtons(); }
    private void createNotification(){
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel c=new NotificationChannel("trigger","Trigger Shots",NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
            Notification n=new Notification.Builder(this,"trigger").setContentTitle("Trigger Shots active").setContentText("Floating trigger is ready").setSmallIcon(android.R.drawable.ic_media_play).build();
            startForeground(71,n);
        }
    }
    GradientDrawable bg(int color,int radius){ GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); return d; }
    TextView button(String text,int size){
        TextView v=new TextView(this); v.setText(text); v.setTextColor(Color.WHITE); v.setTextSize(size); v.setGravity(Gravity.CENTER);
        v.setBackground(bg(Color.argb(alpha,125,130,138),100)); return v;
    }
    int type(){ return Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE; }
    WindowManager.LayoutParams params(int w,int h,int x,int y){
        return new WindowManager.LayoutParams(w,h,type(),WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
    }
    void showButtons(){
        trigger=button("HOLD",14); lp=params(94,94,80,500); lp.gravity=Gravity.TOP|Gravity.LEFT; lp.x=80; lp.y=500; wm.addView(trigger,lp);
        trigger.setOnTouchListener(new View.OnTouchListener(){
            public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){ running=true; h.removeCallbacks(clickLoop); clickLoop.run(); return true; }
                if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){ running=false; h.removeCallbacks(clickLoop); return true; }
                return true;
            }});
        target=button("•",22); WindowManager.LayoutParams tp=params(48,48,0,0); tp.gravity=Gravity.TOP|Gravity.LEFT; tp.x=300; tp.y=500;
        wm.addView(target,tp);
        target.setOnTouchListener(new View.OnTouchListener(){ int startX,startY; float touchX,touchY;
            public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){ startX=tp.x; startY=tp.y; touchX=e.getRawX(); touchY=e.getRawY(); return true; }
                if(e.getAction()==MotionEvent.ACTION_MOVE){ tp.x=startX+(int)(e.getRawX()-touchX); tp.y=startY+(int)(e.getRawY()-touchY); wm.updateViewLayout(target,tp);
                    targetX=tp.x+24; targetY=tp.y+24; return true; }
                return true;
            }});
        targetX=324; targetY=524;
    }
    @Override public int onStartCommand(Intent i,int f,int id){ return START_STICKY; }
    @Override public void onDestroy(){ running=false; h.removeCallbacks(clickLoop); if(trigger!=null) wm.removeView(trigger); if(target!=null) wm.removeView(target); super.onDestroy(); }
    @Override public IBinder onBind(Intent i){ return null; }
}
