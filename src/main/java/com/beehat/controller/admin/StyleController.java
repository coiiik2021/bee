package com.beehat.controller.admin;

import com.beehat.entity.Style;
import com.beehat.repository.StyleRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/style")
public class StyleController {
    @ModelAttribute("iconTitle")
    String iconTitle() {
        return "ph ph-smiley-wink fs-3";
    }
    @ModelAttribute("pageTitle")
    String pageTitle() {
        return "Kiểu dáng";
    }
    @Autowired
    StyleRepo styleRepo;

    @ModelAttribute("listStyle")
    List<Style> listStyle() {
        return styleRepo.findAll();
    }

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("s", new Style());
        return "admin/style/index";
    }

    @PostMapping("/index")
    public String add(@Valid @ModelAttribute("s") Style style, BindingResult result) {
        if (result.hasErrors()) {
            return "admin/style/index";
        }
        styleRepo.save(style);
        return "redirect:/admin/style/index";
    }

    @GetMapping("/update/{id}")
    public String update(@PathVariable int id, Model model) {
        model.addAttribute("s", styleRepo.findById(id).orElse(null));
        return "admin/style/form-style";
    }

    @PostMapping("/update/{id}")
    public String update(@Valid @ModelAttribute("s") Style style, BindingResult result) {
        if (result.hasErrors()) {
            return "admin/style/form-style";
        }
        styleRepo.save(style);
        return "redirect:/admin/style/index";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id) {
        styleRepo.deleteById(id);
        return "redirect:/admin/style/index";
    }
}
