package com.mobtools;

import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.RemoteViews;

public class ToolsWidgetProvider extends AppWidgetProvider {

    private static final String TOOLS_WIDGET_ACTION_OPERATOR_KEY = "TOOLS_WIDGET_ACTION_OPERATOR_KEY";

    private static RemoteViews remoteViews = null;
    public static final String ACTION_NAME = "ToolsWidgetProvider";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        LogUtil.d("==============updateAppWidget");
        if (remoteViews == null) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.tools_widget);
        }

        setListeners(context);
        LogUtil.d("==============updateAppWidget,setListeners()");

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String act = intent.getAction();
        LogUtil.d("==============onReceive,action=" + act);

        super.onReceive(context, intent);

        if (act.equals(ACTION_NAME)) {
            int operator = 0;

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                operator = bundle.getInt(TOOLS_WIDGET_ACTION_OPERATOR_KEY, 0);
            } else {
                LogUtil.d("==============bundle is null");
            }
            LogUtil.d("==============onReceive,operator=" + operator);
            if (operator == Switch.LOCK.getValue()) {
                DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                ComponentName devAdminReceiver = new ComponentName(context, Darclass.class);
                boolean admin = mDPM.isAdminActive(devAdminReceiver);
                if (admin) {
                    mDPM.lockNow();
                    LogUtil.d("==============Screen locked.");
                } else {
                    LogUtil.d("Not an admin");
                }
            }
        }

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
        LogUtil.d("==============onEnabled");
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        LogUtil.d("==============onDisabled");
    }


    private static void setListeners(Context context) {
        setLockClickListener(context);
    }


    private static void setLockClickListener(Context context) {
        Intent mylockIntent = new Intent(context, ToolsWidgetProvider.class);
        mylockIntent.setAction(ACTION_NAME);
        mylockIntent.putExtra(TOOLS_WIDGET_ACTION_OPERATOR_KEY, Switch.LOCK.getValue());
        PendingIntent mylockPi = PendingIntent.getBroadcast(context, Switch.LOCK.getValue(), mylockIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.locker, mylockPi);
    }


}
