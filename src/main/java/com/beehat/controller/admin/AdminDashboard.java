package com.beehat.controller.admin;

import com.beehat.DTO.ProductResponse;
import com.beehat.entity.Product;
import com.beehat.entity.ProductDetail;
import com.beehat.repository.InvoiceDetailRepo;
import com.beehat.repository.ProductDetailRepo;
import com.beehat.service.DashBoard;
import com.beehat.service.InvoiceDetailService;
import com.beehat.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/doanhthu")
public class AdminDashboard {

    @Autowired
    private DashBoard dashBoard;

    @Autowired
    private InvoiceDetailRepo invoiceDetailRepository;
    @Autowired
    private InvoiceDetailService invoiceDetailService;
    @Autowired
    private ProductDetailRepo productDetailRepo;


    @GetMapping
    public ResponseEntity<Map<String, Object>> getMonthlySales(
            @RequestParam String date,
            @RequestParam String type) {

        LocalDate selectedDate = LocalDate.parse(date);  // Chuyển đổi String thành LocalDate
        int month = selectedDate.getMonthValue();        // Lấy tháng từ ngày
        int year = selectedDate.getYear();
        // Lấy doanh thu cho từng tháng trong năm
        List<Integer> monthlySales = dashBoard.getDoanhThuTheoNam(year, type);

        // Tạo danh sách tháng trong năm
        List<Integer> labels = IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList());

        // Doanh thu theo loại (offline/online)
        int totalSales = monthlySales.stream().mapToInt(Integer::intValue).sum();
        List<Integer> salesByType = List.of(
                totalSales / 2, // Ví dụ: Chia đều doanh thu cho online và offline
                totalSales / 2
        );

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels); // Các tháng trong năm
        result.put("sales", monthlySales); // Doanh thu theo từng tháng
        result.put("salesByType", salesByType); // Doanh thu theo loại bán hàng

        return ResponseEntity.ok(result);
    }


    @GetMapping("/banchay")
    public ResponseEntity<List<ProductResponse>> getTopSellingProducts(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        // Sử dụng hàm countSL để tính số lượng sản phẩm bán chạy
        List<ProductResponse> productCountMap = invoiceDetailService.countSL();


        return ResponseEntity.ok(productCountMap);
    }

    @GetMapping("/tileOnline")
    public ResponseEntity<Map<String, Double>> getTopSellingProducts() {
        Map<String, Double> salesData = new HashMap<>();
        salesData.put("offline", 100.0 -dashBoard.tileOnlineTrenOffline()); // Example data: replace with dynamic data
        salesData.put("online", dashBoard.tileOnlineTrenOffline()); // Example data: replace with dynamic data
        return ResponseEntity.ok(salesData);
    }


}
