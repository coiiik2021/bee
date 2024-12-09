package com.beehat.controller.admin;

import com.beehat.entity.Voucher;
import com.beehat.repository.VoucherRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/voucher")
public class VoucherController {
    @Autowired
    private VoucherRepo voucherRepo;
    @ModelAttribute("listVoucher")
    public List<Voucher> listVoucher() { return voucherRepo.findAll(); }
    @ModelAttribute("iconTitle")
    String iconTitle() {
        return "ph ph-tag fs-3";
    }
    @ModelAttribute("pageTitle")
    String pageTitle() {
        return "Phiếu giảm giá";
    }
    @GetMapping("/index")
    public String index(Model model) {
        return "admin/voucher/voucher";
    }
    @GetMapping("/add-voucher")
    public String addForm(Model model) {
        model.addAttribute("voucher", new Voucher());
        return "admin/voucher/form-voucher";
    }
    @PostMapping("/add-voucher")
    public String add(@ModelAttribute("voucher") Voucher voucher) {
        voucher.setDiscountAmount(0);
        voucherRepo.save(voucher);
        return "redirect:/admin/voucher/index";
    }

}
