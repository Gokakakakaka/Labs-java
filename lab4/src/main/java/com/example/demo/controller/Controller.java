package com.example.demo.controller;

import com.example.demo.db.*;
import org.springframework.web.bind.annotation.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import com.example.demo.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
public class Controller {
  private final Users users;


    public Controller(Users users) {
        this.users = users;
    }


  public record RegisterRequest(@NotBlank @Size(max=100) String name, @NotBlank @Email @Size(max=190) String email, @NotBlank String password) {}
  @GetMapping("/csrf")
  public Map<String, String> csrf(CsrfToken token) {
      return Map.of("headerName", token.getHeaderName(), "parameterName", token.getParameterName(), "token", token.getToken());
  }

  @GetMapping("/me")
  public Map<String, Object> me(@AuthenticationPrincipal CustomUserDetails user) {
      return Map.of("id", user.getId(), "email", user.getUsername(), "roles", user.getAuthorities());
  }

//-----------USERS--------------
  @GetMapping("/users/{id}")
  public Map<String, Object> getUser(@PathVariable Long id){
    return Users.getUser(id);
  }
  @GetMapping("/users")
  public Map<String, Object> getUsers(){
    return Users.getUser();
  }
  @PostMapping("/users")
  public Object addUser(@RequestBody Map<String, String> data) {
    return Users.addUser(
        data.get("name"), 
        data.get("email"),
        data.get("password")
        );
  }
  @PatchMapping("/users/{id}")
  public Map<String, Object> updateUser(@PathVariable Long id, @RequestBody Map<String,String> data) {
    return Users.updateUser(
        id,
        data.get("name"),
        data.get("email"),
        data.get("password"),
        data.get("role"),
        Boolean.parseBoolean(data.get("enabled")),
        Boolean.parseBoolean(data.get("aln"))
    );
  }
  @DeleteMapping("/users/{id}")
  public Map<String, Object> removeUser(@PathVariable Long id) {
    return Users.removeUser(id);
  }


//-----------ITEMS--------------
  @GetMapping("/items/{id}")
  public Map<String, Object> getItem(@PathVariable Long id){
    return Item.getItem(id);
  }
  @GetMapping("/items")
  public Map<String, Object> getItem(){
    return Item.getItem();
  }
  @PostMapping("/items")
  public Object addItem(@RequestBody Map<String, String> data){
    return Item.addItem(Long.parseLong(data.get("user_id")), data.get("about"), data.get("cndn"));
  }
  @PatchMapping("/items/{id}")
  public Map<String, Object> updateItem(@PathVariable Long id, @RequestBody Map<String,String> data) {
    return Item.updateItem(
        id,
        Long.parseLong(data.get("user_id")),
        data.get("about"),
        data.get("cndn")
    );
  }
  @DeleteMapping("/items/{id}")
  public  Map<String, Object> removeItem(@PathVariable Long id) {
    return Item.removeItem(id);
  }


//-----------LOTS--------------
  @GetMapping("/lots/{id}")
  public Map<String, Object> getLots(@PathVariable Long id){
    return Lots.getLots(id);
  }
  @GetMapping("/lots")
  public Map<String, Object> getLots(){
    return Lots.getLots();
  }
  @PostMapping("/lots")
  public Object addLots(@RequestBody Map<String, String> data){
    return Lots.addLots(Long.parseLong(data.get("item_id")), Long.parseLong(data.get("price")),
        data.get("step"), data.get("date"), data.get("status"));
  }
  @PatchMapping("/lots/{id}")
  public Map<String, Object> updateLots(@PathVariable Long id, @RequestBody Map<String,String> data) {
    return Lots.updateLots(
        id,
        Long.parseLong(data.get("item_id")),
        Long.parseLong(data.get("price")),
        data.get("step"),
        data.get("date"),
        data.get("status")
    );
  }
  @DeleteMapping("/lots/{id}")
  public Map<String, Object> removeLots(@PathVariable Long id) {
    return Lots.removeLots(id);
  }
  //-----------BIRDS--------------
  @GetMapping("/birds/{id}")
  public Map<String, Object> getbirds(@PathVariable Long id){
    return Birds.getBirds(id);
  }
  @GetMapping("/birds")
  public Map<String, Object> getbirds(){
    return Birds.getBirds();
  }
  @PostMapping("/birds")
  public Object addbirds(@RequestBody Map<String, String> data){
    return Birds.addBirds(Long.parseLong(data.get("lot_id")), Long.parseLong(data.get("user_id")),
        Long.parseLong(data.get("price")), data.get("date"));
  }
  @PatchMapping("/birds/{id}")
  public Map<String, Object> updatebirds(@PathVariable Long id, @RequestBody Map<String,String> data) {
    return Birds.updateBirds(
        id,
        Long.parseLong(data.get("lot_id")),
        Long.parseLong(data.get("user_id")),
        Long.parseLong(data.get("price")),
        data.get("date")
    );
  }
  @DeleteMapping("/birds/{id}")
  public Map<String, Object> removebirds(@PathVariable Long id) {
    return Birds.removeBirds(id);
  }
  //-----------DEALS--------------
  @GetMapping("/deals/{id}")
  public Map<String, Object> getDeals(@PathVariable Long id){
    return Deals.getDeals(id);
  }
  @GetMapping("/deals")
  public Map<String, Object> getDeals(){
    return Deals.getDeals();
  }
  @PostMapping("/deals")
  public Object addDeals(@RequestBody Map<String, String> data){
    return Deals.addDeals(Long.parseLong(data.get("lot_id")), Long.parseLong(data.get("bird_id")),
        Long.parseLong(data.get("price")), data.get("status"));
  }
  @PatchMapping("/deals/{id}")
  public Map<String, Object> updateDeals(@PathVariable Long id, @RequestBody Map<String,String> data) {
    return Deals.updateDeals(
        id,
        Long.parseLong(data.get("lot_id")),
        Long.parseLong(data.get("bird_id")),
        Long.parseLong(data.get("price")),
        data.get("status")
        );
  }
  @DeleteMapping("/deals/{id}")
  public Map<String, Object> removeDeals(@PathVariable Long id) {
    return Deals.removeDeals(id);
  }



  //-----------BUSINESS OPERATIONS--------------
  @PostMapping("/business/publish-item")
  public Object publishItem(@RequestBody Map<String, String> data,
                            @AuthenticationPrincipal CustomUserDetails user) {
    return BusinessOperations.publishItem(
        user.getId(), data.get("about"), data.get("cndn"),
        Long.parseLong(data.get("starting_price")), data.get("step"), data.get("date"));
  }

  @PostMapping("/business/lots/{lotId}/bids")
  public Object placeBid(@PathVariable Long lotId, @RequestBody Map<String, String> data,
                         @AuthenticationPrincipal CustomUserDetails user) {
    return BusinessOperations.placeBid(lotId, user.getId(),
        Long.parseLong(data.get("price")), data.get("date"));
  }

  @PostMapping("/business/lots/{lotId}/close")
  @PreAuthorize("hasRole('ADMIN')")
  public Object closeAuction(@PathVariable Long lotId) {
    return BusinessOperations.closeAuction(lotId);
  }

  @PostMapping("/business/lots/{lotId}/cancel")
  @PreAuthorize("hasRole('ADMIN')")
  public Object cancelAuction(@PathVariable Long lotId) {
    return BusinessOperations.cancelAuction(lotId);
  }

  @DeleteMapping("/business/bids/{bidId}")
  public Object withdrawBid(@PathVariable Long bidId,
                            @AuthenticationPrincipal CustomUserDetails user) {
    return BusinessOperations.withdrawBid(bidId, user.getId(), user.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
  }
}

