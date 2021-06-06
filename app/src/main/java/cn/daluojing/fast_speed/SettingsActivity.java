package cn.daluojing.fast_speed;

import android.content.Intent;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class SettingsActivity extends AppCompatActivity {

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        SettingsFragment settingsFragment = new SettingsFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, settingsFragment)
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("飞速时空");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        SwitchPreferenceCompat sWork = settingsFragment.getSwitchWork();
        EditTextPreference sInfo = settingsFragment.getSpeedInfo();
        ListPreference sSuccess = settingsFragment.getSwitchSuccess();
        SwitchPreferenceCompat sSsr = settingsFragment.getSwitchWork();

        InfoShow showThread = new InfoShow(sInfo);
        CheckWorkSwitch workThread = new CheckWorkSwitch(sWork);

        executorService.scheduleAtFixedRate(workThread, 500, 1000, TimeUnit.MILLISECONDS);
//        executorService.scheduleAtFixedRate(showThread, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    class InfoShow implements Runnable{
        private final static String SPEED_TEST_SERVER_URI_DL = "http://ipv4.ikoula.testdebit.info/10M.iso";
        private static final int SPEED_TEST_DURATION = 3000;
        private static final int REPORT_INTERVAL = 1000;
        EditTextPreference sInfo;
        SpeedTestSocket speedTestSocket = new SpeedTestSocket();
        public InfoShow(EditTextPreference sInfo){
            this.sInfo = sInfo;
        }
        @Override
        public void run() {
            System.out.println("start running speed....");
            speedTestSocket.setSocketTimeout(SPEED_TEST_DURATION);
            // add a listener to wait for speedtest completion and progress
            speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

                @Override
                public void onCompletion(SpeedTestReport report) {
                    // called when download/upload is complete
                    System.out.println("[COMPLETED] rate in octet/s : " + report.getTransferRateOctet());
                    System.out.println("[COMPLETED] rate in bit/s   : " + report.getTransferRateBit());
                }

                @Override
                public void onError(SpeedTestError speedTestError, String errorMessage) {
                    // called when a download/upload error occur
                }

                @Override
                public void onProgress(float percent, SpeedTestReport report) {
                    // called to notify download/upload progress
                    System.out.println("[PROGRESS] progress : " + percent + "%");
                    System.out.println("[PROGRESS] rate in octet/s : " + report.getTransferRateOctet());
                    System.out.println("[PROGRESS] rate in bit/s   : " + report.getTransferRateBit());
                    sInfo.setText("网速:" + report.getTransferRateBit() + "/p->" + report.getTransferRateOctet());
                }
            });
            speedTestSocket.startFixedDownload(SPEED_TEST_SERVER_URI_DL, SPEED_TEST_DURATION, REPORT_INTERVAL);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class CheckWorkSwitch implements Runnable{
        SwitchPreferenceCompat sWork;

        public CheckWorkSwitch(SwitchPreferenceCompat sWork){
            this.sWork = sWork;
        }
        @Override
        public void run() {
            System.out.println("start running check....");
            if( true ){
                //切换飞行模式
                setAirPlaneMode(1);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                setAirPlaneMode(0);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setAirPlaneMode(int isOpen){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1){
            //4.2以下版本
            Settings.System.putInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON, isOpen);
        }else{
            Settings.Global.putInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, isOpen);
        }
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.putExtra("state", true);
        sendBroadcast(intent);
    }


    private long getTotalBytes(){
        return TrafficStats.getUidRxBytes(getApplicationContext().getApplicationInfo().uid) ==
                TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
        EditTextPreference speedInfo = findPreference("time_speed");
        ListPreference switchSuccess = findPreference("is_success");
        SwitchPreferenceCompat switchWork = findPreference("start_work");
        SwitchPreferenceCompat switchSSr = findPreference("connect_ssr");


        public SwitchPreferenceCompat getSwitchWork() {
            return switchWork;
        }

        public EditTextPreference getSpeedInfo() {
            return speedInfo;
        }

        public ListPreference getSwitchSuccess() {
            return switchSuccess;
        }

        public SwitchPreferenceCompat getSwitchSSr() {
            return switchSSr;
        }
    }




}