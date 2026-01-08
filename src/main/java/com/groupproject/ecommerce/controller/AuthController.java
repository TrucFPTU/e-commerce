package com.groupproject.ecommerce.controller;

import com.groupproject.ecommerce.dto.request.LoginRequest;
import com.groupproject.ecommerce.entity.User;
import com.groupproject.ecommerce.service.inter.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final String SESSION_USER = "LOGIN_USER";


    @GetMapping("/login")
    public String showLogin(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/login";
    }
    @PostMapping("/login")
    public String doLogin(@Valid @ModelAttribute("loginRequest") LoginRequest req,
                          BindingResult bindingResult,
                          HttpSession session, Model model) {
      if(bindingResult.hasErrors()) return "auth/login";
      try{
          User user = authService.login(req.getEmail(), req.getPassWord());
          session.setAttribute(SESSION_USER, user);
          switch (user.getRole()) {
              case ADMIN -> {return "redirect:/admin";}
              default -> {return "redirect:/homepage";}
          }

      }catch (RuntimeException e){
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/login";
      }
    }
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

}
