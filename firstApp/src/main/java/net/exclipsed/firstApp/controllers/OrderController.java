package net.exclipsed.firstApp.controllers;

import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {
    
    @GetMapping("/order")
    public String order(@RequestParam String item)
    {
        return "Your order for " + item + " has been received";
    }

}
