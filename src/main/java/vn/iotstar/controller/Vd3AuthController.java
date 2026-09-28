package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.*;
import vn.iotstar.service.AuthService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/vd3")
@RequiredArgsConstructor
public class Vd3AuthController {
    private final AuthService authService;

    @GetMapping("/login")
    public String login() { return "vd3/auth/login"; }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "vd3/auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterDTO dto, BindingResult result,
                           Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) return "vd3/auth/register";
        try {
            authService.register(dto);
            redirect.addFlashAttribute("success", "Mã OTP đã được gửi đến email của bạn.");
            return "redirect:/vd3/verify-otp?email=" + URLEncoder.encode(dto.getEmail().trim(), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            result.reject("register.error", e.getMessage());
            return "vd3/auth/register";
        }
    }

    @GetMapping("/verify-otp")
    public String verifyPage(@RequestParam(required = false) String email, Model model) {
        VerifyOtpDTO dto = new VerifyOtpDTO();
        dto.setEmail(email);
        model.addAttribute("verifyOtpDTO", dto);
        return "vd3/auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verify(@Valid @ModelAttribute VerifyOtpDTO dto, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) return "vd3/auth/verify-otp";
        try {
            if (!authService.verifyRegistration(dto.getEmail(), dto.getOtp())) {
                result.reject("otp.error", "OTP không đúng, đã hết hạn hoặc đã vượt quá số lần thử.");
                return "vd3/auth/verify-otp";
            }
            redirect.addFlashAttribute("success", "Email đã xác nhận. Bạn có thể đăng nhập.");
            return "redirect:/vd3/login?verified=true";
        } catch (IllegalArgumentException e) {
            result.reject("otp.error", e.getMessage());
            return "vd3/auth/verify-otp";
        }
    }

    @PostMapping("/resend-register-otp")
    public String resend(@RequestParam String email, RedirectAttributes redirect) {
        try {
            authService.resendRegistrationOtp(email);
            redirect.addFlashAttribute("success", "Đã gửi lại OTP.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vd3/verify-otp?email=" + URLEncoder.encode(email.trim(), StandardCharsets.UTF_8);
    }

    @GetMapping("/forgot-password")
    public String forgot(Model model) {
        model.addAttribute("forgotPasswordDTO", new ForgotPasswordDTO());
        return "vd3/auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgot(@Valid @ModelAttribute ForgotPasswordDTO dto, BindingResult result,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) return "vd3/auth/forgot-password";
        try {
            authService.requestPasswordReset(dto.getEmail());
        } catch (IllegalArgumentException e) {
            result.reject("forgot.error", e.getMessage());
            return "vd3/auth/forgot-password";
        }
        redirect.addFlashAttribute("email", dto.getEmail().trim());
        redirect.addFlashAttribute("success", "Nếu email đủ điều kiện, mã đặt lại sẽ được gửi trong ít phút.");
        return "redirect:/vd3/reset-password";
    }

    @GetMapping("/reset-password")
    public String reset(Model model) {
        ResetPasswordDTO dto = new ResetPasswordDTO();
        Object email = model.asMap().get("email");
        if (email != null) dto.setEmail(email.toString());
        model.addAttribute("resetPasswordDTO", dto);
        return "vd3/auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String reset(@Valid @ModelAttribute ResetPasswordDTO dto, BindingResult result,
                        RedirectAttributes redirect) {
        if (!dto.getPassword().equals(dto.getConfirmPassword()))
            result.rejectValue("confirmPassword", "password.mismatch", "Mật khẩu xác nhận không khớp.");
        if (result.hasErrors()) return "vd3/auth/reset-password";
        try {
            if (!authService.resetPassword(dto.getEmail(), dto.getOtp(), dto.getPassword())) {
                result.reject("otp.error", "OTP không đúng, hết hạn hoặc đã vượt quá số lần thử.");
                return "vd3/auth/reset-password";
            }
            redirect.addFlashAttribute("success", "Đã đổi mật khẩu. Hãy đăng nhập lại.");
            return "redirect:/vd3/login?reset=true";
        } catch (IllegalArgumentException e) {
            result.reject("reset.error", e.getMessage());
            return "vd3/auth/reset-password";
        }
    }
}
