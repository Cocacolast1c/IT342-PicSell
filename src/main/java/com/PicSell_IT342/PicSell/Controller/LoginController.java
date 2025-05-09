package com.PicSell_IT342.PicSell.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/")
    public String loginPage() {
        return "login";
    }
}
