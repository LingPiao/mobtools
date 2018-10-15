package com.mobtools.weather;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CityWeather implements Serializable {
	private String city;
	private String date;
	private List<WeatherInfo> weatherInfos = new ArrayList<WeatherInfo>();

	public CityWeather(String date) {
		this.date = date;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public List<WeatherInfo> getWeatherInfos() {
		return weatherInfos;
	}

	public void addWeatherInfo(WeatherInfo weatherInfo) {
		weatherInfos.add(weatherInfo);
	}

	public String getDate() {
		if (date == null) {
			return "";
		}
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(city);
		sb.append(":");
		for (WeatherInfo wi : weatherInfos) {
			sb.append(wi.toString());
		}
		return sb.toString();
	}

}
