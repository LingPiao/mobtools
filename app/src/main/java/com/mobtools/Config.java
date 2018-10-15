package com.mobtools;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.mobtools.weather.WeatherCache;


/**
 * @author Kong, LingPiao
 */
public class Config extends Activity {
    public static final String APP_CONFIG_KEY = "SK_FAIRY_CONF";
    public static final String CONFIG_AUTO_START = "CONFIG_AUTO_START";
    public static final String CONFIG_SPEED_THRESHOLD = "CONFIG_SPEED_THRESHOLD";
    public static final String CONFIG_CITY = "CONFIG_CITY";

    private SharedPreferences preferences = null;
    private Editor pEditor = null;
    private EditText cities;

    // private boolean isExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        LogUtil.d("Config onCreate calling...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        // Toast.makeText(Config.this, "Showing...", Toast.LENGTH_SHORT).show();

        preferences = getSharedPreferences(APP_CONFIG_KEY, Context.MODE_PRIVATE);
        pEditor = preferences.edit();

        cities = (EditText) findViewById(R.id.city);
        cities.setText(preferences.getString(CONFIG_CITY, ""));
        TextWatcher cw = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                pEditor.putString(CONFIG_CITY, cities.getText().toString());
                pEditor.commit();
                WeatherCache.getInstance(getApplicationContext()).setCityChanged(true);
            }
        };

        cities.addTextChangedListener(cw);

        activeAdmin();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LogUtil.d("Config onOptionsItemSelected,id=" + item.getItemId());
        int id = item.getItemId();
        if (id == R.id.action_about) {
            // startActivity(new Intent(this, AboutActivity.class));
        } else {
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected void onResume() {
        LogUtil.d("Config onResume calling...");
        super.onResume();
    }

    @Override
    protected void onPause() {
        LogUtil.d("Config onPause calling...");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LogUtil.d("Config onDestroy calling...");
        // stopService(sks);
        super.onDestroy();
    }

    private void activeAdmin() {
        DevicePolicyManager mDPM = (DevicePolicyManager) this.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName devAdminReceiver = new ComponentName(this, Darclass.class);
        boolean admin = mDPM.isAdminActive(devAdminReceiver);
        if (!admin) {
            LogUtil.d("Not admin pop activate admin...");
            Intent activateDeviceAdminIntent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            activateDeviceAdminIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, devAdminReceiver);
            activateDeviceAdminIntent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, this.getResources().getString(R.string.app_name));
            // activateDeviceAdminIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            int REQ_ACTIVATE_DEVICE_ADMIN = 10;
            this.startActivityForResult(activateDeviceAdminIntent, REQ_ACTIVATE_DEVICE_ADMIN);
        }

    }

}
