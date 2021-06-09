package com.neo.omen.controller;

import com.neo.omen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OmenController {


    @Autowired
    private UserService userService;

    @GetMapping("/omen")
    public void omen() {
        userService.omen();
    }

}
