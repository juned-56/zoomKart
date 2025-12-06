package com.ecom.web.app.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/v1")
public class ZoomKartController {

    @GetMapping("/home")
    public String getHome(String str){
        str = "<html><h1>This is an home page</h1></html>";
        return str;
    }
}
