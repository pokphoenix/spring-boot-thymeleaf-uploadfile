package com.poktest.spring.boot.thymeleaf.uploadfile.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String upload(){
        return "upload";
    }
}
