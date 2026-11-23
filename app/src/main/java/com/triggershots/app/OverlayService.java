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
    WindowManager wm;
    WindowManager.LayoutParams triggerLp, targetLp;
    View trigger,target;
    Handler h=new Handler(Looper.getMainLooper());
    boolean running=false;
    int alpha=170;
    float targetX=324,targetY=524;
    BroadcastReceiver sizeReceiver;
    Runnable clickLoop=new Runnable(){ public void run(){
        if(!running) return;
        TriggerAccessibilityService a=TriggerAccessibilityService.get();
        if(a!=null) a.tap(targetX,targetY);
        int speed=getSharedPreferences("settings",0).getInt("speed",10);
        h.postDelayed(this,Math.max(40,1000/speed));
    }};
    @Override public void onCreate(){
        super.onCreate();
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        createNotification();
        showButtons();
        sizeReceiver=new BroadcastReceiver(){ @Override public void onReceive(Context c,Intent i){ resizeTrigger(i.getIntExtra("size",104)); }};
        IntentFilter filter=new IntentFilter("com.triggershots.app.SIZE_CHANGED");
        if(Build.VERSION.SDK_INT>=33) registerReceiver(sizeReceiver,filter,Context.RECEIVER_NOT_EXPORTED); else registerReceiver(sizeReceiver,filter);
    }
    private void createNotification(){
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel c=new NotificationChannel("trigger","Trigger Shots",NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
            Notification n=new Notification.Builder(this,"trigger").setContentTitle("Trigger Shots active").setContentText("Hold A to tap; release to stop").setSmallIcon(android.R.drawable.ic_media_play).build();
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
    int dp(int value){ return (int)(value*getResources().getDisplayMetrics().density+0.5f); }
    void showButtons(){
        int size=getSharedPreferences("settings",0).getInt("trigger_size",104);
        trigger=button("A",18);
        triggerLp=params(dp(size),dp(size),80,500); triggerLp.gravity=Gravity.TOP|Gravity.LEFT; triggerLp.x=dp(80); triggerLp.y=dp(500);
        wm.addView(trigger,triggerLp);
        trigger.setOnTouchListener(new View.OnTouchListener(){
            public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){
                    TriggerAccessibilityService a=TriggerAccessibilityService.get();
                    if(a==null){ Toast.makeText(OverlayService.this,"Enable Trigger Shots in Accessibility settings first",Toast.LENGTH_LONG).show(); return true; }
                    running=true;
                    if(targetLp!=null){ targetLp.flags|=WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE; wm.updateViewLayout(target,targetLp); }
                    h.removeCallbacks(clickLoop); clickLoop.run(); return true;
                }
                if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){
                    running=false; h.removeCallbacks(clickLoop);
                    if(targetLp!=null){ targetLp.flags&=~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE; wm.updateViewLayout(target,targetLp); }
                    return true;
                }
                return true;
            }});
        target=button("A",16);
        targetLp=params(dp(48),dp(48),0,0); targetLp.gravity=Gravity.TOP|Gravity.LEFT; targetLp.x=dp(300); targetLp.y=dp(500);
        wm.addView(target,targetLp);
        targetX=targetLp.x+dp(24); targetY=targetLp.y+dp(24);
        target.setOnTouchListener(new View.OnTouchListener(){ int startX,startY; float touchX,touchY;
            public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){ startX=targetLp.x; startY=targetLp.y; touchX=e.getRawX(); touchY=e.getRawY(); return true; }
                if(e.getAction()==MotionEvent.ACTION_MOVE){ targetLp.x=startX+(int)(e.getRawX()-touchX); targetLp.y=startY+(int)(e.getRawY()-touchY); wm.updateViewLayout(target,targetLp);
                    targetX=targetLp.x+dp(24); targetY=targetLp.y+dp(24); return true; }
                if(e.getAction()==MotionEvent.ACTION_UP){ targetX=targetLp.x+dp(24); targetY=targetLp.y+dp(24); return true; }
                return true;
            }});
    }
    void resizeTrigger(int size){
        if(trigger==null||triggerLp==null) return;
        triggerLp.width=dp(size); triggerLp.height=dp(size);
        wm.updateViewLayout(trigger,triggerLp);
    }
    @Override public int onStartCommand(Intent i,int f,int id){ return START_STICKY; }
    @Override public void onDestroy(){
        running=false; h.removeCallbacks(clickLoop);
        if(sizeReceiver!=null) try{ unregisterReceiver(sizeReceiver); }catch(Exception ignored){}
        if(trigger!=null) wm.removeView(trigger); if(target!=null) wm.removeView(target);
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent i){ return null; }
}
