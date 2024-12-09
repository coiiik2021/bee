package com.beehat.controller.admin;

import com.beehat.entity.Color;
import com.beehat.repository.ColorRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/color")
public class ColorController {
    @Autowired
    ColorRepo colorRepo;
    @ModelAttribute("iconTitle")
    String iconTitle() {
        return "ph ph-palette fs-3";
    }
    @ModelAttribute("pageTitle")
    String pageTitle() {
        return "Màu sắc";
    }

    @ModelAttribute("listColor")
    List<Color> listColor() {
        return colorRepo.findAll();
    }

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("c", new Color());
        return "admin/color/index";
    }

    @PostMapping("/index")
    public String add(@Valid @ModelAttribute("c") Color color, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/color/index";
        }
        colorRepo.save(color);
        return "redirect:/admin/color/index";
    }

    @GetMapping("/update/{id}")
    public String update(@PathVariable int id, Model model) {
        model.addAttribute("c", colorRepo.findById(id).orElse(null));
        return "admin/color/form-color";
    }

    @PostMapping("/update/{id}")
    public String update(@Valid @ModelAttribute("c") Color color, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/color/form-color";
        }
        colorRepo.save(color);
        return "redirect:/admin/color/index";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id) {
        colorRepo.deleteById(id);
        return "redirect:/admin/color/index";
    }
}
