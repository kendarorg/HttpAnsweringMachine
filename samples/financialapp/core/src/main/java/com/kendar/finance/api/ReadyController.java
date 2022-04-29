// 
// Decompiled by Procyon v0.5.36
// 

package com.kendar.finance.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api/ready")
class ReadyController
{
    @GetMapping("")
    public void handle(){

    }
}
