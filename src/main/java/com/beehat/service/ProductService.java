package com.beehat.service;

import com.beehat.entity.*;
import com.beehat.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    private static final String UPLOAD_DIR = "src/main/resources/static/product-img/";

    @Autowired
    private ProductRepo productRepository;
    @Autowired
    private CategoryRepo categoryRepository;

    @Autowired
    private MaterialRepo materialRepository;

    @Autowired
    private LiningRepo liningRepository;

    @Autowired
    private BeltRepo beltRepository;

    @Autowired
    private StyleRepo styleRepository;

//    public void saveProduct(ProductDTO productForm) throws IOException {
//        Product product = new Product();
//        product.setName(productForm.getName());
//        product.setDescription(productForm.getDescription());
//        // Kiểm tra và tạo thư mục lưu ảnh nếu chưa tồn tại
//        Path uploadPath = Paths.get(UPLOAD_DIR);
//        if (!Files.exists(uploadPath)) {
//            Files.createDirectories(uploadPath);
//        }
//
//        // Lưu ảnh
//        List<ProductImage> images = new ArrayList<>();
//        for (MultipartFile file : productForm.getImages()) {
//            if (!file.isEmpty()) {
//                try {
//                    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//                    Path path = Paths.get(UPLOAD_DIR + fileName);
//                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//
//                    ProductImage image = new ProductImage();
//                    image.setImageUrl(fileName);
//                    image.setProduct(product);
//                    images.add(image);
//                } catch (IOException e) {
//                    throw new IOException("Failed to save image: " + file.getOriginalFilename(), e);
//                }
//            }
//        }
//
//        product.setImages(images);
//        // Tìm và set danh mục (category) từ cơ sở dữ liệu
//        if (productForm.getCategory() != null) {
//            Category category = categoryRepository.findById(productForm.getCategory())
//                    .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));
//            product.setCategory(category);
//        }
//
//        // Tìm và set chất liệu (material) từ cơ sở dữ liệu
//        if (productForm.getMaterial() != null) {
//            Material material = materialRepository.findById(productForm.getMaterial())
//                    .orElseThrow(() -> new IllegalArgumentException("Invalid material ID"));
//            product.setMaterial(material);
//        }
//
//        // Tìm và set vải lót (lining) từ cơ sở dữ liệu
//        if (productForm.getLining() != null) {
//            Lining lining = liningRepository.findById(productForm.getLining())
//                    .orElseThrow(() -> new IllegalArgumentException("Invalid lining ID"));
//            product.setLining(lining);
//        }
//
//        // Tìm và set đai mũ (belt) từ cơ sở dữ liệu
//        if (productForm.getBelt() != null) {
//            Belt belt = beltRepository.findById(productForm.getBelt())
//                    .orElseThrow(() -> new IllegalArgumentException("Invalid belt ID"));
//            product.setBelt(belt);
//        }
//
//        // Tìm và set kiểu dáng (style) từ cơ sở dữ liệu
//        if (productForm.getStyle() != null) {
//            Style style = styleRepository.findById(productForm.getStyle())
//                    .orElseThrow(() -> new IllegalArgumentException("Invalid style ID"));
//            product.setStyle(style);
//        }
//
//        // Set trạng thái của sản phẩm
//        product.setStatus(productForm.getStatus());
//        productRepository.save(product);
//    }
}
