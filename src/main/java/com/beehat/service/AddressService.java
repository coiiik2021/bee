package com.beehat.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
@Service
public class AddressService {
    public String getProvinceNameByCode(String provinceCode) {
        return getNameFromApi("https://provinces.open-api.vn/api/p/" + provinceCode);
    }

    public String getDistrictNameByCode(String districtCode) {
        return getNameFromApi("https://provinces.open-api.vn/api/d/" + districtCode);
    }

    public String getWardNameByCode(String wardCode) {
        return getNameFromApi("https://provinces.open-api.vn/api/w/" + wardCode);
    }

    public String getNameFromApi(String url) {
        String name = "";
        try {
            // Gửi request tới API và lấy kết quả
            URL apiUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Phân tích cú pháp JSON và lấy tên
            JSONObject jsonResponse = new JSONObject(response.toString());
            name = jsonResponse.optString("name", "");
        } catch (IOException e) {
            System.err.println("Không thể truy cập API: " + e.getMessage());
        }
        return name;
    }
}
