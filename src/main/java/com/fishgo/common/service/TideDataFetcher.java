package com.fishgo.common.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//조석예보 API Fetcher
public class TideDataFetcher {
    private static volatile Map<String, String> cachedDataMap = new HashMap<>();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static final String URL = "http://www.khoa.go.kr/api/oceangrid/DataType/search.do";
    private static final String SERVICE_KEY = "wldhxng34hkddbsgm81lwldhxng34hkddbsgm81l==";
    private static final String[] OBS_CODES = {
        "SO_0732","SO_0733","SO_0734","SO_0735","SO_0736","SO_0737","SO_0555","DT_0054","SO_0739",
        "SO_0740","SO_0699","SO_0562","SO_0573","SO_0581","SO_0571","SO_0578","SO_0567","SO_0576",
        "SO_0564","SO_0563","SO_0706","SO_0708","SO_0712","SO_0701","SO_0702","SO_0703","SO_0704",
        "DT_0040","DT_0002","DT_0003","DT_0004","DT_0005","DT_0006","DT_0007","DT_0008","DT_0010",
        "DT_0011","DT_0012","DT_0013","DT_0016","DT_0017","DT_0018","DT_0021","DT_0023","DT_0028",
        "DT_0032","DT_0036","SO_0553","SO_0540","DT_0020","DT_0022","DT_0024","DT_0026","DT_0027",
        "DT_0029","DT_0031","DT_0035","DT_0044","DT_0047","DT_0050","DT_0048","DT_0051","SO_0549",
        "DT_0049","DT_0056","DT_0057","SO_0538","SO_0539","DT_0058","SO_0554","SO_0326","IE_0060",
        "DT_0038","DT_0025","DT_0001","DT_0052","DT_0014","DT_0037","DT_0046","DT_0039","DT_0041",
        "DT_0042","DT_0043","DT_0061","SO_0537","SO_0536","SO_0547","SO_0550","SO_0705","SO_0707",
        "SO_0709","SO_0710","SO_0711","SO_0700","DT_0059","DT_0060","SO_0551","SO_0552","IE_0062",
        "IE_0061","SO_0543","SO_0548","SO_0572","SO_0569","SO_0570","SO_0568","SO_0577","SO_0566",
        "SO_0565","SO_0574","SO_0731","SO_1251","SO_1252","SO_0757","SO_0755","SO_0754","SO_1256",
        "DT_0064","SO_1249","SO_1250","SO_1253","SO_1254","SO_1248","SO_0759","DT_0068","SO_0760",
        "SO_0753","SO_0631","SO_0752","SO_0761","DT_0067","DT_0091","SO_1255","SO_0758","SO_0756",
        "DT_0063","DT_0062","SO_1257","DT_0092","SO_1258","SO_1259","SO_1260","SO_1261","SO_1262",
        "SO_1263","SO_1264","SO_1265","SO_1266","SO_1267","DT_0093","SO_1268","SO_1270","SO_1271",
        "SO_1269",
    };

    private static final TideDataFetcher INSTANCE = new TideDataFetcher();
    private TideDataFetcher() {
        scheduleDataFetching();
    }

    public static TideDataFetcher getInstance() {
        return INSTANCE;
    }

    private void scheduleDataFetching() {
        Runnable fetchTask = this::fetchDataForAllStations;

        long initialDelay = calculateInitialDelay();
        scheduler.scheduleAtFixedRate(fetchTask, initialDelay, 1, TimeUnit.DAYS);
    }

    private void fetchDataForAllStations() {
        String todayDate = LocalDate.now().toString().replace("-", "");
        for (String obsCode : OBS_CODES) {
            try {
                System.out.println("url >>> "+URL + "?DataType=tideObsPreTab&Date=" + todayDate + "&ServiceKey=" + SERVICE_KEY + "&ObsCode=" + obsCode + "&ResultType=json");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(URL + "?DataType=tideObsPreTab&Date=" + todayDate + "&ServiceKey=" + SERVICE_KEY + "&ObsCode=" + obsCode + "&ResultType=json"))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    cachedDataMap.put(obsCode, response.body());
                    System.out.println("[INFO] Data fetched for ObsCode: " + obsCode);
                } else {
                    System.err.println("[ERROR] Failed to fetch data for ObsCode: " + obsCode + ", Status: " + response.statusCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private long calculateInitialDelay() {
        long currentMillis = System.currentTimeMillis();
        long nextMidnightMillis = LocalDate.now().plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        return nextMidnightMillis - currentMillis;
    }

    public String getCachedData(String obsCode) {
        return cachedDataMap.get(obsCode);
    }

    public static void main(String[] args) {
        TideDataFetcher fetcher = TideDataFetcher.getInstance();

        // 테스트 용도: 특정 관측소 데이터 가져오기
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("[FRONT REQUEST] Cached Data for DT_0001: " + fetcher.getCachedData("DT_0001"));
    }
}
