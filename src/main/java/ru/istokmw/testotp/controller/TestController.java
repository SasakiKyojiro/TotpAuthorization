package ru.istokmw.testotp.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/test")
public class TestController {
    @GetMapping("/distributer")
    private String distributer() {
        return "distributer";
    }


    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    private String admin() {
        return "admin";
    }


    @GetMapping("/guest")
    @PreAuthorize("hasRole('GUEST')")
    private String guest() {
        return "guest";
    }


    @GetMapping("/seller")
    @PreAuthorize("hasRole('SELLER')")
    private String seller() {
        return "seller";
    }


    @GetMapping("/buyer")
    @PreAuthorize("hasRole('BUYER')")
    private String buyer() {
        return "buyer";
    }


    @GetMapping("/manufacture")
    @PreAuthorize("hasRole('MANUFACTURE')")
    private String manufacture() {
        return "manufacture";
    }
}
