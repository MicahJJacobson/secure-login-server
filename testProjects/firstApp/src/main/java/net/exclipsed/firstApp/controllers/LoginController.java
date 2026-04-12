package net.exclipsed.firstApp.controllers;

import org.springframework.web.bind.annotation.*;

@RestController
public class LoginController {
    
    private static String storedUsername = "username";
    private static String storedPassword = "password";

    @GetMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password)
    {
        if(storedUsername.equals(username))
        {
            if(storedPassword.equals(password))
            {
                return "Correct!";
            }
            else
            {
                return "Bad password";
            }
        }
        else
        {
            return "Bad username";
        }
    }
}
