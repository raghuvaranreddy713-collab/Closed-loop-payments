package com.paypal.closedloop.service;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HabitAiService {
 private final Map<String,List<Map<String,Object>>> history = new ConcurrentHashMap<>();
 public void recordPayment(String userId, String merchant, double amount, LocalTime time, String category) {
  Map<String,Object> row = new LinkedHashMap<>();
  row.put("merchant",merchant); row.put("amount",amount); row.put("time",time.toString()); row.put("category",category);
  history.computeIfAbsent(userId,k->Collections.synchronizedList(new ArrayList<>())).add(row);
 }
 public Map<String,Object> getInsights(String userId) {
  List<Map<String,Object>> rows = history.getOrDefault(userId,List.of());
  Map<String,Object> out = new LinkedHashMap<>();
  out.put("recordCount",rows.size());
  out.put("message", rows.isEmpty() ? "No payment habits have been learned yet." : "Your demo payment history is available for transparent habit insights.");
  out.put("payments", new ArrayList<>(rows));
  return out;
 }
 public void clear(String userId) { history.remove(userId); }
}
