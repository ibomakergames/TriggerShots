package com.triggershots.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    TextView speedText,sizeText,opacityText;
    SeekBar speed,size,opacity;
    EditText xInput,yInput;
    android.content.SharedPreferences prefs;
    int accent=0xff65f5bd, panel=0xff171e2b;
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs=getSharedPreferences("settings",0);
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(0xff0b1019);
        LinearLayout page=new LinearLayout(this); page.setOrientation(1); page.setPadding(dp(20),dp(22),dp(20),dp(28)); scroll.addView(page);
        LinearLayout hero=new LinearLayout(this); hero.setGravity(Gravity.CENTER_VERTICAL); hero.setPadding(0,0,0,dp(18));
        hero.addView(new LogoView(),new LinearLayout.LayoutParams(dp(72),dp(72)));
        LinearLayout brand=new LinearLayout(this); brand.setOrientation(1); brand.setPadding(dp(14),0,0,0);
        TextView title=new TextView(this); title.setText("TRIGGER SHOTS"); title.setTextColor(-1); title.setTextSize(25); title.setTypeface(null,1);
        TextView sub=new TextView(this); sub.setText("PRECISION TAP • GAME TOOL"); sub.setTextColor(accent); sub.setTextSize(11);
        brand.addView(title); brand.addView(sub); hero.addView(brand); page.addView(hero);
        TextView intro=new TextView(this); intro.setText("اضبط سرعة التكرار وموقع زر التشغيل، وحدد نقطة التصويب بالزر الصغير."); intro.setTextColor(0xffaab6c8); intro.setTextSize(14); intro.setPadding(0,0,0,dp(18)); page.addView(intro);
        LinearLayout speedCard=card(page,"01  •  TAP SETTINGS");
        speedText=label(speedCard); speed=new SeekBar(this); speed.setMax(29); styleSeek(speed); speedCard.addView(speed);
        speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean u){ int n=p+1; speedText.setText("سرعة التكرار: "+n+" ضغطة / ثانية"); prefs.edit().putInt("speed",n).apply(); }
            public void onStartTrackingTouch(SeekBar s){} public void onStopTrackingTouch(SeekBar s){}
        });
        sizeText=label(speedCard); size=new SeekBar(this); size.setMax(110); styleSeek(size); speedCard.addView(size);
        size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean u){ int d=70+p; sizeText.setText("حجم زر التشغيل: "+d+" dp"); prefs.edit().putInt("trigger_size",d).apply(); broadcast("SIZE_CHANGED","size",d); }
            public void onStartTrackingTouch(SeekBar s){} public void onStopTrackingTouch(SeekBar s){}
        });
        opacityText=label(speedCard); opacity=new SeekBar(this); opacity.setMax(95); styleSeek(opacity); speedCard.addView(opacity);
        opacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar s,int p,boolean u){ int a=40+p*215/95; opacityText.setText("شفافية الأزرار: "+(a*100/255)+"%"); prefs.edit().putInt("button_alpha",a).apply(); broadcast("ALPHA_CHANGED","alpha",a); }
            public void onStartTrackingTouch(SeekBar s){} public void onStopTrackingTouch(SeekBar s){}
        });
        LinearLayout posCard=card(page,"02  •  BUTTON POSITION");
        TextView posHint=new TextView(this); posHint.setText("حدد إحداثيات الزر الكبير X و Y بالبكسل ثم اضغط تطبيق. لن يتحرك الزر عند لمسه."); posHint.setTextColor(0xffaab6c8); posHint.setTextSize(13); posCard.addView(posHint);
        LinearLayout coords=new LinearLayout(this); coords.setOrientation(0); coords.setPadding(0,dp(10),0,dp(8));
        xInput=coordinate("X",prefs.getInt("trigger_x",dp(80))); yInput=coordinate("Y",prefs.getInt("trigger_y",dp(500)));
        coords.addView(xInput,new LinearLayout.LayoutParams(0,dp(48),1)); Space gap=new Space(this); coords.addView(gap,new LinearLayout.LayoutParams(dp(10),1)); coords.addView(yInput,new LinearLayout.LayoutParams(0,dp(48),1)); posCard.addView(coords);
        Button apply=gameButton("APPLY POSITION"); posCard.addView(apply); apply.setOnClickListener(v->{
            int x=parse(xInput,prefs.getInt("trigger_x",dp(80))), y=parse(yInput,prefs.getInt("trigger_y",dp(500)));
            prefs.edit().putInt("trigger_x",Math.max(0,x)).putInt("trigger_y",Math.max(0,y)).apply();
            Intent i=new Intent("com.triggershots.app.POSITION_CHANGED"); i.setPackage(getPackageName()); i.putExtra("x",Math.max(0,x)); i.putExtra("y",Math.max(0,y)); sendBroadcast(i);
            Toast.makeText(this,"تم تطبيق موقع الزر",Toast.LENGTH_SHORT).show();
        });
        LinearLayout control=card(page,"03  •  OVERLAY CONTROL");
        Button permission=gameButton("ALLOW OVERLAY"); control.addView(permission);
        permission.setOnClickListener(v->{if(!Settings.canDrawOverlays(this))startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName())));});
        Button access=gameButton("ENABLE ACCESSIBILITY"); control.addView(access); access.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        Button start=gameButton("START / SHOW BUTTONS"); control.addView(start); start.setOnClickListener(v->{if(!Settings.canDrawOverlays(this)){Toast.makeText(this,"Allow overlay permission first",Toast.LENGTH_LONG).show();return;} prefs.edit().putBoolean("overlay_visible",true).apply(); startForegroundService(new Intent(this,OverlayService.class)); broadcastVisibility(true);});
        Button hide=gameButton("HIDE / SHOW BUTTONS"); control.addView(hide); hide.setOnClickListener(v->{boolean visible=!prefs.getBoolean("overlay_visible",true); prefs.edit().putBoolean("overlay_visible",visible).apply(); if(!Settings.canDrawOverlays(this)){Toast.makeText(this,"Allow overlay permission first",Toast.LENGTH_LONG).show();return;} startForegroundService(new Intent(this,OverlayService.class)); broadcastVisibility(visible); hide.setText(visible?"HIDE FLOATING BUTTONS":"SHOW FLOATING BUTTONS");});
        TextView foot=new TextView(this); foot.setText("أثناء التشغيل اضغط زر A الكبير مرة لبدء التكرار ومرة أخرى لإيقافه. اسحب الزر الصغير لتحديد الهدف."); foot.setTextColor(0xff8190a5); foot.setTextSize(12); foot.setPadding(0,dp(14),0,0); control.addView(foot);
        setContentView(scroll);
        speed.setProgress(Math.max(0,Math.min(29,prefs.getInt("speed",10)-1)));
        size.setProgress(Math.max(0,Math.min(110,prefs.getInt("trigger_size",104)-70)));
        opacity.setProgress(Math.max(0,Math.min(95,(prefs.getInt("button_alpha",170)-40)*95/215)));
    }
    LinearLayout card(LinearLayout parent,String heading){ LinearLayout c=new LinearLayout(this); c.setOrientation(1); c.setPadding(dp(16),dp(14),dp(16),dp(16)); GradientDrawable bg=new GradientDrawable(); bg.setColor(panel); bg.setCornerRadius(dp(18)); c.setBackground(bg); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=dp(14); parent.addView(c,lp); TextView h=new TextView(this); h.setText(heading); h.setTextColor(accent); h.setTextSize(12); h.setTypeface(null,1); h.setPadding(0,0,0,dp(7)); c.addView(h); return c; }
    TextView label(LinearLayout p){TextView t=new TextView(this);t.setTextColor(-1);t.setTextSize(14);t.setPadding(0,dp(8),0,dp(2));p.addView(t);return t;}
    void styleSeek(SeekBar s){s.setProgressTintList(android.content.res.ColorStateList.valueOf(accent));s.setThumbTintList(android.content.res.ColorStateList.valueOf(accent));}
    EditText coordinate(String hint,int value){EditText e=new EditText(this);e.setSingleLine(true);e.setInputType(InputType.TYPE_CLASS_NUMBER);e.setHint(hint);e.setText(String.valueOf(value));e.setTextColor(-1);e.setHintTextColor(0xff8391a6);e.setPadding(dp(12),0,dp(12),0);GradientDrawable d=new GradientDrawable();d.setColor(0xff0e1521);d.setCornerRadius(dp(10));d.setStroke(dp(1),0xff2a3546);e.setBackground(d);return e;}
    int parse(EditText e,int fallback){try{return Integer.parseInt(e.getText().toString());}catch(Exception ex){return fallback;}}
    Button gameButton(String text){Button b=new Button(this);b.setText(text);b.setTextColor(0xff07130f);b.setTextSize(13);b.setTypeface(null,1);b.setAllCaps(false);GradientDrawable d=new GradientDrawable();d.setColor(accent);d.setCornerRadius(dp(12));b.setBackground(d);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(48));lp.topMargin=dp(6);b.setLayoutParams(lp);return b;}
    void broadcast(String action,String key,int value){Intent i=new Intent("com.triggershots.app."+action);i.setPackage(getPackageName());i.putExtra(key,value);sendBroadcast(i);}
    void broadcastVisibility(boolean visible){Intent i=new Intent("com.triggershots.app.OVERLAY_VISIBILITY");i.setPackage(getPackageName());i.putExtra("visible",visible);sendBroadcast(i);}
    int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    class LogoView extends View{
        Paint p=new Paint(3); LogoView(){super(MainActivity.this);}
        protected void onDraw(Canvas c){float w=getWidth(),h=getHeight();p.setColor(0xff121e2c);c.drawRoundRect(0,0,w,h,dp(18),dp(18),p);p.setColor(accent);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(3));c.drawCircle(w/2,h/2,w*.29f,p);c.drawCircle(w/2,h/2,w*.11f,p);c.drawLine(w*.5f,h*.12f,w*.5f,h*.35f,p);c.drawLine(w*.5f,h*.65f,w*.5f,h*.88f,p);c.drawLine(w*.12f,h*.5f,w*.35f,h*.5f,p);c.drawLine(w*.65f,h*.5f,w*.88f,h*.5f,p);p.setStyle(Paint.Style.FILL);p.setColor(-1);Path path=new Path();path.moveTo(w*.68f,h*.25f);path.lineTo(w*.88f,h*.19f);path.lineTo(w*.82f,h*.39f);path.close();c.drawPath(path,p);}
    }
}
