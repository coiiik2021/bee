package com.beehat.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ProvincesService {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "https://provinces.open-api.vn/api";

    public List<Map<String, Object>> getProvinces() {
        String url = BASE_URL + "/p/";
        return restTemplate.getForObject(url, List.class);
    }

    public List<Map<String, Object>> getDistricts(int provinceId) {
        String url = BASE_URL + "/p/" + provinceId + "?depth=2";
        Map<String, Object> province = restTemplate.getForObject(url, Map.class);
        return (List<Map<String, Object>>) province.get("districts");
    }

    public List<Map<String, Object>> getWards(int districtId) {
        String url = BASE_URL + "/d/" + districtId + "?depth=2";
        Map<String, Object> district = restTemplate.getForObject(url, Map.class);
        return (List<Map<String, Object>>) district.get("wards");
    }
}
