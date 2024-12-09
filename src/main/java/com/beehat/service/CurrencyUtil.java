package com.beehat.service;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtil {
    public static String formatCurrency(Integer amount) { // Chắc chắn rằng đây là Double hoặc kiểu dữ liệu phù hợp
        if (amount == null) {
            return "0 VND"; // Xử lý trường hợp amount là null
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }
}