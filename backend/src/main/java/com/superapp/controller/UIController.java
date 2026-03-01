package com.superapp.controller;

import com.superapp.service.DynamicLayoutService;
import com.superapp.model.UIComponent;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ui")
public class UIController {
    private final DynamicLayoutService service;

    public UIController(DynamicLayoutService service) {
        this.service = service;
    }

    @GetMapping
    public List<UIComponent> getUI(@RequestParam(defaultValue = "Normal day") String context) {
        return service.getLayout(context);
    }
}