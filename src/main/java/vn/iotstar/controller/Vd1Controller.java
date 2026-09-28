package vn.iotstar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.UserService;

@Controller
@RequestMapping("/vd1")
@RequiredArgsConstructor
public class Vd1Controller {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "vd1/auth/login";
    }

    @GetMapping("/home")
    public String home(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null && userDetails.getId() != null) {
            UserDTO userDTO = userService.findById(userDetails.getId());
            model.addAttribute("user", userDTO);
        }
        return "vd1/home";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String admin(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null && userDetails.getId() != null) {
            UserDTO userDTO = userService.findById(userDetails.getId());
            model.addAttribute("user", userDTO);
        }
        model.addAttribute("userList", userService.findAll());
        return "vd1/admin";
    }
}
