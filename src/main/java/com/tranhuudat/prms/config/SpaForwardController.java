package com.tranhuudat.prms.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({
            "/",
            "/login",
            "/dashboard",
            "/dashboard/**",
            "/report",
            "/report/**",
            "/project",
            "/project/**",
            "/resource-allocation",
            "/resource-allocation/**",
            "/employee-ot",
            "/employee-ot/**",
            "/kanban",
            "/kanban/**",
            "/management",
            "/management/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
