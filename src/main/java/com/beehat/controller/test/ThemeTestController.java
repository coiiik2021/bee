package com.beehat.controller.test;

import com.beehat.DTO.ProductDTO;
import com.beehat.entity.*;
import com.beehat.repository.*;
import com.beehat.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class ThemeTestController {
    @Autowired
    AddressService addressService;
    @Autowired
    ProductRepo productRepo;
    @Autowired
    ProductDetailRepo productDetailRepo;
    @Autowired
    CartService cartService;
    @Autowired
    private CartDetailRepo cartDetailRepo;
    @Autowired
    private CustomerRepo customerRepo;
    @Autowired
    InvoiceRepo invoiceRepo;
    @Autowired
    InvoiceDetailRepo invoiceDetailRepo;
    @Autowired
    InvoiceService invoiceService;
    @Autowired
    private ProvincesService provincesService;
    @Autowired
    PaymentHistoryRepo paymentHistoryRepo;
    @Autowired
    PaymentMethodRepo paymentMethodRepo;
    @Autowired
    InvoiceHistoryStatusRepo invoiceHistoryStatusRepo;
    @Autowired
    private VoucherRepo voucherRepo;
    PasswordEncoder passwordEncoder;
    @ModelAttribute("cartSum")
    public long getSum(Principal principal) {
        if (principal == null) {
            return cartService.getTotal();
        }

        Customer customer = customerRepo.findByUsername(principal.getName());

        //trường hợp dùng tài khoản admin employ đăng nhập vào thì return -1
        if (customer == null) {
            return -1;
        }

        // trường hợp giỏ hàng của user không có sản phẩm
        List<CartDetail> cartDetails = cartDetailRepo.findByCustomerIdAndStatus(customer.getId(),(byte) 1);
        if (cartDetails == null || cartDetails.isEmpty()) {
            // Handle case where no cart details are found
            return 0;
        }

        // trường hợp có sản phẩm trong giỏ tài khoản user
        long sum = cartDetails.stream().map(CartDetail::getProductDetail).distinct().count();
        return sum;
    }

    @GetMapping("/")
    public String home(HttpServletRequest request, HttpServletResponse response,Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Kiểm tra nếu người dùng đã đăng nhập và có vai trò là ADMIN hoặc EMPLOYEE
        if (auth != null && (auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))
                || auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_EMPLOYEE")))) {

            // Đăng xuất nếu có quyền ADMIN hoặc EMPLOYEE
            new SecurityContextLogoutHandler().logout(request, response, auth);

            // Sau khi đăng xuất, chuyển hướng về trang login hoặc trang khác nếu cần
            return "redirect:/";  // Điều chỉnh URL chuyển hướng nếu cần
        }
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String username = auth.getName();
            System.out.println(username);// Lấy tên người dùng đăng nhập (username)
            Customer customer = customerRepo.findByUsername(username); // Tìm thông tin Customer theo username
            if (customer != null) {
                customer.setCountry(addressService.getWardNameByCode(customer.getWard())+' '+addressService.getDistrictNameByCode(customer.getDistrict())+' '+addressService.getProvinceNameByCode(customer.getCity()));
                customerRepo.save(customer);
            }
        }
        List<Product> productList = productRepo.findTop12ByOrderByCreatedDateDesc();
        List<ProductDTO> productDTOs = productList.stream()
                .map(ProductDTO::new)  // Tạo DTO cho từng sản phẩm
                .collect(Collectors.toList());  // Collect các DTO vào danh sách
        model.addAttribute("productList",productDTOs);
        return "test-theme/index";
    }

    @GetMapping("/detail/{sku}")
    public String detail(@PathVariable String sku, Model model) {
        Product product = productRepo.findBySku(sku);
        ProductDTO productDTO = new ProductDTO(product);
        model.addAttribute("product", productDTO);
        return "test-theme/product-details";
    }

    @GetMapping("/shop")
    public String shop(Model model) {
        // Lấy tất cả sản phẩm từ cơ sở dữ liệu
        List<Product> products = productRepo.findAll();

        // Chuyển đổi sản phẩm thành DTO và nhóm theo từng sản phẩm
        List<ProductDTO> productDTOs = products.stream()
                .map(ProductDTO::new)  // Tạo DTO cho từng sản phẩm
                .collect(Collectors.toList());  // Collect các DTO vào danh sách
        // Truyền danh sách DTO vào model
        model.addAttribute("products", productDTOs);
        return "test-theme/shop";
    }

    @GetMapping("/shop-cart")
    public String shopCart(Principal principal, Model model) {
        //kiểm tra xem có đăng nhập hay không
        //nếu có thì lấy giỏ hàng từ db và update giỏ hàng tạm thời để có dữ liệu checkout cho hoá đơn tạm thời
        if (principal != null) {
            Customer customer = customerRepo.findByUsername(principal.getName());
            List<CartDetail> cartDetail = cartDetailRepo.findByCustomerIdAndStatus(customer.getId(), (byte) 1);
            model.addAttribute("cartDetail", cartDetail);
            model.addAttribute("totalPrice", cartDetail.stream().mapToInt(cart -> cart.getQuantity() * cart.getProductDetail().getPrice()).sum());
            return "test-theme/shop-cart";
        } else {
            List<CartDetail> cartDetails = cartService.getCartDetails();
            for (CartDetail cartDetail : cartDetails) {
                ProductDetail latestProductDetail = productDetailRepo.findById(cartDetail.getProductDetail().getId()).orElse(null);
                if (latestProductDetail != null) {
                    cartDetail.setProductDetail(latestProductDetail);
                }
            }
            model.addAttribute("cartDetail", cartService.getCartDetails());
            model.addAttribute("totalPrice", cartService.getCartDetails().stream().mapToInt(cart -> cart.getQuantity() * cart.getProductDetail().getPrice()).sum());
            return "test-theme/shop-cart";
        }

    }

    @GetMapping("/blog")
    public String blog() {
        return "test-theme/blog";
    }

    @GetMapping("/blog-details")
    public String blogDetails() {
        return "test-theme/blog-details";
    }

    @PostMapping("/proceed-to-checkout")
    public String proceedToCheckout(Principal principal) {
        if (principal != null) {
            // Khách hàng đã đăng nhập
            Customer customer = customerRepo.findByUsername(principal.getName());
            List<CartDetail> cartDetail = cartDetailRepo.findByCustomerIdAndStatus(customer.getId(), (byte) 1);
            cartService.createTemporaryInvoice(customer, cartDetail);
        } else {
            // Khách hàng chưa đăng nhập
            cartService.createTemporaryInvoice(null, cartService.getCartDetails());
        }
        return "redirect:/checkout";
    }
    @GetMapping("/checkout")
    public String checkout(Model model,Principal principal) {
        model.addAttribute("voucherList",voucherRepo.findByStatus((byte) 1));
        model.addAttribute("provinces", provincesService.getProvinces());
        Invoice temporaryInvoice = cartService.getTemporaryInvoice();
        if (temporaryInvoice == null) {
            return "redirect:/shop-cart"; // Nếu không có hóa đơn tạm thời, quay lại giỏ hàng
        }
        model.addAttribute("invoice", temporaryInvoice);
        if (principal != null) {
            Customer customer = customerRepo.findByUsername(principal.getName());
            model.addAttribute("cartDetails", cartDetailRepo.findByCustomerIdAndStatus(customer.getId(), (byte) 1));
        }else{
            model.addAttribute("cartDetails", cartService.getCartDetails());
        }
        return "test-theme/checkout";
    }

    @GetMapping("/contact")
    public String contact() {
        return "test-theme/contact";
    }

    //tạm thời thêm nhanh ở trang shop
    @PostMapping("/add-product-to-cart/{id}")
    public String addProductToCart(@PathVariable Integer id,
                                   @RequestParam(value = "username", defaultValue = "") String username) {
        ProductDetail productDetail = productDetailRepo.findById(id).orElse(null);
        Customer customer = customerRepo.findByUsername(username);
        if (customer != null) {
            List<CartDetail> cartDetail = cartDetailRepo.findByCustomerIdAndStatus(customer.getId(), (byte) 1);
            boolean isNone = true;
            for (CartDetail cart : cartDetail) {
                if (productDetail.getId() == cart.getProductDetail().getId()) {
                    isNone = false;
                    cart.setQuantity(cart.getQuantity() + 1);
                    cartDetailRepo.save(cart);
                }
            }
            if (isNone) {
                CartDetail cartDetail1 = new CartDetail();
                cartDetail1.setProductDetail(productDetail);
                cartDetail1.setCustomer(customer);
                cartDetail1.setQuantity(1);
                cartDetailRepo.save(cartDetail1);
            }
            return "redirect:/shop";
        } else {
            cartService.add(id,1);
            return "redirect:/shop";
        }
    }

    @GetMapping("/getProductPrice")
    @ResponseBody
    public Map<String, Object> getProductPrice(@RequestParam Integer product, @RequestParam Integer color, @RequestParam Integer size,Principal principal) {
        Map<String, Object> response = new HashMap<>();
        List<CartDetail> cartDetails;
        if (principal != null) {
            Customer customer = customerRepo.findByUsername(principal.getName());
            cartDetails = cartDetailRepo.findByCustomerIdAndStatus(customer.getId(), (byte) 1);
        }else {
            cartDetails = cartService.getCartDetails();
            for (CartDetail cartDetail : cartDetails) {
                ProductDetail latestProductDetail = productDetailRepo.findById(cartDetail.getProductDetail().getId()).orElse(null);
                if (latestProductDetail != null) {
                    cartDetail.setProductDetail(latestProductDetail);
                }
            }
        }
        try {
            ProductDetail productDetail = productDetailRepo.findByProductIdAndColorIdAndSizeId(product, color, size);
            if (productDetail != null) {
                response.put("id", productDetail.getId());
                response.put("price", CurrencyUtil.formatCurrency(productDetail.getPrice()));
                response.put("stock", productDetail.getStock()); // Thêm số lượng tồn kho
                response.put("stockInCart", cartDetails.stream()
                        .filter(cartDetail -> cartDetail.getProductDetail().getId().equals(productDetail.getId()))
                        .mapToInt(CartDetail::getQuantity)
                        .findFirst()
                        .orElse(0));
            } else {
                throw new RuntimeException("ProductDetail not found for color: " + color + " and size: " + size);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("id", -1);   // Giá trị id lỗi
            response.put("price", "-1"); // Giá trị price lỗi
            response.put("stock", "-1"); // Giá trị stock lỗi
        }
        return response;
    }

    @PostMapping("/add-product-detail-to-cart")
    public String addProductDetailToCart(@RequestParam(value = "username", defaultValue = "") String username,
                                         @RequestParam("quantity") Integer quantity,
                                         @RequestParam("productId") Integer id) {
        ProductDetail productDetail = productDetailRepo.findById(id).orElse(null);
        Customer customer = customerRepo.findByUsername(username);
        if (customer != null) {
            List<CartDetail> cartDetail = cartDetailRepo.findByCustomerIdAndStatus(customer.getId(), (byte) 1);
            boolean isNone = true;
            for (CartDetail cart : cartDetail) {
                if (productDetail.getId() == cart.getProductDetail().getId()) {
                    isNone = false;
                    cart.setQuantity(cart.getQuantity() + quantity);
                    cartDetailRepo.save(cart);
                }
            }
            if (isNone) {
                CartDetail cartDetail1 = new CartDetail();
                cartDetail1.setProductDetail(productDetail);
                cartDetail1.setQuantity(quantity);
                cartDetail1.setCustomer(customer);
                cartDetailRepo.save(cartDetail1);
            }
        } else {
            cartService.add(id,quantity);
            return "redirect:/shop";
        }
        return "redirect:/shop";
    }

    @PostMapping("/check-out")
    public String checkOut(@RequestParam("countryInv") String countryInv,
                           @RequestParam("addressInv") String addressInv,
                           @RequestParam("cityInv") String cityInv,
                           @RequestParam("districtInv") String districtInv,
                           @RequestParam("wardInv") String wardInv,
                           @RequestParam("phoneInv") String phoneInv,
                           @RequestParam("paymentInv") Integer idPayment
                           //thêm voucher
                           //@RequestParam("code") String code
    ) {
        // Lấy phương thức thanh toán
        PaymentMethod paymentMethod = paymentMethodRepo.findById(idPayment).orElse(null);
        if (paymentMethod == null) {
            return "redirect:/cart";
        }

        // Lấy hóa đơn tạm thời
        Invoice temporaryInvoice = cartService.getTemporaryInvoice();
        if (temporaryInvoice == null) {
            return "redirect:/cart";
        }
        //thêm voucher
       // Voucher voucher = voucherRepo.findByCode(code);
        // Cập nhật thông tin vận chuyển và thanh toán cho hóa đơn tạm
        temporaryInvoice.setShippingCountry(countryInv);
        temporaryInvoice.setShippingAddress(addressInv);
        temporaryInvoice.setShippingCity(cityInv);
        temporaryInvoice.setShippingDistrict(districtInv);
        temporaryInvoice.setShippingWard(wardInv);
        temporaryInvoice.setPhone(phoneInv);
        temporaryInvoice.setPaymentMethod(paymentMethod);
        //thêm voucher
       // temporaryInvoice.setVoucher(voucher);
        // Kiểm tra giỏ hàng
        List<CartDetail> cartDetails;
        if (temporaryInvoice.getCustomer() != null) {
            cartDetails = cartDetailRepo.findByCustomerIdAndStatus(
                    temporaryInvoice.getCustomer().getId(), (byte) 1);
        } else {
            cartDetails = cartService.getCartDetails();
        }

        if (cartDetails == null || cartDetails.isEmpty()) {
            return "redirect:/cart";
        }

        // Lưu hóa đơn
        Invoice savedInvoice = saveInvoice(temporaryInvoice, cartDetails);

        // Cập nhật trạng thái giỏ hàng
        if (temporaryInvoice.getCustomer() != null) {
            for (CartDetail cart : cartDetails) {
                cart.setStatus((byte) 2); // Đánh dấu là đã thanh toán
                cartDetailRepo.save(cart);
            }
        } else {
            cartService.clear(); // Xóa giỏ hàng nếu người dùng không đăng nhập
        }

        // Xóa hóa đơn tạm thời
        cartService.clearTemporaryInvoice();

        // Chuyển đến trang xác nhận
        return "redirect:/confirmation?invoiceId=" + savedInvoice.getId();
    }

    private Invoice saveInvoice(Invoice invoice, List<CartDetail> cartDetails) {
        // Tạo mã giao dịch duy nhất
        String randomUUID = UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        // Lưu hóa đơn vào cơ sở dữ liệu
        Invoice savedInvoice = invoiceRepo.save(invoice);

        // Lưu chi tiết hóa đơn và cập nhật số lượng sản phẩm
        for (CartDetail cartDetail : cartDetails) {
            if (cartDetail.getProductDetail().getStock() < cartDetail.getQuantity()) {
                throw new IllegalArgumentException("Không đủ hàng cho sản phẩm  : "
                        + cartDetail.getProductDetail().getId());
            }

            // Tạo chi tiết hóa đơn
            InvoiceDetail invoiceDetail = new InvoiceDetail();
            invoiceDetail.setInvoice(savedInvoice);
            invoiceDetail.setProductDetail(cartDetail.getProductDetail());
            invoiceDetail.setQuantity(cartDetail.getQuantity());
            invoiceDetail.setUnitPrice(cartDetail.getProductDetail().getPrice());
            invoiceDetail.setFinalPrice(cartDetail.getProductDetail().getPrice() * cartDetail.getQuantity());
            invoiceDetailRepo.save(invoiceDetail);

            // Cập nhật số lượng trong kho
            ProductDetail productDetail = cartDetail.getProductDetail();
            productDetail.setStock(productDetail.getStock() - cartDetail.getQuantity());
            productDetailRepo.save(productDetail);
        }
        //Set trạng thái hoá đơn đã thanh toán
        savedInvoice.setStatus((byte) 3);
        invoiceRepo.save(savedInvoice);

        // Lưu lịch sử trạng thái hóa đơn
        InvoiceStatusHistory invoiceStatusHistory = new InvoiceStatusHistory();
        invoiceStatusHistory.setInvoice(savedInvoice);
        invoiceStatusHistory.setNewStatus((byte) 3);
        invoiceStatusHistory.setUpdatedAt(LocalDateTime.now());
        invoiceHistoryStatusRepo.save(invoiceStatusHistory);

        // Lưu lịch sử thanh toán
        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setInvoice(savedInvoice);
        paymentHistory.setPaymentDate(LocalDateTime.now());
        paymentHistory.setTransactionCode(randomUUID + "-" + savedInvoice.getInvoiceTrackingNumber());
        paymentHistory.setAmountPaid(savedInvoice.getTotalPrice());
        paymentHistory.setPaymentMethod(savedInvoice.getPaymentMethod());
        paymentHistoryRepo.save(paymentHistory);
        return savedInvoice;
    }

    @GetMapping("/confirmation")
    public String confirmation(@RequestParam("invoiceId") Integer id, Model model) {
        model.addAttribute("invoice", invoiceRepo.findById(id).orElse(null));
        return "test-theme/confirmation";
    }

    @GetMapping("/districts")
    @ResponseBody
    public List<Map<String, Object>> getDistricts(@RequestParam("provinceId") int provinceId) {
        return provincesService.getDistricts(provinceId);
    }

    @GetMapping("/wards")
    @ResponseBody
    public List<Map<String, Object>> getWards(@RequestParam("districtId") int districtId) {
        return provincesService.getWards(districtId);
    }
    @GetMapping("/account")
    public String account(Principal principal,Model model) {
        String username= principal.getName();
        Customer customer = customerRepo.findByUsername(username);
        model.addAttribute("customer",customer);
        return "test-theme/account";
    }
    @PostMapping("/account/update")
    public String updateCustomer(@ModelAttribute Customer customer, BindingResult bindingResult) {
        Customer existingCustomer = customerRepo.findById(customer.getId()).orElse(null);
        if (existingCustomer != null) {
            // Kiểm tra số điện thoại đã tồn tại, nhưng bỏ qua nếu không thay đổi
            if (!existingCustomer.getPhone().equals(customer.getPhone()) && customerRepo.existsByPhone(customer.getPhone())) {
                bindingResult.rejectValue("phone", "error.customer", "Số điện thoại đã tồn tại");
            }
            // Kiểm tra email đã tồn tại, nhưng bỏ qua nếu không thay đổi
            if (!existingCustomer.getEmail().equals(customer.getEmail()) && customerRepo.existsByEmail(customer.getEmail())) {
                bindingResult.rejectValue("email", "error.customer", "Email đã tồn tại");
            }
        }

        // Kiểm tra có lỗi không
        if (bindingResult.hasErrors()) {
            return "admin/customer/customerDetail"; // Trả về trang cập nhật nếu có lỗi
        }
        // Sao chép các trường không có trong form\
        customer.setCountry(addressService.getWardNameByCode(customer.getWard())+' '+addressService.getDistrictNameByCode(customer.getDistrict())+' '+addressService.getProvinceNameByCode(customer.getCity()));
        customer.setStatus(existingCustomer.getStatus());
        customer.setPassword(existingCustomer.getPassword());
        customer.setUsername(existingCustomer.getUsername());
        customer.setUpdatedDate(LocalDateTime.now());
        customer.setPhoto(existingCustomer.getPhoto());
        customerRepo.save(customer);
        return "redirect:/account";
    }
    @GetMapping("/order")
    public String order(Principal principal,Model model) {
        String username= principal.getName();
        Customer customer = customerRepo.findByUsername(username);
        List<Invoice> invoiceList = invoiceRepo.findByCustomerId(customer.getId());
        model.addAttribute("customer",customer);
        model.addAttribute("invoiceList",invoiceList);
        return "test-theme/order";
    }
    @GetMapping("/orderDetail/{id}")
    public String orderdetail(@PathVariable("id") Integer id, Model model) {
        Invoice invoice = invoiceRepo.findById(id).orElse(null);
        Customer customer = customerRepo.findeById(invoice.getCustomer().getId());

        model.addAttribute("customer",customer);
        model.addAttribute("invoice",invoice);
        List<InvoiceDetail> listInvoiceDetail = invoiceDetailRepo.findByInvoiceId(id);
        int totalDiscount = listInvoiceDetail.stream()
                .mapToInt(detail -> detail.getDiscountAmount()!=null?detail.getDiscountAmount():0) // Lấy giá trị discountAmount của từng sản phẩm
                .sum(); // Tính tổng các giảm giá từ sản phẩm
        totalDiscount += invoice.getVoucherDiscount();
        model.addAttribute("totalDiscount", totalDiscount);
        model.addAttribute("listInvoiceDetail",listInvoiceDetail);
        return "test-theme/orderdetail";
    }

    @GetMapping("/look-up")
    public String lookUp() {
        return "test-theme/look-up";
    }
    @PostMapping("/update-cart")
    public String updateCart(@RequestParam("productId") int id,
                             @RequestParam("quantity") int quantity,
                             Principal principal) {
        if (principal != null) {
            Customer customer = customerRepo.findByUsername(principal.getName());
            CartDetail cartDetails = cartDetailRepo.findByCustomerIdAndStatusAndProductDetailId(customer.getId(),(byte) 1,id);
            if (cartDetails != null) {
                if (cartDetails.getQuantity() > quantity) {
                    cartDetails.setQuantity(quantity);
                    cartDetailRepo.save(cartDetails);
                }
                if (cartDetails.getQuantity() < quantity) {
                    cartDetails.setQuantity(quantity);
                    cartDetailRepo.save(cartDetails);
                }
            }
        }else{
            cartService.update(id, quantity);
        }
            return "redirect:/shop-cart";
    }
    @GetMapping("/delete-cart/{id}")
    public String deleteCart(@PathVariable("id") Integer id,Principal principal) {
        if (principal != null) {
            cartDetailRepo.deleteById(id);
        } else {
            cartService.remove(id);
        }
        return "redirect:/shop-cart";
    }
    @GetMapping("/change-pass")
    public String changePass(Model model,Principal principal){
        String username = principal.getName();
        Customer customer = customerRepo.findByUsername(username);
        model.addAttribute("customer",customer);
        return "test-theme/change-pass";
    }
    @PostMapping("/change-pass")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes){
        String username = principal.getName();
        Customer customer = customerRepo.findByUsername(username);
        System.out.println("pass:"+currentPassword+"passnew:"+newPassword+":"+confirmPassword+":"+username);
        if(!passwordEncoder.matches(currentPassword,customer.getPassword())){
            redirectAttributes.addFlashAttribute("error","Mật khẩu hiện tại không chính xác !");
            return "redirect:/change-pass";
        }
        if(!newPassword.equals(confirmPassword)){
            redirectAttributes.addFlashAttribute("error","Mật khẩu xác nhận không khớp với mật khẩu mới !");
            return "redirect:/change-pass";
        }
        customer.setPassword(passwordEncoder.encode(newPassword));
        customerRepo.save(customer);
        redirectAttributes.addFlashAttribute("message","Thay đổi thành công !");
        return "redirect:/change-pass";
    }
}
