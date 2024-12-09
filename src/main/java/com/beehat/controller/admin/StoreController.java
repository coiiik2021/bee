package com.beehat.controller.admin;

import com.beehat.entity.*;
import com.beehat.repository.*;
import com.beehat.service.InvoiceService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/store")
public class StoreController {
    @Autowired
    InvoiceRepo invoiceRepo;
    @Autowired
    InvoiceDetailRepo invoiceDetailRepo;
    @Autowired
    ProductRepo productRepo;
    @Autowired
    ProductDetailRepo productDetailRepo;
    @Autowired
    PaymentMethodRepo paymentMethodRepo;
    @Autowired
    CustomerRepo customerRepo;
    @Autowired
    VoucherRepo voucherRepo;
    @Autowired
    InvoiceService invoiceService;
    @Autowired
    EmployeeRepo employeeRepo;
    @Autowired
    PaymentHistoryRepo paymentHistoryRepo;
    @Autowired
    ColorRepo colorRepo;
    @Autowired
    SizeRepo sizeRepo;
    @Autowired
    MaterialRepo materialRepo;
    @Autowired
    CategoryRepo categoryRepo;
    @Autowired
    BeltRepo beltRepo;
    @Autowired
    LiningRepo liningRepo;
    @Autowired
    StyleRepo styleRepo;
    @ModelAttribute("iconTitle")
    String iconTitle() {
        return "ph ph-basket fs-3";
    }

    @ModelAttribute("pageTitle")
    String pageTitle() {
        return "Bán hàng tại quầy";
    }

    @ModelAttribute("productDetails")
    public Page<ProductDetail> detail(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(required = false) Integer categoryId,
                                      @RequestParam(required = false) Integer materialId,
                                      @RequestParam(required = false) Integer colorId,
                                      @RequestParam(required = false) Integer sizeId,
                                      @RequestParam(required = false) Integer styleId,
                                      @RequestParam(required = false) Integer liningId,
                                      @RequestParam(required = false) Integer beltId,
                                      @RequestParam(required = false) String keyword, // Nhận giá trị tìm kiếm theo tên
                                      @RequestParam(required = false) String sort,
                                      Model model) {
        // Xử lý sắp xếp theo giá
        Sort sortOrder = Sort.by("price"); // Mặc định sắp xếp theo price
        if ("Giá giảm dần".equals(sort)) {
            sortOrder = Sort.by("price").descending();
        } else if ("Giá tăng dần".equals(sort)) {
            sortOrder = Sort.by("price").ascending();
        }

        Pageable pageable = PageRequest.of(page, 10, sortOrder);
        Page<ProductDetail> productDetailsPage = productDetailRepo.findByCriteria(categoryId, materialId, colorId, sizeId, styleId, liningId, beltId, keyword, pageable);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productDetailsPage.getTotalPages());
        model.addAttribute("totalItems", productDetailsPage.getTotalElements());
        return productDetailsPage;
    }

    @ModelAttribute("listInvoice")
    List<Invoice> listOrder() {
        return invoiceRepo.findByStatusAndInvoiceStatus(Byte.valueOf("0"), (byte) 0);
    }

    // ????
    @ModelAttribute("invoiceDetail")
    List<InvoiceDetail> listOrderDetail() {
        return invoiceDetailRepo.findAll();
    }

    @ModelAttribute("listPayment")
    List<PaymentMethod> listPayment() {
        return paymentMethodRepo.findAll();
    }

    @ModelAttribute("listCustomer")
    List<Customer> listCustomer() {
        return customerRepo.findByStatus((byte) 1);
    }

    @ModelAttribute("listVoucher")
    List<Voucher> listVoucher() {
        return voucherRepo.findByStatus((byte) 1);
    }
    @ModelAttribute("listCate")
    List<Category> listCategory() {
        return categoryRepo.findByStatus(Byte.valueOf("1"));
    }

    @ModelAttribute("listBelt")
    List<Belt> listBelt() {
        return beltRepo.findByStatus(Byte.valueOf("1"));
    }

    @ModelAttribute("listLining")
    List<Lining> listLining() {
        return liningRepo.findByStatus(Byte.valueOf("1"));
    }

    @ModelAttribute("listStyle")
    List<Style> listStyle() {
        return styleRepo.findByStatus(Byte.valueOf("1"));
    }

    @ModelAttribute("listMater")
    List<Material> listMaterial() {
        return materialRepo.findByStatus(Byte.valueOf("1"));
    }

    @ModelAttribute("listColor")
    List<Color> listColor() {
        return colorRepo.findByStatus(Byte.valueOf("1"));
    }

    @ModelAttribute("listSize")
    List<Size> listSize() {
        return sizeRepo.findByStatus(Byte.valueOf("1"));
    }

    @GetMapping("/index")
    public String store(Model model) {
        model.addAttribute("i", new Invoice());
        model.addAttribute("pay", new Invoice());
        model.addAttribute("kh", new Customer());
        return "admin/store/index";
    }

    @PostMapping("/add-invoice")
    public String addInvoice(@ModelAttribute("i") Invoice invoice
            , @RequestParam(name = "username") String username) {
        Employee employee = employeeRepo.findByUsername(username);
        invoice.setEmployee(employee);
        invoice.setInvoiceStatus(Byte.valueOf("0"));
        invoiceRepo.save(invoice);
        return "redirect:/admin/store/invoice-detail/" + invoice.getId();
    }

    @GetMapping("/invoice-detail/{id}")
    public String invoiceDetail(@PathVariable int id, Model model) {
        model.addAttribute("activeInvoiceId", id);
        model.addAttribute("infoInvoice", invoiceRepo.findById(id).orElseThrow(() -> new NullPointerException("Invoice not found")));
        model.addAttribute("totalAmount", invoiceRepo.findById(id).get().getTotalPrice());
        LocalDateTime createdDate = invoiceRepo.findById(id).get().getCreatedDate(); // Lấy giá trị LocalDateTime
        model.addAttribute("kh", new Customer());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String formattedDate = createdDate.format(formatter); // Format thành chuỗi
        model.addAttribute("formattedDate", formattedDate); // Truyền vào model
        List<InvoiceDetail> listInvoiceDetail = invoiceDetailRepo.findByInvoiceId(id);
        model.addAttribute("listInvoiceDetail", listInvoiceDetail);
        model.addAttribute("invoiceId", id);
//        String qrUrl = "https://img.vietqr.io/image/MB-2234686869-compact2.png?amount=" + invoiceRepo.findById(id).get().getTotalPrice() + "&addInfo=Thanh toan hoa don : " + invoiceRepo.findById(id).get().getInvoiceTrackingNumber() + "&accountName=NGUYEN THE QUANG";
//        model.addAttribute("qrUrl", qrUrl);
        return "admin/store/index";
    }

    // Hàm tính giá sau khi giảm
    int getDiscountedPrice(ProductDetail productDetail, Product product) {
        if (product.getPromotion() != null) {
            if (product.getPromotion().getDiscountAmount() != null && product.getPromotion().getDiscountAmount() > 0) {
                return productDetail.getPrice() - product.getPromotion().getDiscountAmount();
            } else if (product.getPromotion().getDiscountPercentage() != null) {
                return productDetail.getPrice() - (productDetail.getPrice() * product.getPromotion().getDiscountPercentage()) / 100;
            }
        }
        return productDetail.getPrice();
    }

    @PostMapping("/add-product-detail-to-invoice")
    public String addInvoice(@RequestParam("invoiceId") Integer idInvoice,
                             @RequestParam("productDetailId") int idProductDetail) {

        ProductDetail productDetail = productDetailRepo.findById(idProductDetail).orElse(null);
        Invoice invoice = invoiceRepo.findById(idInvoice).orElse(null);
        if (productDetail == null || invoice == null) {
            return "redirect:/admin/store/invoice-detail/" + idInvoice;
        }

        List<InvoiceDetail> listInvoiceDetail = invoiceDetailRepo.findByInvoiceId(idInvoice);
        Product product = productDetail.getProduct();
        boolean isExistingProduct = false;

        int unitPrice = getDiscountedPrice(productDetail, product);

        // Kiểm tra sản phẩm đã tồn tại trong hóa đơn chi tiết chưa
        for (InvoiceDetail invoiceDetail : listInvoiceDetail) {
            if (invoiceDetail.getProductDetail().getId().equals(idProductDetail)) {
                isExistingProduct = true;
                invoiceDetail.setUnitPrice(unitPrice);
                invoiceDetail.setQuantity(invoiceDetail.getQuantity() + 1);
                invoiceDetail.setFinalPrice(invoiceDetail.getUnitPrice() * invoiceDetail.getQuantity());
                invoiceDetailRepo.save(invoiceDetail);

                // Giảm số lượng tồn kho của sản phẩm
                productDetail.setStock(productDetail.getStock() - 1);
                productDetailRepo.save(productDetail);
                break;
            }
        }

        // Nếu sản phẩm chưa tồn tại trong hóa đơn
        if (!isExistingProduct) {
            InvoiceDetail invoiceDetail = new InvoiceDetail();
            invoiceDetail.setInvoice(invoice);
            invoiceDetail.setProductDetail(productDetail);
            invoiceDetail.setQuantity(1);
            invoiceDetail.setUnitPrice(unitPrice);
            invoiceDetail.setFinalPrice(unitPrice);

            invoiceDetailRepo.save(invoiceDetail);

            // Giảm số lượng tồn kho của sản phẩm
            productDetail.setStock(productDetail.getStock() - 1);
            productDetailRepo.save(productDetail);

            listInvoiceDetail.add(invoiceDetail);
        }

        // Cập nhật tổng tiền của hóa đơn
        int totalInvoicePrice = listInvoiceDetail.stream()
                .mapToInt(invoiceDetail -> invoiceDetail.getProductDetail().getPrice() * invoiceDetail.getQuantity())
                .sum();
        int discountMoney = totalInvoicePrice - listInvoiceDetail.stream().mapToInt(InvoiceDetail::getFinalPrice).sum();
        invoice.setVoucherDiscount(discountMoney);
        int finalInvoicePrice = totalInvoicePrice - discountMoney;
        invoice.setTotalPrice(totalInvoicePrice);
        invoice.setFinalPrice(finalInvoicePrice);  // Có thể thêm xử lý khác nếu cần
        invoiceRepo.save(invoice);

        return "redirect:/admin/store/invoice-detail/" + idInvoice;
    }


    @GetMapping("/delete-invoice/{id}")
    public String deleteInvoice(@PathVariable int id, Model model) {
        // Tìm và cộng lại số lượng sản phẩm trong kho
        List<InvoiceDetail> invoiceDetails = invoiceDetailRepo.findByInvoiceId(id);
        for (InvoiceDetail invoiceDetail : invoiceDetails) {
            ProductDetail productDetail = invoiceDetail.getProductDetail();
            int updatedStock = productDetail.getStock() + invoiceDetail.getQuantity();
            productDetail.setStock(updatedStock);
        }
        productDetailRepo.saveAll(invoiceDetails.stream().map(InvoiceDetail::getProductDetail).collect(Collectors.toList()));

        Invoice invoice = invoiceRepo.findById(id).orElseThrow(() -> new NullPointerException("Invoice not found"));
        invoice.setStatus(Byte.valueOf("1"));
        invoiceRepo.save(invoice);
        return "redirect:/admin/store/index";
    }

    @PostMapping("/add-new-customer")
    public String addCustomer(@ModelAttribute("kh") Customer customer,
                              @RequestParam("invoiceId") int invoiceId) {
        customerRepo.save(customer);
        return "redirect:/admin/store/invoice-detail/" + invoiceId;
    }

    @GetMapping("/{id}/pdf")
    public void generateInvoicePdf(@PathVariable Integer id, HttpServletResponse response) throws Exception {
        Invoice invoice = invoiceRepo.findById(id).orElse(null);
        byte[] pdfData = invoiceService.generateInvoicePdf(invoice);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=invoice_" + id + ".pdf");
        response.setContentLength(pdfData.length);

        try (OutputStream os = response.getOutputStream()) {
            os.write(pdfData);
            os.flush();
        }

    }

    //???
    @GetMapping("/{id}/view")
    public String viewInvoice(@PathVariable Integer id, Model model) {
        model.addAttribute("invoiceId", id);
        return "/admin/invoice-view";
    }

    @GetMapping("/delete-product-from-invoice/{id}")
    public String deleteProductFromInvoice(@PathVariable int id) {
        InvoiceDetail invoiceDetail = invoiceDetailRepo.findById(id).orElse(null);
        if (invoiceDetail != null) {
            int invoiceId = invoiceDetail.getInvoice().getId();

            ProductDetail productDetail = productDetailRepo.findById(invoiceDetail.getProductDetail().getId()).orElse(null);
            if (productDetail != null) {
                productDetail.setStock(productDetail.getStock() + invoiceDetail.getQuantity());
                productDetailRepo.save(productDetail);
            }
            invoiceDetailRepo.deleteById(id);
            Invoice invoice = invoiceDetail.getInvoice();
            int totalInvoicePrice = invoice.getInvoiceDetails().stream().mapToInt(InvoiceDetail::getFinalPrice).sum();
            invoice.setTotalPrice(totalInvoicePrice);
            invoice.setFinalPrice(totalInvoicePrice);
            invoiceRepo.save(invoice);
            return "redirect:/admin/store/invoice-detail/" + invoiceId;
        }

// Xử lý nếu không tìm thấy invoiceDetail
        return "redirect:/admin/store/invoice-detail";  // Có thể thay đổi đường dẫn nếu cần
    }

    @PostMapping("/update-quantity")
    public String updateQuantity(@RequestParam("ids") int ids, @RequestParam("quantities") int quantities) {

        InvoiceDetail invoiceDetail = invoiceDetailRepo.findById(ids).orElse(null);
        if (invoiceDetail == null) {
            // Thêm thông báo lỗi hoặc trả về trang thông báo lỗi.
            return "redirect:admin/store/index";
        }
        ProductDetail productDetail = invoiceDetail.getProductDetail();
        Product product = productDetail.getProduct();

        // Trường hợp giảm số lượng
        if (invoiceDetail.getQuantity() > quantities) {
            int sl = invoiceDetail.getQuantity() - quantities;
            invoiceDetail.setQuantity(quantities);
            productDetail.setStock(productDetail.getStock() + sl);
        }
        // Trường hợp tăng số lượng
        else if (invoiceDetail.getQuantity() < quantities) {
            int sl = quantities - invoiceDetail.getQuantity();
            invoiceDetail.setQuantity(quantities);
            productDetail.setStock(productDetail.getStock() - sl);
        }

        // Cập nhật giá khuyến mãi nếu có
        int unitPrice = productDetail.getPrice();
        if (product.getPromotion() != null) {
            unitPrice = product.getPromotion().getDiscountAmount() != null && product.getPromotion().getDiscountAmount() > 0
                    ? productDetail.getPrice() - product.getPromotion().getDiscountAmount()
                    : productDetail.getPrice() - (productDetail.getPrice() * product.getPromotion().getDiscountPercentage()) / 100;
        }
        invoiceDetail.setUnitPrice(unitPrice);
        invoiceDetail.setFinalPrice(unitPrice * quantities);

        // Lưu cập nhật cho invoiceDetail và productDetail
        invoiceDetailRepo.save(invoiceDetail);
        productDetailRepo.save(productDetail);

        // Cập nhật tổng giá trị hóa đơn
        Invoice invoice = invoiceDetail.getInvoice();
        int totalPrice = invoice.getInvoiceDetails().stream()
                .mapToInt(detail -> detail.getProductDetail().getPrice() * detail.getQuantity())
                .sum();
        int finalInvoicePrice = invoice.getInvoiceDetails().stream()
                .mapToInt(InvoiceDetail::getFinalPrice)
                .sum();
        int discountMoney = totalPrice - finalInvoicePrice;

        invoice.setVoucherDiscount(discountMoney);
        invoice.setTotalPrice(totalPrice);
        invoice.setFinalPrice(finalInvoicePrice);
        invoiceRepo.save(invoice);

        // Điều hướng về trang chi tiết hóa đơn
        return "redirect:/admin/store/invoice-detail/" + invoice.getId();
    }


    // Hàm tính giá giảm giá
    private int calculateDiscountedPrice(ProductDetail productDetail, Promotion promotion) {
        if (promotion.getDiscountAmount() != null && promotion.getDiscountAmount() > 0) {
            return productDetail.getPrice() - promotion.getDiscountAmount();
        } else if (promotion.getDiscountPercentage() != null) {
            return productDetail.getPrice() - (productDetail.getPrice() * promotion.getDiscountPercentage()) / 100;
        }
        return productDetail.getPrice();
    }


    @PostMapping("/add-customer-to-invoice")
    public String addCustomerToInvoice(@RequestParam("customerId") int customerId, @RequestParam("invoiceId") int invoiceId) {
        Invoice invoice = invoiceRepo.findById(invoiceId).orElse(null);
        Customer customer = customerRepo.findById(customerId).orElse(null);

        if (invoice != null && customer != null) {
            invoice.setCustomer(customer);
            invoiceRepo.save(invoice);
        }

        return "redirect:/admin/store/invoice-detail/" + invoiceId;
    }

    @PostMapping("/updateCustomer")
    public ResponseEntity<String> updateCustomer(
            @RequestParam("invoiceId") Integer invoiceId,
            @RequestParam("customerId") Integer customerId) {
        try {
            Invoice invoice = invoiceRepo.findById(invoiceId).orElse(null);
            Customer customer = customerRepo.findById(customerId).orElse(null);

            if (invoice != null && customer != null) {
                invoice.setCustomer(customer);
                invoiceRepo.save(invoice);
                return ResponseEntity.ok("Cập nhật thành công");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy hóa đơn hoặc khách hàng");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Cập nhật thất bại");
        }
    }

    @PostMapping("/pay")
    public String pay(@RequestParam(name = "idPayment") int id,
                      @RequestParam(name = "totalPayment") int totalPayment,
                      @RequestParam(name = "voucherPayment", defaultValue = "-1") int voucherPayment,
                      @RequestParam(name = "methodPayment") int paymentMethod) {
        System.out.println("Voucher ID received: " + voucherPayment);  // Log giá trị voucherPayment
        String randomUUID = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        Invoice invoice = invoiceRepo.findById(id).orElse(null);
        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setInvoice(invoice);
        paymentHistory.setAmountPaid(totalPayment);
        paymentHistory.setPaymentDate(LocalDateTime.now());
        paymentHistory.setTransactionCode(randomUUID+"-"+invoice.getInvoiceTrackingNumber());
        Voucher voucher = voucherRepo.findById(voucherPayment).orElse(null);
        if (voucher != null) {
            invoice.setVoucher(voucher);
        }

        PaymentMethod paymentMethod1 = paymentMethodRepo.findById(paymentMethod).orElse(null);
        if (paymentMethod1 != null) {
            invoice.setPaymentMethod(paymentMethod1);
            paymentHistory.setPaymentMethod(paymentMethod1);
        }

        invoice.setFinalPrice(totalPayment);
        invoice.setStatus(Byte.valueOf("2"));
        invoiceRepo.save(invoice);
        paymentHistoryRepo.save(paymentHistory);
        return "redirect:/admin/store/index";
    }
}
