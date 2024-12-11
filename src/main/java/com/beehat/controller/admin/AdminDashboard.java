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
    public ResponseEntity<Map<String, Object>> getSales(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        // Lấy dữ liệu tổng hợp từ service
        Map<String, List<Integer>> salesData = dashBoard.getDoanhThuTongHop(start, end);

        // Tạo danh sách ngày (labels)
        List<String> labels = start.datesUntil(end.plusDays(1))
                .map(LocalDate::toString)
                .collect(Collectors.toList());

        // Kết quả trả về
        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("onlineSales", salesData.get("onlineSales"));
        result.put("offlineSales", salesData.get("offlineSales"));

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
