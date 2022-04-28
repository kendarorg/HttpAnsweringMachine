// 
// Decompiled by Procyon v0.5.36
// 

package com.wbsoftwareconsutlancy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/ready")
class ReadyHandler
{
    @GetMapping("")
    public void handle(){

    }
}
