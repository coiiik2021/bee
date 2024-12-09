package com.beehat.controller.admin;

import com.beehat.entity.*;
import com.beehat.repository.*;
import com.beehat.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/product")
public class ProductController {
    @Autowired
    ProductRepo productRepo;
    @Autowired
    CategoryRepo categoryRepo;
    @Autowired
    BeltRepo beltRepo;
    @Autowired
    LiningRepo liningRepo;
    @Autowired
    StyleRepo styleRepo;
    @Autowired
    MaterialRepo materialRepo;
    @Autowired
    ProductDetailRepo productDetailRepo;
    @Autowired
    ColorRepo colorRepo;
    @Autowired
    SizeRepo sizeRepo;
    @Autowired
    ProductService productService;
    @Autowired
    private ProductImageRepo productImageRepo;

    @ModelAttribute("iconTitle")
    String iconTitle() {
        return "ph ph-baseball-cap fs-3";
    }
    @ModelAttribute("pageTitle")
    String pageTitle() {
        return "Sản phẩm";
    }

    @ModelAttribute("listCategory")
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

    @ModelAttribute("listMaterial")
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
    @ModelAttribute("listProduct")
    public Page<Product> products(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer materialId,
            @RequestParam(required = false) Integer styleId,
            @RequestParam(required = false) Integer liningId,
            @RequestParam(required = false) Integer beltId,
            @RequestParam(required = false) String name,
            Model model) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<Product> productsPage = productRepo.findByCriteria(
                name,categoryId, materialId, styleId, liningId, beltId, pageable);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productsPage.getTotalPages());
        model.addAttribute("totalItems", productsPage.getTotalElements());
        return productsPage;
    }
    @GetMapping("/index")
    public String product(Model model) {
        model.addAttribute("product", productRepo.findAll());
        return "admin/product/product";
    }
    @GetMapping("/add-product")
    public String addProduct(Model model) {
        model.addAttribute("p", new Product());
        model.addAttribute("isAdd", true);
        return "admin/product/add-product";
    }

    @PostMapping("/add-product")
    public String addProduct(@Valid @ModelAttribute("p") Product p, BindingResult rs,
                             Model model, @RequestParam(value = "file") MultipartFile[] files) {
        if (rs.hasErrors() || files[0].getOriginalFilename().equals("")) {
            model.addAttribute("errorMessage", "Phải tải lên ít nhất một ảnh!");
            model.addAttribute("isAdd", true);
            return "admin/product/add-product";
        }
        // Kiểm tra trùng tên sản phẩm (ngoại trừ sản phẩm hiện tại)
        if (productRepo.existsByName(p.getName())) {
            rs.rejectValue("name", "error.p", "Tên sản phẩm đã tồn tại!");
            model.addAttribute("isAdd", true);
            return "admin/product/add-product";
        }

        String debug = files[0].getOriginalFilename();
        System.out.println(debug);
        // Kiểm tra nếu có file ảnh
        if (files != null && files.length > 0) {
            List<ProductImage> productImages = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // Lưu file ảnh vào thư mục trên server
                    String fileName = file.getOriginalFilename();
                    try {
                        // Định nghĩa đường dẫn lưu ảnh
                        String filePath = "src/main/resources/static/product-img/" + fileName;
                        Path path = Paths.get(filePath);
                        Files.write(path, file.getBytes());

                        // Tạo đối tượng ProductImage
                        ProductImage img = new ProductImage();
                        img.setImageUrl("/product-img/" + fileName);  // Đường dẫn truy cập ảnh
                        img.setProduct(p);  // Liên kết ảnh với sản phẩm
                        productImages.add(img);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Xử lý lỗi khi lưu ảnh
                        model.addAttribute("error", "Error uploading image: " + fileName);
                        return "admin/product/add-product";
                    }
                }
            }
            // Gán danh sách ảnh vào sản phẩm
            p.setImages(productImages);
        }

        // Lưu sản phẩm vào database (giả định bạn có một service để lưu sản phẩm)
        productRepo.save(p);
        return "redirect:/admin/product/index";
    }

    @GetMapping("/add-product-detail/{id}")
    public String addProductDetail(@PathVariable("id") int id, Model model) {
        //tạo ra đối tượng mới để binding dữ liệu với form
        ProductDetail productDetail = new ProductDetail();
        //fix cứng id sản phẩm cho form
        productDetail.setProduct(productRepo.findById(id).orElse(null));
        model.addAttribute("pd", productDetail);
        //hiển thị danh sách sản phẩm chi tiết của sản phẩm
        List<ProductDetail> productDetails = productDetailRepo.findByProductId(id);
        Map<Color, List<ProductDetail>> groupedByColor = productDetails.stream()
                .collect(Collectors.groupingBy(ProductDetail::getColor));
        model.addAttribute("groupedByColor", groupedByColor);
        model.addAttribute("colors", new ArrayList<>()); // Danh sách màu sẽ được chọn
        return "admin/product/add-product-detail";
    }

    @PostMapping("/add-product-detail/{id}")
    public String addProductDetail(@PathVariable("id") int id,
                                   @Valid @ModelAttribute("pd") ProductDetail pd,
                                   BindingResult rs,
                                   @RequestParam(value = "colors", required = false) List<Integer> colorIds,
                                   @RequestParam(value = "sizes", required = false) List<Integer> sizeIds,
                                   Model model) {

        List<ProductDetail> productDetails = productDetailRepo.findByProductId(id);
        Map<Color, List<ProductDetail>> groupedByColor = productDetails.stream()
                .collect(Collectors.groupingBy(ProductDetail::getColor));
        model.addAttribute("groupedByColor", groupedByColor);
        // Nếu có lỗi, trả về form
        if (rs.hasErrors()) {
            pd.setProduct(productRepo.findById(id).orElse(null));
            model.addAttribute("pd", pd);
            model.addAttribute("product", productRepo.findById(id).orElse(null));
            return "admin/product/add-product-detail";
        }

        // Kiểm tra màu sắc và kích cỡ
        if (colorIds == null || colorIds.isEmpty()) {
            rs.rejectValue("color", "error.pd", "Phải chọn ít nhất một màu sắc!");
        }
        if (sizeIds == null || sizeIds.isEmpty()) {
            rs.rejectValue("size", "error.pd", "Phải chọn ít nhất một kích cỡ!");
        }

        // Nếu có lỗi, trả về trang thêm mới với thông báo lỗi
        if (rs.hasErrors()) {
            return "admin/product/add-product-detail";
        }

        // Lưu ProductDetail cho từng màu sắc và kích cỡ
        for (Integer colorId : colorIds) {
            for (Integer sizeId : sizeIds) {
                if (productDetailRepo.existsByProductIdAndColorIdAndSizeId(id, colorId, sizeId)) {
                    rs.rejectValue("product", "duplicateProductDetail", "Sản phẩm đã tồn tại với màu sắc và kích cỡ này!");
                    model.addAttribute("product", productRepo.findById(id).orElse(null));
                    return "admin/product/add-product-detail";
                }
                ProductDetail productDetail = new ProductDetail();
                productDetail.setProduct(productRepo.findById(id).orElse(null));
                productDetail.setColor(colorRepo.findById(colorId).orElse(null));
                productDetail.setSize(sizeRepo.findById(sizeId).orElse(null));
                productDetail.setPrice(pd.getPrice());
                productDetail.setStock(pd.getStock());
                productDetail.setStatus(pd.getStatus());

                productDetailRepo.save(productDetail);
            }
        }

        return "redirect:/admin/product/add-product-detail/" + id;
    }


    @GetMapping("/update-product/{id}")
    public String updateProduct(@PathVariable("id") int id, Model model) {
        model.addAttribute("p", productRepo.findById(id).orElse(null));
        model.addAttribute("isAdd", false);
        return "admin/product/add-product";
    }

    @PostMapping("/update-product/{id}")
    public String updateProduct(@PathVariable("id") int id, @Valid @ModelAttribute("p") Product p, BindingResult rs,
                                @RequestParam("file") MultipartFile[] files, Model model) {
        if (rs.hasErrors()) {
            model.addAttribute("isAdd", false);
            return "admin/product/add-product";
        }
        // Kiểm tra trùng tên sản phẩm, loại trừ sản phẩm hiện tại
        if (productRepo.existsByNameAndIdNot(p.getName(), id)) {
            rs.rejectValue("name", "error.p", "Tên sản phẩm đã tồn tại!");
            model.addAttribute("isAdd", false);
            return "admin/product/add-product";
        }
        p.setId(id);
        p.setSku(productRepo.findById(id).get().getSku());

        // Kiểm tra nếu có file ảnh
        if (!files[0].getOriginalFilename().equals("")) {
            List<ProductImage> productImages = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // Lưu file ảnh vào thư mục trên server
                    String fileName = file.getOriginalFilename();
                    try {
                        // Định nghĩa đường dẫn lưu ảnh
                        String filePath = "src/main/resources/static/product-img/" + fileName;
                        Path path = Paths.get(filePath);
                        Files.write(path, file.getBytes());

                        // Tạo đối tượng ProductImage
                        ProductImage img = new ProductImage();
                        img.setImageUrl("/product-img/" + fileName);  // Đường dẫn truy cập ảnh
                        img.setProduct(p);  // Liên kết ảnh với sản phẩm
                        productImages.add(img);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Xử lý lỗi khi lưu ảnh
                        model.addAttribute("error", "Error uploading image: " + fileName);
                        return "admin/product/add-product";
                    }
                }
            }
            // Gán danh sách ảnh vào sản phẩm
            productImageRepo.deleteByProductId(id);
            p.setImages(productImages);
        } else {
            List<ProductImage> productImages = productImageRepo.findByProductId(id);
            p.setImages(productImages);
        }
        productRepo.save(p);
        return "redirect:/admin/product/index";
    }
    @GetMapping("/detail-product/{id}")
    public String detailProduct(@PathVariable("id") int id, Model model) {
        model.addAttribute("product", productRepo.findById(id).orElse(null));
        return "admin/product/view-product-detail";
    }
    @PostMapping("/add-category")
    @ResponseBody
    public ResponseEntity<Category> addCategory(@RequestBody Category categorydto) {
        Category category = categoryRepo.save(categorydto); // Lưu danh mục mới
        return ResponseEntity.ok(category); // Trả về đối tượng danh mục vừa thêm
    }
    @GetMapping("/test-page")
    public String viewProducts(Model model,
                               @RequestParam(defaultValue = "1") int page) {
        Pageable pageable = PageRequest.of(page, 5);
        Page<Product> productPage = productRepo.findAll(pageable);
        model.addAttribute("productPage", productPage);
        return "admin/product/test-product"; // Tên file Thymeleaf HTML
    }

    @GetMapping("/test-page/{page}")
    public String getProductsByPage(@PathVariable("page") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 5);
        Page<Product> productPage = productRepo.findAll(pageable);
        model.addAttribute("productPage", productPage);
        return "admin/product/productTable :: productTable";
    }
    @PostMapping("/change-product-detail")
    public String changeProductDetail(@RequestParam("id") int id,
                                      @RequestParam("quantity") int quantity,
                                      @RequestParam("price") int price,
                                      Model model) {
        ProductDetail productDetail = productDetailRepo.findById(id).orElse(null);
        if (productDetail == null) {
            return "redirect:/admin/product/index";
        }
        productDetail.setStock(quantity);
        productDetail.setPrice(price);
        productDetailRepo.save(productDetail);
        return "redirect:/admin/product/detail-product/" + productDetail.getProduct().getId();
    }
    @PostMapping("/change-status-product-detail")
    public String changeStatusProductDetail(@RequestParam("id") int id){
        ProductDetail productDetail = productDetailRepo.findById(id).orElse(null);
        if (productDetail == null) {
            return "redirect:/admin/product/index";
        }
        if (productDetail.getStatus() == 1){
            productDetail.setStatus((byte) 2);
            productDetailRepo.save(productDetail);
            return "redirect:/admin/product/detail-product/" + productDetail.getProduct().getId();
        }
        if (productDetail.getStatus() == 2){
            productDetail.setStatus((byte) 1);
            productDetailRepo.save(productDetail);
            return "redirect:/admin/product/detail-product/" + productDetail.getProduct().getId();
        }
        return "redirect:/admin/product/detail-product/" + productDetail.getProduct().getId();
    }
}
