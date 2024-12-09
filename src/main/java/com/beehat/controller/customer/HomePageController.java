package com.beehat.controller.customer;

import com.beehat.entity.Color;
import com.beehat.entity.Product;
import com.beehat.entity.ProductDetail;
import com.beehat.entity.Size;
import com.beehat.repository.ProductDetailRepo;
import com.beehat.repository.ProductImageRepo;
import com.beehat.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.LinkedHashSet;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/home")
public class HomePageController {
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductImageRepo productImageRepo;
    @Autowired
    private ProductDetailRepo productDetailRepo;
    @GetMapping("/index")
    public String home(Authentication authentication, Model model) {
        model.addAttribute("products", productRepo.findAll());

        if (authentication != null && authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            Boolean isLoggedIn = username!=null?true:false;
            model.addAttribute("isLoggedIn", isLoggedIn);
            System.out.println(username);
            model.addAttribute("username1", username);
        }else {
            model.addAttribute("isLoggedIn", false);  // Đảm bảo trường hợp không đăng nhập
        }
        return "online_store/trangchu";
    }
   @GetMapping("/cart")
    public String cart(Model model) {
       return "online_store/giohang";
   }
   @GetMapping("/product-detail/{id}")
    public String productDetail(@PathVariable int id,Model model) {
       Product products = productRepo.findById(id).orElse(null);
       List<ProductDetail> productDetails = productDetailRepo.findByProductId(id);
       // Sử dụng Set để loại bỏ các màu sắc trùng lặp
       List<Color> uniqueColors = productDetails.stream()
               .map(ProductDetail::getColor)
               .collect(Collectors.toCollection(LinkedHashSet::new)) // Lọc trùng và giữ thứ tự
               .stream().collect(Collectors.toList()); // Chuyển về
       List<Size> uniqueSizes = productDetails.stream()
               .map(ProductDetail::getSize)
               .collect(Collectors.toCollection(LinkedHashSet::new)) // Lọc trùng và giữ thứ tự
               .stream().collect(Collectors.toList()); // Chuyển về List

        model.addAttribute("product",products );
        model.addAttribute("minPrice", productDetailRepo.findTopByProductIdOrderByPriceAsc(id) == null ? null : productDetailRepo.findTopByProductIdOrderByPriceAsc(id).getPrice());
        model.addAttribute("maxPrice", productDetailRepo.findTopByProductIdOrderByPriceDesc(id) == null ? null : productDetailRepo.findTopByProductIdOrderByPriceAsc(id).getPrice());
        model.addAttribute("uniqueColors", uniqueColors);
        model.addAttribute("uniqueSizes", uniqueSizes);

       return "online_store/productdetail";
   }
   @GetMapping("/lien-he")
    public String lienHe(Model model) {
       return "online_store/lienhe";
   }
   @GetMapping("/tin-tuc")
    public String tinTuc(Model model) {
       return "online_store/tintuc";
   }
    @GetMapping("/chitietdonhang")
    public String chiTiet(Model model) {
        return "orderdetail";
    }
}
