package com.mobtools.weather;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by kongling on 5/12/2015.
 */
public class ConfUtil {

	public static final String CITY_SEPARATOR_ZH = "，";
	public static final String CITY_SEPARATOR = ",";

	public static String[] getConfiguredCities(Context wtContext) {
		SharedPreferences preferences = wtContext.getSharedPreferences(com.mobtools.Config.APP_CONFIG_KEY,
				Context.MODE_PRIVATE);
		String ct = preferences.getString(com.mobtools.Config.CONFIG_CITY, "");
		if (ct != null && ct.trim().length() > 0) {
			String sp = ct.indexOf(CITY_SEPARATOR_ZH) > 0 ? CITY_SEPARATOR_ZH : CITY_SEPARATOR;
			String[] cities = ct.split(sp);
			for (int i = 0; i < cities.length; i++) {
				//cities[i] = Util.encodeURLWithUTF8(cities[i].trim());
				cities[i] = cities[i].trim();
			}
			return cities;

		}
		// Default
		//return new String[] { Util.encodeURLWithUTF8("上海") };
		return new String[] { "上海" };
	}
}
