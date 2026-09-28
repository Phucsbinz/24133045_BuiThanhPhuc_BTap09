package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.ProductService;

@Controller
@RequestMapping("/vd3/products")
@RequiredArgsConstructor
public class Vd3ProductController {
    private final ProductService products;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @AuthenticationPrincipal CustomUserDetails principal, Model model) {
        boolean admin = isAdmin(principal);
        model.addAttribute("products", products.findVisible(keyword, page, size, principal.getId(), admin));
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);
        model.addAttribute("isAdmin", admin);
        return "vd3/products/list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("productDTO", new ProductDTO());
        model.addAttribute("mode", "create");
        return "vd3/products/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("productDTO") ProductDTO dto, BindingResult result,
                         @RequestParam(required = false) MultipartFile image,
                         @AuthenticationPrincipal CustomUserDetails principal, Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) { model.addAttribute("mode", "create"); return "vd3/products/form"; }
        try {
            products.create(dto, image, principal.getId());
            redirect.addFlashAttribute("success", "Đã tạo sản phẩm.");
            return "redirect:/vd3/products";
        } catch (IllegalArgumentException e) {
            result.reject("product.error", e.getMessage());
            model.addAttribute("mode", "create");
            return "vd3/products/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("productDTO", products.findVisibleById(id, principal.getId(), isAdmin(principal)));
        model.addAttribute("mode", "edit");
        return "vd3/products/form";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @Valid @ModelAttribute("productDTO") ProductDTO dto,
                       BindingResult result, @RequestParam(required = false) MultipartFile image,
                       @AuthenticationPrincipal CustomUserDetails principal, Model model,
                       RedirectAttributes redirect) {
        if (result.hasErrors()) { model.addAttribute("mode", "edit"); return "vd3/products/form"; }
        try {
            products.update(id, dto, image, principal.getId(), isAdmin(principal));
            redirect.addFlashAttribute("success", "Đã cập nhật sản phẩm.");
            return "redirect:/vd3/products";
        } catch (IllegalArgumentException e) {
            result.reject("product.error", e.getMessage());
            model.addAttribute("mode", "edit");
            return "vd3/products/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails principal,
                         RedirectAttributes redirect) {
        products.delete(id, principal.getId(), isAdmin(principal));
        redirect.addFlashAttribute("success", "Đã xóa sản phẩm.");
        return "redirect:/vd3/products";
    }

    private static boolean isAdmin(CustomUserDetails principal) {
        return principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
