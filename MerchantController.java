package com.paypal.closedloop.controller;
import com.paypal.closedloop.service.MerchantService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/merchant") @CrossOrigin(origins="*")
public class MerchantController {
 private final MerchantService service;
 public MerchantController(MerchantService s){service=s;}
 @GetMapping("/summary") public Map<String,Object> summary(@RequestParam(defaultValue="today") String period){return service.summary(period);}
 @GetMapping("/inventory") public Collection<MerchantService.InventoryItem> inventory(){return service.getInventory();}
 @PostMapping("/inventory/decrement") public Map<String,Object> decrement(@RequestBody Map<String,Object> p){
  String name=String.valueOf(p.getOrDefault("itemName","")); int qty=Integer.parseInt(String.valueOf(p.getOrDefault("quantity",1)));
  boolean ok=service.decrementStock(name,qty); return Map.of("success",ok,"message",ok?"Stock updated.":"Item not found or insufficient stock.");
 }
 @GetMapping("/coupons") public List<Map<String,Object>> coupons(){return service.coupons();}
 @PostMapping("/coupons") public Map<String,Object> coupon(@RequestBody Map<String,Object> p){return service.publishCoupon(p);}
 @GetMapping("/udhaar") public List<MerchantService.UdhaarRecord> udhaar(){return service.ledger();}
 @PostMapping("/voice-sale") public Map<String,Object> voiceSale(@RequestBody Map<String,Object> p){return service.voiceSale(String.valueOf(p.getOrDefault("text","")));}
 @PostMapping("/udhaar/reminder") public Map<String,Object> reminder(@RequestBody Map<String,Object> p){
  String name=String.valueOf(p.getOrDefault("customerName","Customer")); double amount=Double.parseDouble(String.valueOf(p.getOrDefault("amount",0)));
  String tone=String.valueOf(p.getOrDefault("tone","GENTLE"));
  String msg="Hi "+name+", a friendly reminder that ₹"+amount+" is pending. Please pay when convenient. (Demo message; not sent.)";
  if("URGENT".equalsIgnoreCase(tone)) msg="Dear "+name+", please clear the pending ₹"+amount+" balance at your earliest convenience. (Demo message; not sent.)";
  return Map.of("message",msg,"sent",false);
 }
}
