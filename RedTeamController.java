package com.paypal.closedloop.controller;
import com.paypal.closedloop.service.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/ai/red-team") @CrossOrigin(origins="*")
public class RedTeamController {
 private final RedTeamAiService red; private final HabitAiService habits;
 public RedTeamController(RedTeamAiService r,HabitAiService h){red=r;habits=h;}
 @GetMapping("/suite") public List<RedTeamAiService.AttackResult> suite(){return red.runFullTestSuite();}
 @PostMapping("/test") public RedTeamAiService.AttackResult test(@RequestBody Map<String,Object> p){
  return red.testAttackPayload(String.valueOf(p.getOrDefault("attackType","GENERIC")),String.valueOf(p.getOrDefault("payload","")));
 }
 @GetMapping("/habits") public Map<String,Object> habits(){return habits.getInsights("DEMO-USER");}
}
