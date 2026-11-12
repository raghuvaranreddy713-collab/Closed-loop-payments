package com.paypal.closedloop.service;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.util.*;

@Service
public class RiskAiService {
 public record RiskAssessment(int riskScore, String riskLevel, String plainEnglishReason,
                              String requiredTier, boolean forceStepUp, List<String> riskFactors) {}
 public RiskAssessment evaluateRisk(double amount, double dailySpent, String merchantId,
                                    boolean trusted, LocalTime time) {
  int score = 5; boolean stepUp = false; List<String> factors = new ArrayList<>();
  if (amount > 50000) { score += 45; factors.add("Amount exceeds ₹50,000 high-value threshold"); }
  else if (amount > 10000) { score += 25; factors.add("Amount exceeds ₹10,000 quick-pay threshold"); }
  if (dailySpent + amount > 25000) { score += 25; stepUp = true; factors.add("Projected 24-hour spend exceeds ₹25,000"); }
  if (time.getHour() >= 1 && time.getHour() <= 5) { score += 20; factors.add("Transaction is during unusual late-night hours"); }
  if (!trusted) { score += 15; factors.add("Merchant is not in the trusted list"); }
  else { score = Math.max(0, score - 10); factors.add("Trusted merchant discount applied"); }
  score = Math.min(100, score);
  String tier, level, reason;
  if (amount > 50000 || score >= 70) { tier="TIER_3_HIGH_VALUE"; level="HIGH"; }
  else if (amount > 10000 || stepUp || score >= 35) { tier="TIER_2_STEP_UP"; level="MEDIUM"; }
  else { tier="TIER_1_PIN"; level="LOW"; }
  reason = "Risk " + level + " (" + score + "/100): " + String.join("; ", factors);
  return new RiskAssessment(score, level, reason, tier, stepUp, List.copyOf(factors));
 }
}
