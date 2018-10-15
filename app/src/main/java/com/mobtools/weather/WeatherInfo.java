package com.mobtools.weather;

import com.mobtools.LogUtil;

import java.io.Serializable;

public class WeatherInfo implements Serializable {

    private static final String W_SEPARATOR = " ";
    private static final String TEMP_UNIT = "℃";

    private String date;
    private String weather;
    private String temperature;
    private String wind;
    private String dayIcon;
    private String nightIcon;
    private boolean loaded = false;
    private String error;

    /**
     * 21日（今天） 阴转多云 22/14℃ 3-4级转微风
     *
     * @param weatherInfo
     * @return
     */
    public static WeatherInfo build(String weatherInfo) {
        WeatherInfo wi = new WeatherInfo();
        try {
            String[] infos = weatherInfo.trim().split(W_SEPARATOR);
            wi.setDate(infos[0].substring(infos[0].indexOf("（") + 1, infos[0].indexOf("）")));
            wi.setWeather(truncateWeatherInfo(infos[1]));
            wi.setTemperature(infos[2].replaceAll(TEMP_UNIT, "") + TEMP_UNIT); // 22℃/32℃ -> 22/32℃
            wi.setWind(infos[3]);

        } catch (Exception e) {
            LogUtil.v("Get weather info error:" + e.getLocalizedMessage());
            wi.setLoaded(false);
            return wi;
        }
        wi.setLoaded(true);
        return wi;
    }

    private static String truncateWeatherInfo(String weatherInfo) {
        if (weatherInfo == null) {
            return null;
        }
        int i = weatherInfo.indexOf("转");
        int j = weatherInfo.indexOf("到");
        if (i > 0) {
            return weatherInfo.substring(0, i);
        } else if (j > 0) {
            return weatherInfo.substring(j + 1);
        }
        return weatherInfo;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public String getDayIcon() {
        return dayIcon;
    }

    public void setDayIcon(String dayIcon) {
        this.dayIcon = dayIcon;
    }

    public String getNightIcon() {
        return nightIcon;
    }

    public void setNightIcon(String nightIcon) {
        this.nightIcon = nightIcon;
    }

    @Override
    public String toString() {
        return date + "," + weather + "," + temperature + "\n";
    }

}
