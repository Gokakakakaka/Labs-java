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
public String getUser(@PathVariable Long id) {
    return users.getUser(id);
}

@GetMapping("/users")
public List getUsers() {
    return users.getUser();
}

@PostMapping("/users")
public String addUser(@RequestBody Map<String, String> data) {

    boolean added = users.addUser(
        data.get("name"),
        data.get("email"),
        data.get("password")
    );

    return added ? "Yes" : "No";
}

@PatchMapping("/users/{id}")
public String updateUser(
        @PathVariable Long id,
        @RequestBody Map<String, String> data) {

    boolean added = users.updateUser(
        id,
        data.get("name"),
        data.get("email"),
        data.get("password")
    );

    return added ? "Yes" : "No";
}

@DeleteMapping("/users/{id}")
public String removeUser(@PathVariable Long id) {

    boolean added = users.removeUser(id);

    return added ? "Yes" : "No";
}

//-----------ITEMS--------------
  @GetMapping("/items/{id}")
  public String getItem(@PathVariable Long id){
    return Item.getItem(id);
  }
  @GetMapping("/items")
  public List getItem(){
    return Item.getItem();
  }
  @PostMapping("/items")
  public String addItem(@RequestBody Map<String, String> data){
    boolean added = Item.addItem(
        Long.parseLong(data.get("user_id")),
        data.get("about"),
        data.get("cndn")
        );
    return added ? "Yes" : "No";
  }
  @PatchMapping("/items/{id}")
  public String updateItem(@PathVariable Long id, @RequestBody Map<String,String> data) {
    boolean added = Item.updateItem(
        id,
        Long.parseLong(data.get("user_id")),
        data.get("about"),
        data.get("cndn")
    );

    return added ? "Yes" : "No";
  }
  @DeleteMapping("/items/{id}")
  public String removeItem(@PathVariable Long id) {
    boolean added = Item.removeItem(id);
    return added ? "Yes" : "No";
  }


//-----------LOTS--------------
  @GetMapping("/lots/{id}")
  public String getLots(@PathVariable Long id){
    return Lots.getLots(id);
  }
  @GetMapping("/lots")
  public List getLots(){
    return Lots.getLots();
  }
  @PostMapping("/lots")
  public String addLots(@RequestBody Map<String, String> data){
    boolean added = Lots.addLots(
        Long.parseLong(data.get("item_id")),
        Long.parseLong(data.get("price")),
        data.get("step"),
        data.get("date"),
        data.get("status")
        );
    return added ? "Yes" : "No";
  }
  @PatchMapping("/lots/{id}")
  public String updateLots(@PathVariable Long id, @RequestBody Map<String,String> data) {
    boolean added = Lots.updateLots(
        id,
        Long.parseLong(data.get("item_id")),
        Long.parseLong(data.get("price")),
        data.get("step"),
        data.get("date"),
        data.get("status")
    );

    return added ? "Yes" : "No";
  }
  @DeleteMapping("/lots/{id}")
  public String removeLots(@PathVariable Long id) {
    boolean added = Lots.removeLots(id);
    return added ? "Yes" : "No";
  }
  //-----------BIRDS--------------
  @GetMapping("/birds/{id}")
  public String getbirds(@PathVariable Long id){
    return Birds.getBirds(id);
  }
  @GetMapping("/birds")
  public List getbirds(){
    return Birds.getBirds();
  }
  @PostMapping("/birds")
  public String addbirds(@RequestBody Map<String, String> data){
    boolean added = Birds.addBirds(
        Long.parseLong(data.get("lot_id")),
        Long.parseLong(data.get("user_id")),
        Long.parseLong(data.get("price")),
        data.get("date")
        );
    return added ? "Yes" : "No";
  }
  @PatchMapping("/birds/{id}")
  public String updatebirds(@PathVariable Long id, @RequestBody Map<String,String> data) {
    boolean added = Birds.updateBirds(
        id,
        Long.parseLong(data.get("lot_id")),
        Long.parseLong(data.get("user_id")),
        Long.parseLong(data.get("price")),
        data.get("date")
    );

    return added ? "Yes" : "No";
  }
  @DeleteMapping("/birds/{id}")
  public String removebirds(@PathVariable Long id) {
    boolean added = Birds.removeBirds(id);
    return added ? "Yes" : "No";
  }
  //-----------DEALS--------------
  @GetMapping("/deals/{id}")
  public String getDeals(@PathVariable Long id){
    return Deals.getDeals(id);
  }
  @GetMapping("/deals")
  public List getDeals(){
    return Deals.getDeals();
  }
  @PostMapping("/deals")
  public String addDeals(@RequestBody Map<String, String> data){
    boolean added = Deals.addDeals(
        Long.parseLong(data.get("lot_id")),
        Long.parseLong(data.get("bird_id")),
        Long.parseLong(data.get("price")),
        data.get("status")
        );
    return added ? "Yes" : "No";
  }
  @PatchMapping("/deals/{id}")
  public String updateDeals(@PathVariable Long id, @RequestBody Map<String,String> data) {
    boolean added = Deals.updateDeals(
        id,
        Long.parseLong(data.get("lot_id")),
        Long.parseLong(data.get("bird_id")),
        Long.parseLong(data.get("price")),
        data.get("status")
        );

    return added ? "Yes" : "No";
  }
  @DeleteMapping("/deals/{id}")
  public String removeDeals(@PathVariable Long id) {
    boolean added = Deals.removeDeals(id);
    return added ? "Yes" : "No";
  }


//-----------BUSINESS OPERATIONS--------------
  @PostMapping("/business/publish-item")
  public String publishItem(@RequestBody Map<String, String> data,
                            @AuthenticationPrincipal CustomUserDetails user) {
    return BusinessOperations.publishItem(
        user.getId(), data.get("about"), data.get("cndn"),
        Long.parseLong(data.get("starting_price")), data.get("step"), data.get("date"));
  }

  @PostMapping("/business/lots/{lotId}/bids")
  public String placeBid(@PathVariable Long lotId, @RequestBody Map<String, String> data,
                         @AuthenticationPrincipal CustomUserDetails user) {
    return BusinessOperations.placeBid(lotId, user.getId(),
        Long.parseLong(data.get("price")), data.get("date"));
  }

  @PostMapping("/business/lots/{lotId}/close")
  @PreAuthorize("hasRole('ADMIN')")
  public String closeAuction(@PathVariable Long lotId) {
    return BusinessOperations.closeAuction(lotId);
  }

  @PostMapping("/business/lots/{lotId}/cancel")
  @PreAuthorize("hasRole('ADMIN')")
  public String cancelAuction(@PathVariable Long lotId) {
    return BusinessOperations.cancelAuction(lotId);
  }

  @DeleteMapping("/business/bids/{bidId}")
  public String withdrawBid(@PathVariable Long bidId,
                            @AuthenticationPrincipal CustomUserDetails user) {
    return BusinessOperations.withdrawBid(bidId, user.getId(), user.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
  }

}

