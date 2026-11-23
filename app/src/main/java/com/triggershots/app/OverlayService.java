package com.triggershots.app;

import android.app.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.graphics.Color;

public class OverlayService extends Service {
    WindowManager wm;
    WindowManager.LayoutParams triggerLp,targetLp;
    View trigger,target;
    Handler h=new Handler(Looper.getMainLooper());
    boolean running=false,dragging=false;
    int alpha=170;
    float targetX=324,targetY=524;
    BroadcastReceiver settingsReceiver;
    Runnable clickLoop=new Runnable(){ public void run(){
        if(!running) return;
        TriggerAccessibilityService a=TriggerAccessibilityService.get();
        if(a!=null) a.tap(targetX,targetY);
        int speed=getSharedPreferences("settings",0).getInt("speed",10);
        h.postDelayed(this,Math.max(40,1000/speed));
    }};
    @Override public void onCreate(){
        super.onCreate(); wm=(WindowManager)getSystemService(WINDOW_SERVICE); createNotification(); showButtons();
        settingsReceiver=new BroadcastReceiver(){ @Override public void onReceive(Context c,Intent i){
            String action=i.getAction();
            if("com.triggershots.app.SIZE_CHANGED".equals(action)) resizeTrigger(i.getIntExtra("size",104));
            if("com.triggershots.app.ALPHA_CHANGED".equals(action)) setAlpha(i.getIntExtra("alpha",170));
        }};
        IntentFilter filter=new IntentFilter(); filter.addAction("com.triggershots.app.SIZE_CHANGED"); filter.addAction("com.triggershots.app.ALPHA_CHANGED");
        if(Build.VERSION.SDK_INT>=33) registerReceiver(settingsReceiver,filter,Context.RECEIVER_NOT_EXPORTED); else registerReceiver(settingsReceiver,filter);
    }
    private void createNotification(){
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel c=new NotificationChannel("trigger","Trigger Shots",NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
            Notification n=new Notification.Builder(this,"trigger").setContentTitle("Trigger Shots active").setContentText("Hold A to tap; drag A to move").setSmallIcon(android.R.drawable.ic_media_play).build();
            startForeground(71,n);
        }
    }
    GradientDrawable bg(int color){ GradientDrawable d=new GradientDrawable(); d.setShape(GradientDrawable.OVAL); d.setColor(color); return d; }
    TextView button(String text,int size){
        TextView v=new TextView(this); v.setText(text); v.setTextColor(Color.WHITE); v.setTextSize(size); v.setGravity(Gravity.CENTER);
        v.setBackground(bg(Color.argb(alpha,125,130,138))); return v;
    }
    int type(){ return Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE; }
    WindowManager.LayoutParams params(int w,int h,int x,int y){
        return new WindowManager.LayoutParams(w,h,type(),WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
    }
    int dp(int value){ return (int)(value*getResources().getDisplayMetrics().density+0.5f); }
    void showButtons(){
        android.content.SharedPreferences prefs=getSharedPreferences("settings",0);
        int size=prefs.getInt("trigger_size",104); alpha=prefs.getInt("button_alpha",170);
        trigger=button("A",18);
        triggerLp=params(dp(size),dp(size),0,0); triggerLp.gravity=Gravity.TOP|Gravity.LEFT;
        triggerLp.x=prefs.getInt("trigger_x",dp(80)); triggerLp.y=prefs.getInt("trigger_y",dp(500));
        wm.addView(trigger,triggerLp);
        trigger.setOnTouchListener(new View.OnTouchListener(){
            int downX,downY; float rawX,rawY; boolean moved;
            public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){
                    downX=triggerLp.x; downY=triggerLp.y; rawX=e.getRawX(); rawY=e.getRawY(); moved=false; dragging=false;
                    TriggerAccessibilityService a=TriggerAccessibilityService.get();
                    if(a==null){ Toast.makeText(OverlayService.this,"Enable Trigger Shots in Accessibility settings first",Toast.LENGTH_LONG).show(); return true; }
                    running=true; setTargetTouchable(false); h.removeCallbacks(clickLoop); clickLoop.run(); return true;
                }
                if(e.getAction()==MotionEvent.ACTION_MOVE){
                    int dx=(int)(e.getRawX()-rawX),dy=(int)(e.getRawY()-rawY);
                    if(!moved&&(Math.abs(dx)>dp(8)||Math.abs(dy)>dp(8))){ moved=true; dragging=true; running=false; h.removeCallbacks(clickLoop); setTargetTouchable(true); }
                    if(moved){ triggerLp.x=Math.max(0,downX+dx); triggerLp.y=Math.max(0,downY+dy); wm.updateViewLayout(trigger,triggerLp); }
                    return true;
                }
                if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){
                    running=false; h.removeCallbacks(clickLoop); setTargetTouchable(true);
                    if(moved){ getSharedPreferences("settings",0).edit().putInt("trigger_x",triggerLp.x).putInt("trigger_y",triggerLp.y).apply(); dragging=false; }
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
                if(e.getAction()==MotionEvent.ACTION_MOVE){ targetLp.x=startX+(int)(e.getRawX()-touchX); targetLp.y=startY+(int)(e.getRawY()-touchY); wm.updateViewLayout(target,targetLp); targetX=targetLp.x+dp(24); targetY=targetLp.y+dp(24); return true; }
                if(e.getAction()==MotionEvent.ACTION_UP){ targetX=targetLp.x+dp(24); targetY=targetLp.y+dp(24); return true; }
                return true;
            }});
    }
    void setTargetTouchable(boolean touchable){
        if(targetLp==null||target==null) return;
        if(touchable) targetLp.flags&=~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        else targetLp.flags|=WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        wm.updateViewLayout(target,targetLp);
    }
    void resizeTrigger(int size){ if(trigger==null||triggerLp==null)return; triggerLp.width=dp(size); triggerLp.height=dp(size); wm.updateViewLayout(trigger,triggerLp); }
    void setAlpha(int a){ alpha=Math.max(40,Math.min(255,a)); if(trigger!=null) trigger.setBackground(bg(Color.argb(alpha,125,130,138))); if(target!=null) target.setBackground(bg(Color.argb(alpha,125,130,138))); }
    @Override public int onStartCommand(Intent i,int f,int id){ return START_STICKY; }
    @Override public void onDestroy(){
        running=false; h.removeCallbacks(clickLoop); if(settingsReceiver!=null) try{unregisterReceiver(settingsReceiver);}catch(Exception ignored){}
        if(trigger!=null)wm.removeView(trigger); if(target!=null)wm.removeView(target); super.onDestroy();
    }
    @Override public IBinder onBind(Intent i){ return null; }
}
