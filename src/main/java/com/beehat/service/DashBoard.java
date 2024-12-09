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

    public List<Integer> getDoanhThuTheoNam(int year, String type) {
        List<Integer> monthlySales = new ArrayList<>(Collections.nCopies(12, 0)); // Khởi tạo danh sách 12 tháng

        // Lặp qua các tháng trong năm
        for (int month = 1; month <= 12; month++) {
            LocalDate startOfMonth = YearMonth.of(year, month).atDay(1);
            LocalDate endOfMonth = YearMonth.of(year, month).atEndOfMonth();

            int totalSales = 0;

            // Lọc dữ liệu thanh toán theo loại (online/offline) và ngày trong tháng
            boolean isOnline = type.equals("online");
            List<PaymentHistory> payments = paymentHistoryRepo.findAll().stream()
                    .filter(payment -> !payment.getPaymentDate().toLocalDate().isBefore(startOfMonth) &&
                            !payment.getPaymentDate().toLocalDate().isAfter(endOfMonth))
                    .collect(Collectors.toList());

            for (PaymentHistory payment : payments) {
                boolean isValidType = (isOnline && payment.getInvoice() != null) || (!isOnline && payment.getInvoice() == null);
                if (isValidType) {
                    totalSales += payment.getAmountPaid();
                }
            }

            // Gán doanh thu cho tháng tương ứng
            monthlySales.set(month - 1, totalSales); // Gán doanh thu cho tháng (0-11)
        }

        return monthlySales;
    }







    public int[] totalOnline(){
        List<PaymentHistory> paymentHistories = paymentHistoryRepo.findAll();
        int total = 0;
        for (PaymentHistory paymentHistory : paymentHistories) {
            if(paymentHistory.getInvoice() != null){
                total += paymentHistory.getAmountPaid();
            }
        }
        return new int[]{paymentHistories.size(), total};
    }

    public double[] tileOnline(){

        LocalDateTime dateTime = LocalDateTime.now(); // Lấy thời gian hiện tại

        int monthValue = dateTime.getMonthValue();
        int yearOle = monthValue != 1 ? dateTime.getYear() : dateTime.getYear() - 1;
        int monthLienKe = monthValue != 1 ? monthValue - 1 : 12;


        List<PaymentHistory> paymentHistories = paymentHistoryRepo.findAll();

        int totalThangLienKeOnline = 0, totalThangHienTaiOnline = 0, totalThangHienTaiOffline = 0, totalThangLienKeOffline = 0;

        for(PaymentHistory paymentHistory : paymentHistories){
            if(paymentHistory.getInvoice() != null){
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
            if(paymentHistory.getInvoice() != null){
                ++total;
            }
        }
        return !paymentHistories.isEmpty() ? (double) total/paymentHistories.size() : 0;
    }





}
