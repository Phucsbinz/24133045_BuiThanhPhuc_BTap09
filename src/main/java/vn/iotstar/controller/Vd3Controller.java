package vn.iotstar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.ProductService;
import vn.iotstar.service.UserService;

@Controller
@RequestMapping("/vd3")
@RequiredArgsConstructor
public class Vd3Controller {
    private final UserService users;
    private final ProductService products;

    @GetMapping
    public String index() { return "redirect:/vd3/home"; }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        boolean admin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("userCount", admin ? users.countUsers() : 0L);
        model.addAttribute("productCount", admin ? products.countAll() : products.countByUser(principal.getId()));
        model.addAttribute("myProductCount", products.countByUser(principal.getId()));
        model.addAttribute("isAdmin", admin);
        return "vd3/home";
    }
}
