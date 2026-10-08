package com.example.demo.controller;

import com.example.demo.dto.RepairOrderDto;
import com.example.demo.entity.RepairStatus;
import com.example.demo.service.RepairOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class RepairOrderWebController {
    private final RepairOrderService service;

    @GetMapping("/")
    public String home() {
        return "redirect:/orders";
    }

    @GetMapping("/orders")
    public String list(Model model, Authentication authentication) {
        model.addAttribute("orders", service.findAll());
        model.addAttribute("username", authentication.getName());
        return "orders/list";
    }

    @GetMapping("/orders/new")
    public String createForm(Model model) {
        model.addAttribute("order", new RepairOrderDto(null, "", "", "", "", "", "", RepairStatus.NEW, null));
        model.addAttribute("statuses", RepairStatus.values());
        return "orders/form";
    }

    @PostMapping("/orders")
    public String create(@Valid @ModelAttribute("order") RepairOrderDto dto, BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("statuses", RepairStatus.values());
            return "orders/form";
        }
        service.create(dto);
        redirectAttributes.addFlashAttribute("message", "Repair order created");
        return "redirect:/orders";
    }

    @GetMapping("/orders/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("order", service.findById(id));
        model.addAttribute("statuses", RepairStatus.values());
        return "orders/form";
    }

    @PostMapping("/orders/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("order") RepairOrderDto dto,
                         BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("statuses", RepairStatus.values());
            return "orders/form";
        }
        service.update(id, dto);
        redirectAttributes.addFlashAttribute("message", "Repair order updated");
        return "redirect:/orders";
    }

    @PostMapping("/admin/orders/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        service.delete(id);
        redirectAttributes.addFlashAttribute("message", "Repair order deleted");
        return "redirect:/orders";
    }
}
