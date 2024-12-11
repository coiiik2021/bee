package com.beehat.service;


import com.beehat.entity.PaymentHistory;
import com.beehat.repository.PaymentHistoryRepo;
import javassist.compiler.ast.Pair;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashBoard {
    PaymentHistoryRepo paymentHistoryRepo;


    public int[] total(){
        List<PaymentHistory> paymentHistories = paymentHistoryRepo.findAll();
        int total = 0;
        for (PaymentHistory paymentHistory : paymentHistories) {
            total += paymentHistory.getAmountPaid();
        }

        return new int[]{ paymentHistories.size(), total};
    }

    public Map<String, List<Integer>> getDoanhThuTongHop(LocalDate startDate, LocalDate endDate) {
        // Số ngày giữa startDate và endDate
        int daysInRange = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // Tạo danh sách doanh thu mặc định với giá trị 0
        List<Integer> onlineSales = new ArrayList<>(Collections.nCopies(daysInRange, 0));
        List<Integer> offlineSales = new ArrayList<>(Collections.nCopies(daysInRange, 0));

        // Duyệt qua tất cả các payment trong paymentHistoryRepo
        paymentHistoryRepo.findAll().stream()
                .filter(payment -> {
                    LocalDate paymentDate = payment.getPaymentDate().toLocalDate();
                    return !paymentDate.isBefore(startDate) && !paymentDate.isAfter(endDate);
                })
                .forEach(payment -> {
                    int dayIndex = (int) ChronoUnit.DAYS.between(startDate, payment.getPaymentDate().toLocalDate());
                    int totalPrice = payment.getAmountPaid();

                    if (payment.getInvoice().getShippingAddress() != null) { // Online
                        onlineSales.set(dayIndex, onlineSales.get(dayIndex) + totalPrice);
                    } else { // Offline
                        offlineSales.set(dayIndex, offlineSales.get(dayIndex) + totalPrice);
                    }
                });

        // Tạo map trả về
        Map<String, List<Integer>> result = new HashMap<>();
        result.put("onlineSales", onlineSales);
        result.put("offlineSales", offlineSales);

        return result;
    }






    public int[] totalOnline(){
        List<PaymentHistory> paymentHistories = paymentHistoryRepo.findAll();
        int total = 0;
        int dem = 0;
        for (PaymentHistory paymentHistory : paymentHistories) {
            if(paymentHistory.getInvoice().getShippingAddress() != null){
                total += paymentHistory.getAmountPaid();
                ++dem;
            }
        }
        return new int[]{dem, total};
    }

    public double[] tileOnline(){

        LocalDateTime dateTime = LocalDateTime.now(); // Lấy thời gian hiện tại

        int monthValue = dateTime.getMonthValue();
        int yearOle = monthValue != 1 ? dateTime.getYear() : dateTime.getYear() - 1;
        int monthLienKe = monthValue != 1 ? monthValue - 1 : 12;


        List<PaymentHistory> paymentHistories = paymentHistoryRepo.findAll();

        int totalThangLienKeOnline = 0, totalThangHienTaiOnline = 0, totalThangHienTaiOffline = 0, totalThangLienKeOffline = 0;

        for(PaymentHistory paymentHistory : paymentHistories){
            if(paymentHistory.getInvoice().getShippingAddress() != null){
                if(yearOle == paymentHistory.getPaymentDate().getYear() && monthLienKe == paymentHistory.getPaymentDate().getMonthValue()){
                    totalThangLienKeOnline += paymentHistory.getAmountPaid();
                }
                if(dateTime.getYear() == paymentHistory.getPaymentDate().getYear() && monthValue == paymentHistory.getPaymentDate().getMonthValue()){
                    totalThangHienTaiOnline += paymentHistory.getAmountPaid();
                }

            }else{
                if(yearOle == paymentHistory.getPaymentDate().getYear() && monthLienKe == paymentHistory.getPaymentDate().getMonthValue()){
                    totalThangLienKeOffline += paymentHistory.getAmountPaid();
                }
                if(dateTime.getYear() == paymentHistory.getPaymentDate().getYear() && monthValue == paymentHistory.getPaymentDate().getMonthValue()){
                    totalThangHienTaiOffline += paymentHistory.getAmountPaid();
                }

            }

        }

        if(totalThangHienTaiOffline == 0 && totalThangHienTaiOnline == 0){
            return new double[]{0, 0, 0};
        } else if( totalThangLienKeOnline == 0 && totalThangLienKeOffline == 0){
            return new double[]{100, 100, 100};

        }


        return new double[] {100.0 - (double) totalThangHienTaiOnline / totalThangLienKeOnline, 100.0 - (double) totalThangHienTaiOffline / totalThangLienKeOffline, 100 - (double) (totalThangHienTaiOffline + totalThangHienTaiOnline )/ (totalThangLienKeOnline + totalThangLienKeOffline)};
    }

    public double tileOnlineTrenOffline(){
        List<PaymentHistory> paymentHistories = paymentHistoryRepo.findAll();
        int total = 0;
        for(PaymentHistory paymentHistory : paymentHistories){
            if(paymentHistory.getInvoice().getShippingAddress() != null){
                ++total;
            }
        }
        return !paymentHistories.isEmpty() ?  total*100.0/paymentHistories.size() : 0;
    }





}
