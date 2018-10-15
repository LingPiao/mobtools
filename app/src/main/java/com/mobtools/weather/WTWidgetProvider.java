package com.mobtools.weather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.mobtools.LogUtil;
import com.mobtools.R;
import com.mobtools.Switch;
import com.mobtools.Util;

import java.util.Map;

public class WTWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_NAME = "WTWidgetProvider";
    private static final String WT_WIDGET_ACTION_OPERATOR_KEY = "WT_WIDGET_ACTION_OPERATOR_KEY";
    private static RemoteViews remoteViews = null;
    private WTCNDataLoader dataLoader = null;
    private long updateTime = System.currentTimeMillis();
    private static final long WEATHER_UPDATE_INTERVAL = 3600000; //An hour

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        LogUtil.d("==============WTWidgetProvider.updateAppWidget");
        if (remoteViews == null) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.wt_widget);
        }

        LogUtil.d("==============WTWidgetProvider.updateAppWidget,setListeners()");
        setWeatherClickListener(context);
        setCityClickListener(context);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            LogUtil.d("==============onUpdate,appWidgetId=" + appWidgetId);
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        LogUtil.d("==============WTWidgetProvider.onEnabled()");
    }

    private boolean isInternetConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return false;
        }
        return true;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String act = intent.getAction();
        LogUtil.d("==============WTWidgetProvider.onReceive,action=" + act);
        if (dataLoader == null) {
            dataLoader = new WTCNDataLoader(this, context);
        }
        if (remoteViews == null) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.wt_widget);
        }
        int operator = 0;
        if (act.equals(ACTION_NAME)) {
            Bundle bundle = intent.getExtras();

            if (bundle != null) {
                operator = bundle.getInt(WT_WIDGET_ACTION_OPERATOR_KEY, 0);
            } else {
                LogUtil.d("==============bundle is null");
            }
            LogUtil.d("==============WTWidgetProvider.onReceive,operator=" + operator);
            if (operator == Switch.SWTICH_CITY.getValue()) {
                switchCity(context);
            } else {
                updateStatus(context);
            }
        } else if (act.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            LogUtil.d("==============WTWidgetProvider.WIFI_STATE_CHANGED");
            long current = System.currentTimeMillis();
            if (isUpdateRequired(updateTime, current)) {
                if (isNetEnable(context) && isInternetConnected(context)) {
                    LogUtil.d("==============WIFI_STATE_CHANGED. Loading weather info");
                    dataLoader.execute();
                    updateTime = current;
                    updateStatus(context);
                }
            }
        }
    }

    private boolean isUpdateRequired(long updateTime, long current) {
        if ((current - updateTime) >= WEATHER_UPDATE_INTERVAL) {
            return true;
        }
        //Coming a new day
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(new java.util.Date(updateTime));
        int d1 = c.get(java.util.Calendar.DAY_OF_MONTH);
        c.setTime(new java.util.Date(current));
        int d2 = c.get(java.util.Calendar.DAY_OF_MONTH);
        return (d2 - d1) >= 1;
    }

    private boolean isNetEnable(Context context) {
        android.net.wifi.WifiManager wifiManager = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();
        if (wifiEnabled) {
            return true;
        }
        ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNet = conman.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mNet != null) {
            if (mNet.isAvailable() && mNet.isConnected()) {
                return true;
            }
        }
        return false;
    }

    private void weatherWidgetClick(Context context) {
        LogUtil.d("==============WTWidgetProvider.weatherWidgetClick");
        if (WeatherCache.getInstance(context).isLoading()) {
            LogUtil.d("==============WTWidgetProvider is loading weather data...");
            return;
        }

        if (WeatherCache.getInstance(context).isUpdateRequired()) {
            LogUtil.d("==============WTWidgetProvider.weatherWidgetClick.isUpdateRequired");
            if (isInternetConnected(context)) {
                Util.msgBox(context, R.string.widget_weather_loading);
                dataLoader.execute();
            } else {
                if (WeatherCache.getInstance(context).getCachedWeatherInfos().size() > 0) {
                    // Update weather info from cache file if exists
                    updateWeatherInfo(context);
                } else {
                    LogUtil.d("==============WTWidgetProvider.weatherWidgetClick.isUpdateRequired.noInternet");
                    Util.msgBox(context, R.string.widget_weather_no_inet);
                }
            }
        } else {
            Util.msgBox(context, R.string.widget_weather_msg_latest);
        }
        LogUtil.d("==============WTWidgetProvider.weatherWidgetClick.setCityClickListener");

        setWeatherClickListener(context);
    }

    private static void setWeatherClickListener(Context context){
        LogUtil.d("==============WTWidgetProvider.setWeatherClickListener()");
        Intent wtIntent = new Intent(context, WTWidgetProvider.class);
        wtIntent.setAction(ACTION_NAME);
        wtIntent.putExtra(WT_WIDGET_ACTION_OPERATOR_KEY, Switch.WEATHER.getValue());
        PendingIntent wtPi = PendingIntent.getBroadcast(context, Switch.WEATHER.getValue(), wtIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.wtWidgetCtn, wtPi);
    }

    private void updateStatus(Context context) {
        LogUtil.d("==============WTWidgetProvider.updateStatus");
        weatherWidgetClick(context);
        updateWidget(context);
    }

    public void updateWidget(Context context) {
        AppWidgetManager appWidgetManger = AppWidgetManager.getInstance(context);
        appWidgetManger.updateAppWidget(new ComponentName(context, WTWidgetProvider.class), remoteViews);
    }

    public void updateWeatherInfo(Context context) {
        Map<String, CityWeather> wis = WeatherCache.getInstance(context).getCachedWeatherInfos();
        CityWeather cw = wis.get(dataLoader.getCities()[WeatherCache.getInstance(context).getCurrentCityIndex()]);
        updateWeatherInfo(context, cw);
    }

    private void updateWeatherInfo(Context context, CityWeather cw) {
        if (cw != null) {
            remoteViews.setTextViewText(R.id.city, cw.getCity());

            WeatherInfo today = cw.getWeatherInfos().get(0);

            remoteViews.setTextViewText(R.id.date, cw.getDate());
            remoteViews.setTextViewText(R.id.weather, today.getWeather() + Util.BLANK_STRING + today.getWind());
            remoteViews.setTextViewText(R.id.temperature, today.getTemperature());
            remoteViews.setImageViewResource(R.id.weatherIcon, Util.getTodayIconId(today.getWeather()));

            // Next1
            WeatherInfo d1 = cw.getWeatherInfos().get(1);
            remoteViews.setTextViewText(R.id.day1, d1.getDate());
            remoteViews.setTextViewText(R.id.day1Temperature, d1.getTemperature());
            remoteViews.setImageViewResource(R.id.day1Icon, Util.getDayIconId(d1.getWeather()));

            // Next2
            WeatherInfo d2 = cw.getWeatherInfos().get(2);
            remoteViews.setTextViewText(R.id.day2, d2.getDate());
            remoteViews.setTextViewText(R.id.day2Temperature, d2.getTemperature());
            remoteViews.setImageViewResource(R.id.day2Icon, Util.getDayIconId(d2.getWeather()));

            // Next3
            WeatherInfo d3 = cw.getWeatherInfos().get(3);
            remoteViews.setTextViewText(R.id.day3, d3.getDate());
            remoteViews.setTextViewText(R.id.day3Temperature, d3.getTemperature());
            remoteViews.setImageViewResource(R.id.day3Icon, Util.getDayIconId(d3.getWeather()));
        }
        setCityClickListener(context);
        updateWidget(context);
    }
    private static void setCityClickListener(Context context) {
        LogUtil.d("==============WTWidgetProvider.setCityClickListener()");
        Intent wtIntent = new Intent(context, WTWidgetProvider.class);
        wtIntent.setAction(ACTION_NAME);
        wtIntent.putExtra(WT_WIDGET_ACTION_OPERATOR_KEY, Switch.SWTICH_CITY.getValue());
        PendingIntent swCityPi = PendingIntent.getBroadcast(context, Switch.SWTICH_CITY.getValue(), wtIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.city, swCityPi);
        remoteViews.setOnClickPendingIntent(R.id.weatherIcon, swCityPi);
    }

    private void switchCity(Context context) {
        int currentCityIndex = WeatherCache.getInstance(context).getCurrentCityIndex();
        int max = WeatherCache.getInstance(context).getCachedWeatherInfos().size() - 1;
        LogUtil.d("==============WTWidgetProvider.switchCity(),max=" + max);
        if (max < 1)
            return;
        if (++currentCityIndex > max) {
            currentCityIndex = 0;
        }
        WeatherCache.getInstance(context).setCurrentCityIndex(currentCityIndex);
        updateWeatherInfo(context);
    }

}
