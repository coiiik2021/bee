package com.beehat.controller.admin;

import com.beehat.entity.Belt;
import com.beehat.repository.BeltRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/belt")
public class BeltController {
    @Autowired
    BeltRepo beltRepo;
    @ModelAttribute("iconTitle")
    String iconTitle() {
        return "ph ph-belt fs-3";
    }
    @ModelAttribute("pageTitle")
    String pageTitle() {
        return "Đai mũ";
    }
    @ModelAttribute("listBelt")
    List<Belt> list() {
        return beltRepo.findAll();
    }

    @GetMapping("/index")
    public String belt(Model model, @ModelAttribute("b") Belt belt) {
        model.addAttribute("belt", beltRepo.findAll());
        return "admin/belt/belt";
    }

    @PostMapping("/index")
    public String create(@Valid @ModelAttribute("b") Belt belt, BindingResult rs, RedirectAttributes redirectAttributes) {
        if (rs.hasErrors()) {
            return "admin/belt/belt";
        }
        beltRepo.save(belt);
        redirectAttributes.addFlashAttribute("message", "Thêm mới thành công!");
        return "redirect:/admin/belt/index";
    }


    @GetMapping("/update/{id}")
    public String update(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("b", beltRepo.findById(id).orElse(null));
        return "admin/belt/form-belt";
    }

    @PostMapping("/update/{id}")
    public String update(@Valid @ModelAttribute("b") Belt belt, BindingResult rs) {
        if (rs.hasErrors()) {
            return "admin/belt/form-belt";
        }
        beltRepo.save(belt);
        return "redirect:/admin/belt/index";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Integer id) {
        beltRepo.deleteById(id);
        return "redirect:/admin/belt/index";
    }
}
