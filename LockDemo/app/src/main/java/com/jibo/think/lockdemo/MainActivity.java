package com.jibo.think.lockdemo;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;


public class MainActivity extends Activity {

    private Button btLock;
    private Context mContext;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();
        initData();
    }

    private void initView() {
        btLock = (Button) findViewById(R.id.lock);
        powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TAG");

    }

    private void initData() {
        btLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lockScreen();
            }
        });


    }

    /**
     * 设置屏幕为常亮
     */
    public void acquire(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        wakeLock.acquire();
    }

    /**
     * 取消屏幕常亮
     */
    public void realse(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        wakeLock.acquire();
        wakeLock.release();
    }


    /**
     * 判断是否是自动亮度调节
     * @param aContentResolver
     * @return
     */
     public boolean isAutoBrightness(ContentResolver aContentResolver) {
            boolean automicBrightness = false;
            try{
                automicBrightness = Settings.System.getInt(aContentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
                 }catch(Settings.SettingNotFoundException e) {
                 e.printStackTrace();
           }
         return automicBrightness;
     }

   /*
    *获取屏幕的亮度
    */
    public int getScreenBrightness() {
       int nowBrightnessValue = 0;
       ContentResolver resolver = mContext.getContentResolver();
            try{
                nowBrightnessValue =
                        android.provider.Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS);
               } catch(Exception e) {
                 e.printStackTrace();
               }
       return nowBrightnessValue;
    }

    /**
     * 设置屏幕亮度的方法
     * @param brightness 0~255
     * @return
     */
    public  void setBrightness(int brightness) {
          WindowManager.LayoutParams lp = this.getWindow().getAttributes();
          lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
          this.getWindow().setAttributes(lp);
     }

    /**
     * 设置屏幕亮度模式为手动模式
     * Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC：值为1，自动调节亮度。
     * Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL：值为0，手动模式
     */
    public void setScrennManualMode() {
        ContentResolver contentResolver = mContext.getContentResolver();
        try {
            int mode = Settings.System.getInt(contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }


    /*
     *停止自动亮度调节
     */
    public void stopAutoBrightness(){
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;

    /**
     * 锁屏
     * 需要用户的主动授权
     */
    private void lockScreen() {
        mAdminName = new ComponentName(this, AdminManageReceiver.class);
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (mDPM.isAdminActive(mAdminName)) {
            mDPM.lockNow();
        } else if (!mDPM.isAdminActive(mAdminName)) {
            showAdminManagement(mAdminName);
        }
    }

    /**
     * 跳转设备管理器激活界面
     * @param mAdminName
     */
    private void showAdminManagement(ComponentName mAdminName) {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
          "Sono richiesti i privilegi di Amministratore Dispositivo per bloccare lo schermo");
        startActivityForResult(intent,1);
    }
}
