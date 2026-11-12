package com.paypal.closedloop.controller;
import com.paypal.closedloop.service.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController @RequestMapping("/api/payments") @CrossOrigin(origins="*")
public class PaymentController {
 private static final double STEP=10000, HIGH=50000, DAILY=25000;
 private final RiskAiService risk; private final HabitAiService habits; private final MerchantService merchants;
 private double dailySpend=0; private int failedPins=0; private boolean locked=false;
 private final Map<String,Map<String,Object>> orders=new ConcurrentHashMap<>();
 private final Set<String> captured=new HashSet<>();
 private final List<Map<String,Object>> feed=Collections.synchronizedList(new ArrayList<>());
 public PaymentController(RiskAiService r,HabitAiService h,MerchantService m){risk=r;habits=h;merchants=m;}
 @PostMapping("/create-order") public synchronized ResponseEntity<?> createOrder(@RequestBody Map<String,Object> p) {
  double amount=num(p.get("amount")); if(!Double.isFinite(amount)||amount<=0) return ResponseEntity.badRequest().body(Map.of("error","Amount must be a positive number."));
  String code=String.valueOf(p.getOrDefault("couponCode","")); String merchant=String.valueOf(p.getOrDefault("merchantId","MERCH-CHAI-01"));
  boolean trusted=Boolean.parseBoolean(String.valueOf(p.getOrDefault("isTrusted",true)));
  double pct=switch(code.toUpperCase(Locale.ROOT)){case "CHAI20"->20;case "SNACK10"->10;case "HAPPYHOUR25"->25;default->0;};
  double discount=amount*pct/100, payable=amount-discount;
  var assessment=risk.evaluateRisk(payable,dailySpend,merchant,trusted,LocalTime.now());
  String token="TKN-"+UUID.randomUUID().toString().substring(0,8).toUpperCase();
  Map<String,Object> order=new LinkedHashMap<>(); order.put("orderToken",token); order.put("amount",payable);
  order.put("merchantName",p.getOrDefault("merchantName","Chai Point")); order.put("couponCode",code);
  order.put("createdAt",Instant.now()); order.put("risk",assessment); orders.put(token,order);
  return ResponseEntity.ok(Map.of("orderToken",token,"orderId","ORD-"+UUID.randomUUID().toString().substring(0,8),
   "originalAmount",amount,"discountAmount",discount,"finalAmount",payable,"couponApplied",pct>0?code:"NONE",
   "riskAssessment",assessment,"requiredTier",assessment.requiredTier(),"tokenExpirySeconds",180,
   "paymentMode","DEMO_SIMULATION_NOT_REAL_PAYPAL"));
 }
 @PostMapping("/verify-and-pay") public synchronized ResponseEntity<?> verify(@RequestBody Map<String,Object> p) {
  if(locked) return ResponseEntity.status(423).body(Map.of("error","Demo account locked after 3 incorrect PIN attempts."));
  String token=String.valueOf(p.getOrDefault("orderToken","")); Map<String,Object> order=orders.get(token);
  if(order==null) return ResponseEntity.badRequest().body(Map.of("error","Unknown or expired order token."));
  Instant created=(Instant)order.get("createdAt");
  if(Duration.between(created,Instant.now()).getSeconds()>180) {orders.remove(token); return ResponseEntity.status(410).body(Map.of("error","Order token expired."));}
  if(captured.contains(token)) return ResponseEntity.status(409).body(Map.of("error","Order token has already been used."));
  if(!"1234".equals(String.valueOf(p.get("pin")))) {
   failedPins++; if(failedPins>=3){locked=true; return ResponseEntity.status(423).body(Map.of("error","Three incorrect PIN attempts. Demo account locked."));}
   return ResponseEntity.status(401).body(Map.of("error","Invalid demo PIN. Attempts remaining: "+(3-failedPins)));
  }
  failedPins=0; double amount=((Number)order.get("amount")).doubleValue();
  boolean high=amount>HIGH, step=amount>STEP || dailySpend+amount>DAILY;
  String biometric=String.valueOf(p.getOrDefault("biometricToken",""));
  if((high||step) && biometric.isBlank()) return ResponseEntity.status(403).body(Map.of("error","Step-up verification required. Supply a demo biometricToken."));
  captured.add(token); dailySpend+=amount;
  String merchant=String.valueOf(order.get("merchantName"));
  merchants.decrementStock("Masala Chai",1); merchants.addRevenue(amount);
  habits.recordPayment("DEMO-USER",merchant,amount,LocalTime.now(),"General");
  Map<String,Object> tx=new LinkedHashMap<>(); tx.put("orderToken",token); tx.put("merchantName",merchant);
  tx.put("amount",amount); tx.put("couponCode",order.get("couponCode")); tx.put("tier",high?"TIER_3_HIGH_VALUE":step?"TIER_2_STEP_UP":"TIER_1_PIN");
  tx.put("paypalTxnId","DEMO-TXN-"+UUID.randomUUID().toString().substring(0,10).toUpperCase());
  tx.put("timestamp",Instant.now().toString()); tx.put("status","DEMO_CAPTURED"); feed.add(0,tx);
  return ResponseEntity.ok(Map.of("status","SUCCESS_DEMO_ONLY","transaction",tx,"tierApplied",tx.get("tier"),
   "soundboxMessage","Demo payment of ₹"+amount+" received.","dailyCumulativeSpent",dailySpend,
   "remainingDailyLimit",Math.max(0,DAILY-dailySpend),"notice","No real PayPal transaction was processed."));
 }
 @PostMapping("/risk-check") public ResponseEntity<?> riskCheck(@RequestBody Map<String,Object> p) {
  double amount=num(p.get("amount")); if(!Double.isFinite(amount)||amount<=0) return ResponseEntity.badRequest().body(Map.of("error","Amount must be positive."));
  return ResponseEntity.ok(risk.evaluateRisk(amount,dailySpend,String.valueOf(p.getOrDefault("merchantId","MERCH-01")),
   Boolean.parseBoolean(String.valueOf(p.getOrDefault("isTrusted",true))),LocalTime.now()));
 }
 @PostMapping("/refund") public ResponseEntity<?> refund(@RequestBody Map<String,Object> p) {
  return ResponseEntity.status(501).body(Map.of("status","NOT_IMPLEMENTED","message","Real PayPal refunds are not connected. This demo backend does not move money."));
 }
 @GetMapping("/feed") public List<Map<String,Object>> feed(){return new ArrayList<>(feed);}
 @GetMapping("/habits") public Map<String,Object> habits(){return habits.getInsights("DEMO-USER");}
 @DeleteMapping("/habits") public Map<String,Object> clearHabits(){habits.clear("DEMO-USER");return Map.of("status","CLEARED_DEMO_DATA");}
 private double num(Object o){try{return Double.parseDouble(String.valueOf(o));}catch(Exception e){return Double.NaN;}}
}
