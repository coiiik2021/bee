package com.beehat.controller.admin;

import com.beehat.entity.Product;
import com.beehat.entity.ProductDetail;
import com.beehat.entity.ProductPromotion;
import com.beehat.entity.Promotion;
import com.beehat.repository.ProductDetailRepo;
import com.beehat.repository.ProductPromotionRepo;
import com.beehat.repository.ProductRepo;
import com.beehat.repository.PromotionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/promotion")
public class PromotionController {
    @Autowired
    private PromotionRepo promotionRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductPromotionRepo productPromotionRepo;
    @Autowired
    private ProductDetailRepo productDetailRepo;
    @ModelAttribute("iconTitle")
    String iconTitle() {
        return "ph ph-seal-percent fs-3";
    }
    @ModelAttribute("pageTitle")
    String pageTitle() {
        return "Chương trình giảm giá";
    }
    @GetMapping
    public String promotion(Model model) {
        List<Promotion> listPromotion = promotionRepo.findAll();
        model.addAttribute("listPromotion",listPromotion);
        return "admin/promotion/promotion";
    }
    @GetMapping("/search")
    public String searchPromotions(@RequestParam(value = "search", required = false) String search,
                                   @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime fromDate,
                                   @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDateTime toDate,
                                   @RequestParam(value = "status", required = false) String status,
                                   @RequestParam(value = "provalue", required = false) String provalue, // Cập nhật provalue là optional
                                   Model model) {

        List<Promotion> promotions ;
        Byte statusValue = null;

        // Chuyển đổi status nếu không null hoặc rỗng
        if (status != null && !status.isEmpty()) {
            try {
                statusValue = Byte.parseByte(status);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        // Kiểm tra giá trị từ ngày và đến ngày
        if (fromDate == null) {
            fromDate = LocalDate.of(2000, 1, 1).atStartOfDay(); // Hoặc giá trị mặc định khác
        }
        if (toDate == null) {
            toDate = LocalDate.of(2222, 1, 1).atStartOfDay();
        }

        // Xử lý loại giảm giá
        if ("amount".equals(provalue)) {
            promotions = promotionRepo.findPromotionsByDiscountAmountGreaterThan(search, fromDate, toDate, statusValue);
        } else if ("percent".equals(provalue)) {
            promotions = promotionRepo.findPromotionsByDiscountPercentageGreaterThan(search, fromDate, toDate, statusValue);
        } else {
            // Nếu loại null, kiểm tra tên và tìm kiếm
            if (search == null || search.isEmpty()) {
                promotions = promotionRepo.findAllPromotions(null, fromDate, toDate, statusValue); // Truyền null cho tên
            } else {
                promotions = promotionRepo.findAllPromotions(search, fromDate, toDate, statusValue); // Truyền tên
            }
        }

        // Gán danh sách khuyến mãi vào model
        model.addAttribute("listPromotion", promotions);
        return "admin/promotion/promotion";
    }
    @GetMapping("/add")
    public String add(Model model,@RequestParam(required = false) List<Integer> selectedProducts,@RequestParam(defaultValue = "0") int page){
        Pageable pageable = PageRequest.of(page, 5); // 5 sản phẩm mỗi trang
        Page<Product> productsPage = productRepo.findByPromotionIsNull(pageable);
        // Khởi tạo tập hợp để lưu trạng thái checkbox
        Set<Integer> selectedSet = new HashSet<>();
        if (selectedProducts != null) {
            selectedSet.addAll(selectedProducts);
        }
        model.addAttribute("selectedSet", selectedSet);
        model.addAttribute("listProduct",productsPage);
        return "/admin/promotion/add-promotion";
    }
    @PostMapping("/add")
    public String addPromotion(Model model,
                               @RequestParam(required = false) List<Integer> selectedProducts,
                               @RequestParam String name,
                               @RequestParam(required = false) Integer value,
                               @RequestParam(required = false) Byte percentage,
                               @RequestParam LocalDateTime startDate, // Ngày bắt đầu
                               @RequestParam LocalDateTime endDate, // Ngày kết thúc
                               @RequestParam(required = false) Integer promotionId) {
        if (value == null) {
            value = 0; // hoặc giá trị mặc định khác nếu muốn
        }

        if (percentage == null) {
            percentage = 0; // hoặc giá trị mặc định khác nếu muốn
        }


        // Khởi tạo đối tượng khuyến mãi
        Promotion promotion = new Promotion();
        promotion.setName(name);
        // Kiểm tra loại khuyến mãi nào được chọn
        if (percentage > 0) {
            promotion.setDiscountAmount(value); // Giá trị được chọn
            promotion.setDiscountPercentage( percentage); // Giá trị phần trăm
        } else if (value > 0) {
            promotion.setDiscountAmount(value); // Giá trị
            promotion.setDiscountPercentage(percentage); // Giá trị phần trăm
        }

        // Thiết lập ngày bắt đầu và kết thúc
        promotion.setStartDate(startDate);
        promotion.setEndDate(endDate);
        // Kiểm tra trạng thái khuyến mãi
        LocalDateTime now = LocalDateTime.now();
        if (startDate.isAfter(now)) {
            promotion.setStatus((byte) 0); // Chưa bắt đầu
        } else if (endDate.isBefore(now)) {
            promotion.setStatus((byte) 2); // Đã kết thúc
        } else {
            promotion.setStatus((byte) 1); // Đang diễn ra
        }
        // Lưu khuyến mãi mới vào cơ sở dữ liệu
        promotionRepo.save(promotion);
        if (selectedProducts != null) {
            // Giả sử bạn có một bảng để theo dõi mối quan hệ giữa sản phẩm và khuyến mãi
            for (Integer productId : selectedProducts) {
                Product product = productRepo.findById(productId).orElse(null);
                // Thực hiện ghi lại thông tin để gán khuyến mãi sau
                ProductPromotion promotionProduct = new ProductPromotion();
                promotionProduct.setPromotion(promotion);
                promotionProduct.setProduct(product);
                promotionProduct.setStartDate(startDate);
                promotionProduct.setEndDate(endDate);
                promotionProduct.setStatus((byte) 1);
                productPromotionRepo.save(promotionProduct); // Lưu vào bảng mối quan hệ
            }
        }
        return "redirect:/admin/promotion"; // Chuyển hướng về trang danh sách sản phẩm
    }
    @GetMapping("/detail/{id}")
    public String detail(Model model,@PathVariable Integer id){
        Promotion promotion = promotionRepo.findeById(id);
        List<ProductDetail> listProductDetail = productDetailRepo.findByPromotionId(id);
        model.addAttribute("listProductDetail",listProductDetail);
        model.addAttribute("promotion",promotion);
        return "/admin/promotion/detail-promotion";
    }
    @GetMapping("/update")
    public String update(@RequestParam Integer id,
                         Model model,
                         @RequestParam(required = false) List<Integer> selectedProducts,
                         @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 5); // 5 sản phẩm mỗi trang
        Page<Product> productsPage = productRepo.findProductUpdate(id,pageable);

        // Tìm tất cả ProductPromotion liên quan đến khuyến mãi đó
        List<ProductPromotion> productPromotions = productPromotionRepo.findByPromotionId(id);

        // Khởi tạo tập hợp để lưu trạng thái checkbox
        Set<Integer> selectedSet = new HashSet<>();

        // Thêm các sản phẩm đã chọn vào selectedSet
        if (selectedProducts != null) {
            selectedSet.addAll(selectedProducts);
        }
        // Lấy ID sản phẩm từ ProductPromotion liên quan và thêm vào selectedSet
        productPromotions.forEach(pp -> selectedSet.add(pp.getProduct().getId()));

        // Thêm vào model để sử dụng trong view
        model.addAttribute("selectedSet", selectedSet);
        model.addAttribute("id", id);
        Promotion promotion = promotionRepo.findById(id).orElse(null);
        model.addAttribute("promotion", promotion);
        model.addAttribute("listProduct", productsPage);

        return "/admin/promotion/update-promotion";
    }
    @Transactional
    @PostMapping("/update")
    public String updatePromotion(Model model,
                                  @RequestParam(required = false) List<Integer> selectedProducts,
                                  @RequestParam String name,
                                  @RequestParam(required = false) Integer value,
                                  @RequestParam(required = false) Byte percentage,
                                  @RequestParam LocalDateTime startDate, // Ngày bắt đầu
                                  @RequestParam LocalDateTime endDate, // Ngày kết thúc
                                  @RequestParam(required = false) Integer id) {

        // Kiểm tra và thiết lập giá trị mặc định
        if (value == null) {
            value = 0; // Giá trị mặc định
        }

        if (percentage == null) {
            percentage = 0; // Giá trị mặc định
        }

        // Tạo hoặc cập nhật đối tượng khuyến mãi
        Promotion promotion = promotionRepo.findById(id).orElse(new Promotion());
        promotion.setName(name);

        // Kiểm tra và thiết lập giá trị khuyến mãi
        if (percentage > 0) {
            promotion.setDiscountAmount(0);
            promotion.setDiscountPercentage(percentage); // Giá trị phần trăm
        } else if (value > 0) {
            promotion.setDiscountAmount(value); // Giá trị
            promotion.setDiscountPercentage((byte) 0);
        }

        // Thiết lập ngày bắt đầu và kết thúc
        promotion.setStartDate(startDate);
        promotion.setEndDate(endDate);

        // Xác định trạng thái khuyến mãi
        LocalDateTime now = LocalDateTime.now();
        if (startDate.isAfter(now)) {
            promotion.setStatus((byte) 0); // Chưa bắt đầu
        } else if (endDate.isBefore(now)) {
            promotion.setStatus((byte) 2); // Đã kết thúc
        } else {
            promotion.setStatus((byte) 1); // Đang diễn ra
        }

        // Lưu khuyến mãi vào cơ sở dữ liệu
        promotionRepo.save(promotion);

        // Lấy danh sách sản phẩm hiện có liên kết với promotionId
        List<ProductPromotion> existingProductPromotions = productPromotionRepo.findByPromotionId(id);
        List<Product> existingProducts = existingProductPromotions.stream()
                .map(ProductPromotion::getProduct)
                .collect(Collectors.toList());

        // Xóa promotionId cho các sản phẩm hiện tại
        for (Product product : existingProducts) {
            product.setPromotion(null);
            productRepo.save(product); // Lưu lại cập nhật cho sản phẩm
        }

        // Lấy danh sách ProductPromotion hiện tại
        Set<Integer> existingProductIds = existingProductPromotions.stream()
                .map(pp -> pp.getProduct().getId())
                .collect(Collectors.toSet());

        // Cập nhật trạng thái cho các ProductPromotion không còn được chọn
        for (ProductPromotion pp : existingProductPromotions) {
            if (!selectedProducts.contains(pp.getProduct().getId())) {
                // Chỉ cập nhật trạng thái
                pp.setStatus((byte) 0); // Đặt trạng thái thành 0
                productPromotionRepo.save(pp); // Lưu thay đổi vào cơ sở dữ liệu
            }
        }

        // Thêm hoặc cập nhật ProductPromotion cho các sản phẩm được chọn
        for (Integer productId : selectedProducts) {
            Product product = productRepo.findById(productId).orElse(null);
            if (product != null) {
                // Kiểm tra nếu ProductPromotion đã tồn tại
                Optional<ProductPromotion> existingPP = existingProductPromotions.stream()
                        .filter(pp -> pp.getProduct().getId().equals(productId))
                        .findFirst();

                if (existingPP.isPresent()) {
                    // Cập nhật các thuộc tính của ProductPromotion
                    ProductPromotion promotionProduct = existingPP.get();
                    promotionProduct.setStartDate(startDate);
                    promotionProduct.setEndDate(endDate);
                    promotionProduct.setStatus((byte) 1); // Đang hoạt động
                    productPromotionRepo.save(promotionProduct); // Cập nhật vào cơ sở dữ liệu
                } else {
                    // Thêm mới ProductPromotion
                    ProductPromotion promotionProduct = new ProductPromotion();
                    promotionProduct.setPromotion(promotion);
                    promotionProduct.setProduct(product);
                    promotionProduct.setStartDate(startDate);
                    promotionProduct.setEndDate(endDate);
                    promotionProduct.setStatus((byte) 1); // Đang hoạt động
                    productPromotionRepo.save(promotionProduct); // Lưu vào bảng mối quan hệ
                }
            }
        }

        return "redirect:/admin/promotion"; // Chuyển hướng về trang danh sách khuyến mãi
    }
}
