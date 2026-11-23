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
    WindowManager wm; WindowManager.LayoutParams triggerLp,targetLp; View trigger,target;
    Handler h=new Handler(Looper.getMainLooper());
    boolean running=false,visible=true; int alpha=170; float targetX=324,targetY=524;
    BroadcastReceiver settingsReceiver;
    Runnable clickLoop=new Runnable(){public void run(){
        if(!running)return;
        TriggerAccessibilityService a=TriggerAccessibilityService.get();
        if(a!=null)a.tap(targetX,targetY);
        int speed=getSharedPreferences("settings",0).getInt("speed",10);
        h.postDelayed(this,Math.max(40,1000/speed));
    }};
    @Override public void onCreate(){
        super.onCreate();wm=(WindowManager)getSystemService(WINDOW_SERVICE);createNotification();showButtons();
        settingsReceiver=new BroadcastReceiver(){public void onReceive(Context c,Intent i){
            String a=i.getAction();
            if("com.triggershots.app.SIZE_CHANGED".equals(a))resizeTrigger(i.getIntExtra("size",104));
            if("com.triggershots.app.ALPHA_CHANGED".equals(a))setAlpha(i.getIntExtra("alpha",170));
            if("com.triggershots.app.POSITION_CHANGED".equals(a))setPosition(i.getIntExtra("x",0),i.getIntExtra("y",0));
            if("com.triggershots.app.OVERLAY_VISIBILITY".equals(a))setVisible(i.getBooleanExtra("visible",true));
        }};
        IntentFilter f=new IntentFilter();f.addAction("com.triggershots.app.SIZE_CHANGED");f.addAction("com.triggershots.app.ALPHA_CHANGED");f.addAction("com.triggershots.app.POSITION_CHANGED");f.addAction("com.triggershots.app.OVERLAY_VISIBILITY");
        if(Build.VERSION.SDK_INT>=33)registerReceiver(settingsReceiver,f,Context.RECEIVER_NOT_EXPORTED);else registerReceiver(settingsReceiver,f);
    }
    private void createNotification(){if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel("trigger","Trigger Shots",NotificationManager.IMPORTANCE_LOW);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);Notification n=new Notification.Builder(this,"trigger").setContentTitle("Trigger Shots active").setContentText("Hold A to tap; release to stop").setSmallIcon(android.R.drawable.ic_media_play).build();startForeground(71,n);}}
    GradientDrawable bg(int color){GradientDrawable d=new GradientDrawable();d.setShape(GradientDrawable.OVAL);d.setColor(color);return d;}
    TextView button(String text,int size){TextView v=new TextView(this);v.setText(text);v.setTextColor(Color.WHITE);v.setTextSize(size);v.setGravity(Gravity.CENTER);v.setBackground(bg(Color.argb(alpha,35,205,145)));return v;}
    int type(){return Build.VERSION.SDK_INT>=26?WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:WindowManager.LayoutParams.TYPE_PHONE;}
    WindowManager.LayoutParams params(int w,int h,int x,int y){return new WindowManager.LayoutParams(w,h,type(),WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);}
    int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    void showButtons(){
        android.content.SharedPreferences p=getSharedPreferences("settings",0);alpha=p.getInt("button_alpha",170);visible=p.getBoolean("overlay_visible",true);
        trigger=button("A",18);triggerLp=params(dp(p.getInt("trigger_size",104)),dp(p.getInt("trigger_size",104)),0,0);triggerLp.gravity=Gravity.TOP|Gravity.LEFT;triggerLp.x=Math.max(0,p.getInt("trigger_x",dp(80)));triggerLp.y=Math.max(0,p.getInt("trigger_y",dp(500)));wm.addView(trigger,triggerLp);
        trigger.setOnTouchListener(new View.OnTouchListener(){
            public boolean onTouch(View v,MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){
                    if(TriggerAccessibilityService.get()==null){Toast.makeText(OverlayService.this,"Enable Trigger Shots in Accessibility settings first",Toast.LENGTH_LONG).show();return true;}
                    startRunning(); return true;
                }
                if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){stopRunning();return true;}
                return true;
            }
        });
        target=button("A",16);targetLp=params(dp(48),dp(48),0,0);targetLp.gravity=Gravity.TOP|Gravity.LEFT;targetLp.x=p.getInt("target_x",dp(300));targetLp.y=p.getInt("target_y",dp(500));wm.addView(target,targetLp);targetX=targetLp.x+dp(24);targetY=targetLp.y+dp(24);
        target.setOnTouchListener(new View.OnTouchListener(){int sx,sy;float rx,ry;public boolean onTouch(View v,MotionEvent e){
            if(e.getAction()==MotionEvent.ACTION_DOWN){sx=targetLp.x;sy=targetLp.y;rx=e.getRawX();ry=e.getRawY();return true;}
            if(e.getAction()==MotionEvent.ACTION_MOVE){targetLp.x=Math.max(0,sx+(int)(e.getRawX()-rx));targetLp.y=Math.max(0,sy+(int)(e.getRawY()-ry));wm.updateViewLayout(target,targetLp);targetX=targetLp.x+dp(24);targetY=targetLp.y+dp(24);return true;}
            if(e.getAction()==MotionEvent.ACTION_UP){p.edit().putInt("target_x",targetLp.x).putInt("target_y",targetLp.y).apply();return true;}return true;
        }});
        setVisible(visible);
    }
    void startRunning(){if(running)return;running=true;setTargetTouchable(false);h.removeCallbacks(clickLoop);clickLoop.run();trigger.setBackground(bg(Color.argb(alpha,230,75,82)));((TextView)trigger).setText("●");}
    void stopRunning(){if(!running)return;running=false;h.removeCallbacks(clickLoop);setTargetTouchable(true);trigger.setBackground(bg(Color.argb(alpha,35,205,145)));((TextView)trigger).setText("A");}
    void setTargetTouchable(boolean touchable){if(targetLp==null||target==null)return;if(touchable)targetLp.flags&=~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;else targetLp.flags|=WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;wm.updateViewLayout(target,targetLp);}
    void setPosition(int x,int y){if(triggerLp==null||trigger==null)return;triggerLp.x=Math.max(0,x);triggerLp.y=Math.max(0,y);wm.updateViewLayout(trigger,triggerLp);}
    void setVisible(boolean show){visible=show;if(trigger!=null)trigger.setVisibility(show?View.VISIBLE:View.GONE);if(target!=null)target.setVisibility(show?View.VISIBLE:View.GONE);if(!show&&running){running=false;h.removeCallbacks(clickLoop);setTargetTouchable(true);if(trigger!=null){((TextView)trigger).setText("A");trigger.setBackground(bg(Color.argb(alpha,35,205,145)));}}}
    void resizeTrigger(int s){if(trigger==null||triggerLp==null)return;triggerLp.width=dp(s);triggerLp.height=dp(s);wm.updateViewLayout(trigger,triggerLp);}
    void setAlpha(int a){alpha=Math.max(40,Math.min(255,a));if(trigger!=null)trigger.setBackground(bg(Color.argb(alpha,running?230:35,running?75:205,running?82:145)));if(target!=null)target.setBackground(bg(Color.argb(alpha,35,205,145)));}
    @Override public int onStartCommand(Intent i,int f,int id){return START_STICKY;}
    @Override public void onDestroy(){running=false;h.removeCallbacks(clickLoop);if(settingsReceiver!=null)try{unregisterReceiver(settingsReceiver);}catch(Exception ignored){}if(trigger!=null)wm.removeView(trigger);if(target!=null)wm.removeView(target);super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}
