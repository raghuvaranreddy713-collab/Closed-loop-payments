package com.paypal.closedloop.service;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class MerchantService {
 public static class InventoryItem {
  public String id, name, category; public double price; public int stock, lowStockThreshold;
  InventoryItem(String id,String name,double price,int stock,int threshold,String category) {
   this.id=id; this.name=name; this.price=price; this.stock=stock; this.lowStockThreshold=threshold; this.category=category;
  }
 }
 public record UdhaarRecord(String customerName,String phone,double amountOwed,int daysOverdue,String lastTransaction) {}
 private final Map<String,InventoryItem> inventory = new LinkedHashMap<>();
 private final List<UdhaarRecord> ledger = new ArrayList<>();
 private final List<Map<String,Object>> coupons = new ArrayList<>();
 private double todayRevenue = 14280;
 public MerchantService() {
  inventory.put("ITEM-1",new InventoryItem("ITEM-1","Masala Chai",20,48,15,"Beverages"));
  inventory.put("ITEM-2",new InventoryItem("ITEM-2","Filter Coffee",30,32,10,"Beverages"));
  inventory.put("ITEM-3",new InventoryItem("ITEM-3","Aloo Samosa (2 pcs)",40,18,10,"Snacks"));
  inventory.put("ITEM-4",new InventoryItem("ITEM-4","Bun Maska",50,12,5,"Snacks"));
  inventory.put("ITEM-5",new InventoryItem("ITEM-5","Organic Green Tea Box",280,6,8,"Retail Pack"));
  ledger.add(new UdhaarRecord("Ramesh Kumar","+91 98765 43210",450,14,"28 Oct 2026"));
  Map<String,Object> c=new LinkedHashMap<>(); c.put("code","CHAI20"); c.put("discountPercent",20); c.put("minSpend",100); c.put("active",true); coupons.add(c);
 }
 public synchronized Collection<InventoryItem> getInventory(){ return new ArrayList<>(inventory.values()); }
 public synchronized boolean decrementStock(String name,int qty) {
  if(qty<=0) return false;
  for(InventoryItem i:inventory.values()) if(i.name.equalsIgnoreCase(name) || name.toLowerCase().contains(i.name.toLowerCase())) {
   if(i.stock<qty) return false; i.stock-=qty; return true;
  } return false;
 }
 public synchronized void addRevenue(double amount){ todayRevenue += amount; }
 public synchronized Map<String,Object> summary(String period) {
  double revenue = period.equalsIgnoreCase("month") ? 348200 : period.equalsIgnoreCase("week") ? 89450 : todayRevenue;
  double expenses = period.equalsIgnoreCase("month") ? 112000 : period.equalsIgnoreCase("week") ? 28100 : 4650;
  Map<String,Object> m=new LinkedHashMap<>(); m.put("period",period); m.put("grossRevenue",revenue);
  m.put("expenses",expenses); m.put("netProfit",revenue-expenses);
  m.put("profitMarginPercent",Math.round((revenue-expenses)/revenue*1000.0)/10.0);
  m.put("peakHours","3:30 PM – 5:30 PM"); m.put("aiSummary","Demo insight: evening beverage orders are a strong sales period. Consider a time-limited offer during quieter hours.");
  return m;
 }
 public List<UdhaarRecord> ledger(){ return new ArrayList<>(ledger); }
 public synchronized List<Map<String,Object>> coupons(){ return new ArrayList<>(coupons); }
 public synchronized Map<String,Object> publishCoupon(Map<String,Object> input) {
  String code=String.valueOf(input.getOrDefault("code","")).trim();
  if(code.isBlank()) throw new IllegalArgumentException("Coupon code is required.");
  Map<String,Object> c=new LinkedHashMap<>(input); c.put("active",true); coupons.add(c); return c;
 }
 public synchronized Map<String,Object> voiceSale(String text) {
  String lower=text==null?"":text.toLowerCase(Locale.ROOT); List<Map<String,Object>> items=new ArrayList<>(); double total=0;
  for(InventoryItem i:inventory.values()) {
   String key=i.name.toLowerCase(Locale.ROOT);
   if(lower.contains(key) || (key.contains("chai") && lower.contains("chai"))) {
    int qty=1; java.util.regex.Matcher m=java.util.regex.Pattern.compile("(\\d+)\\s+").matcher(lower);
    if(m.find()) qty=Math.max(1,Math.min(20,Integer.parseInt(m.group(1))));
    if(i.stock>=qty) { i.stock-=qty; Map<String,Object> row=new LinkedHashMap<>(); row.put("name",i.name); row.put("quantity",qty); row.put("subtotal",i.price*qty); items.add(row); total+=i.price*qty; }
   }
  }
  Map<String,Object> out=new LinkedHashMap<>(); out.put("originalVoiceInput",text); out.put("itemsDetected",items); out.put("totalAmount",total); out.put("status",items.isEmpty()?"NO_MATCH":"SUCCESS"); return out;
 }
}
