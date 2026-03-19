package com.corporate.finance.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    @Value("${weather.api-key}")
    private String apiKey;

    @Value("${weather.base-url}")
    private String baseUrl;

    private final Map<String, CachedWeather> weatherCache = new HashMap<>();

    private static final int CACHE_EXPIRY_MINUTES = 30;

    private static final int MAX_RETRY_COUNT = 3;

    private static final long RETRY_INTERVAL_MS = 1000;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static class CachedWeather {
        private final String weatherInfo;
        private final long timestamp;

        public CachedWeather(String weatherInfo) {
            this.weatherInfo = weatherInfo;
            this.timestamp = System.currentTimeMillis();
        }

        public String getWeatherInfo() {
            return weatherInfo;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRY_MINUTES * 60 * 1000;
        }
    }

    public String getWeather(String city) throws IOException {
        if (weatherCache.containsKey(city) && !weatherCache.get(city).isExpired()) {
            logger.info("使用缓存的天气数据: {}", city);
            return weatherCache.get(city).getWeatherInfo();
        }

        logger.info("查询天气: {}", city);
        int retryCount = 0;
        while (retryCount < MAX_RETRY_COUNT) {
            try {
                String encodedCity = java.net.URLEncoder.encode(city, "UTF-8");
                String url = baseUrl + "?city=" + encodedCity + "&key=" + apiKey + "&extensions=all&output=json";
                
                logger.debug("请求天气API: {}", url);
                
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Accept", "application/json; charset=utf-8")
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .build();
                
                Response response = client.newCall(request).execute();
                
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    logger.debug("API响应: {}", responseBody);
                    String weatherInfo = parseWeatherResponse(responseBody);
                    weatherCache.put(city, new CachedWeather(weatherInfo));
                    return weatherInfo;
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    logger.error("API错误: {} {}, 详细信息: {}", response.code(), response.message(), errorBody);
                    
                    String errorMsg = "错误：" + response.code() + " " + response.message() + "\n详细信息：" + errorBody;
                    logger.error(errorMsg);
                    return errorMsg;
                }
            } catch (Exception e) {
                retryCount++;
                logger.warn("查询天气失败 ({}次): {}", retryCount, e.getMessage());
                if (retryCount < MAX_RETRY_COUNT) {
                    try {
                        Thread.sleep(RETRY_INTERVAL_MS * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    String errorMsg = "查询天气失败：" + e.getMessage();
                    logger.error(errorMsg);
                    return errorMsg;
                }
            }
        }
        return "查询天气失败：达到最大重试次数";
    }

    public String getWeatherByCity(String city) throws IOException {
        return getWeather(city);
    }

    private String parseWeatherResponse(String responseBody) throws IOException {
        try {
            Map<String, Object> weatherData = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            
            String status = String.valueOf(weatherData.get("status"));
            if (!"1".equals(status)) {
                String info = (String) weatherData.get("info");
                logger.warn("API返回错误: {}", info);
                return "查询天气失败，错误信息：" + info;
            }
            
            List<Map<String, Object>> forecasts = null;
            if (weatherData.containsKey("forecasts")) {
                forecasts = objectMapper.convertValue(weatherData.get("forecasts"), new TypeReference<List<Map<String, Object>>>() {});
            }
            
            if (forecasts == null || forecasts.isEmpty()) {
                logger.error("API响应中缺少forecasts字段或数据为空");
                return "查询天气失败：未获取到天气数据";
            }
            
            Map<String, Object> forecast = forecasts.get(0);
            String cityName = (String) forecast.get("city");
            String province = (String) forecast.get("province");
            String reportTime = (String) forecast.get("reporttime");
            
            List<Map<String, Object>> casts = null;
            if (forecast.containsKey("casts")) {
                casts = objectMapper.convertValue(forecast.get("casts"), new TypeReference<List<Map<String, Object>>>() {});
            }
            
            if (casts == null || casts.isEmpty()) {
                logger.error("API响应中缺少casts字段或数据为空");
                return "查询天气失败：未获取到天气预报数据";
            }
            
            StringBuilder weatherInfo = new StringBuilder();
            weatherInfo.append(String.format("城市: %s %s\n", province, cityName));
            weatherInfo.append(String.format("发布时间: %s\n\n", reportTime));
            
            String[] weekDays = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
            
            for (int i = 0; i < casts.size() && i < 4; i++) {
                Map<String, Object> cast = casts.get(i);
                String date = (String) cast.get("date");
                String week = weekDays[Integer.parseInt((String) cast.get("week"))];
                String dayWeather = (String) cast.get("dayweather");
                String nightWeather = (String) cast.get("nightweather");
                String dayTemp = (String) cast.get("daytemp");
                String nightTemp = (String) cast.get("nighttemp");
                String dayWind = (String) cast.get("daywind");
                String nightWind = (String) cast.get("nightwind");
                String dayPower = (String) cast.get("daypower");
                String nightPower = (String) cast.get("nightpower");
                
                if (i == 0) {
                    weatherInfo.append("=== 今天天气 ===\n");
                    weatherInfo.append(String.format("日期: %s (%s)\n", date, week));
                    weatherInfo.append(String.format("白天: %s, 温度 %s°C, %s风 %s级\n", dayWeather, dayTemp, dayWind, dayPower));
                    weatherInfo.append(String.format("夜间: %s, 温度 %s°C, %s风 %s级\n", nightWeather, nightTemp, nightWind, nightPower));
                } else {
                    weatherInfo.append(String.format("\n=== %s预报 ===\n", i == 1 ? "明天" : (i == 2 ? "后天" : "大后天")));
                    weatherInfo.append(String.format("日期: %s (%s)\n", date, week));
                    weatherInfo.append(String.format("白天: %s, 温度 %s°C\n", dayWeather, dayTemp));
                    weatherInfo.append(String.format("夜间: %s, 温度 %s°C\n", nightWeather, nightTemp));
                }
            }
            
            logger.debug("解析天气数据成功: {}", cityName);
            return weatherInfo.toString();
        } catch (Exception e) {
            logger.error("解析天气响应失败: {}", e.getMessage());
            return "查询天气失败：数据解析错误";
        }
    }
}
