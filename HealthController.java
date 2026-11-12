package com.paypal.closedloop.controller;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
public class HealthController {
 @GetMapping("/") public Map<String,String> health(){ return Map.of("status","ok","service","PayAI demo backend"); }
}
