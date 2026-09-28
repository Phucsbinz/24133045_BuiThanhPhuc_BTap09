package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.UserService;

@Controller
@RequestMapping("/vd3/users")
@RequiredArgsConstructor
public class Vd3UserController {
    private final UserService users;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size, Model model) {
        model.addAttribute("users", users.search(keyword, page, size));
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", Math.clamp(size, 1, 50));
        return "vd3/users/list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        UserDTO dto = new UserDTO();
        dto.setEnabled(true);
        dto.setRoleName("ROLE_USER");
        model.addAttribute("userDTO", dto);
        model.addAttribute("mode", "create");
        return "vd3/users/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("userDTO") UserDTO dto, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { model.addAttribute("mode", "create"); return "vd3/users/form"; }
        try {
            users.create(dto);
            redirect.addFlashAttribute("success", "Đã tạo tài khoản. Mật khẩu ban đầu: 123456.");
            return "redirect:/vd3/users";
        } catch (IllegalArgumentException e) {
            result.reject("user.error", e.getMessage());
            model.addAttribute("mode", "create");
            return "vd3/users/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("userDTO", users.findById(id));
        model.addAttribute("mode", "edit");
        return "vd3/users/form";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @Valid @ModelAttribute("userDTO") UserDTO dto,
                       BindingResult result, @AuthenticationPrincipal CustomUserDetails actor,
                       Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { model.addAttribute("mode", "edit"); return "vd3/users/form"; }
        try {
            users.update(id, dto, actor.getId());
            redirect.addFlashAttribute("success", "Đã cập nhật tài khoản; phiên đăng nhập trước đó đã bị thu hồi.");
            return "redirect:/vd3/users";
        } catch (IllegalArgumentException e) {
            result.reject("user.error", e.getMessage());
            model.addAttribute("mode", "edit");
            return "vd3/users/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails actor,
                         RedirectAttributes redirect) {
        try {
            users.delete(id, actor.getId());
            redirect.addFlashAttribute("success", "Đã xóa tài khoản.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vd3/users";
    }
}
