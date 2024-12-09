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

    public List<Integer> getDoanhThuTheoThang(int month, int year, String type) {
        LocalDate startOfMonth = YearMonth.of(year, month).atDay(1);
        LocalDate endOfMonth = YearMonth.of(year, month).atEndOfMonth();

        int daysInMonth = endOfMonth.getDayOfMonth();
        List<Integer> doanhThuTheoNgay = new ArrayList<>(Collections.nCopies(daysInMonth, 0));
        boolean isOnline = type.equals("online");

        if(isOnline) {
            paymentHistoryRepo.findAll().stream()
                    .filter(payment ->  payment.getInvoice() != null &&
                            !payment.getPaymentDate().toLocalDate().isBefore(startOfMonth) &&
                            !payment.getPaymentDate().toLocalDate().isAfter(endOfMonth))
                    .forEach(payment -> {
                        int day = payment.getPaymentDate().getDayOfMonth();
                        int totalPrice = payment.getAmountPaid();
                        doanhThuTheoNgay.set(day - 1, doanhThuTheoNgay.get(day - 1) + totalPrice);
                    });
        } else{
            paymentHistoryRepo.findAll().stream()
                    .filter(payment -> payment.getInvoice() == null &&
                            !payment.getPaymentDate().toLocalDate().isBefore(startOfMonth) &&
                            !payment.getPaymentDate().toLocalDate().isAfter(endOfMonth))
                    .forEach(payment -> {
                        int day = payment.getPaymentDate().getDayOfMonth();
                        int totalPrice = payment.getAmountPaid();
                        doanhThuTheoNgay.set(day - 1, doanhThuTheoNgay.get(day - 1) + totalPrice);
                    });
        }

        return doanhThuTheoNgay;
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
        return !paymentHistories.isEmpty() ? (double) total/paymentHistories.size() : 0;
    }





}
