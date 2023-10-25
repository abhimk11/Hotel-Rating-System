package com.abhi.user.service.controllers;

import com.abhi.user.service.entities.User;
import com.abhi.user.service.services.UserService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    private Logger logger = LoggerFactory.getLogger(UserController.class);
    //create
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user){
        User user1 = userService.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user1);
    }

    //single user get
    int retryCount=1;
    @GetMapping("/{userId}")
    //@CircuitBreaker(name = "ratingHotelBreaker",fallbackMethod = "ratingHotelFallback")
    @Retry(name = "ratingHotelService",fallbackMethod = "ratingHotelFallback")
    //@RateLimiter(name = "userRateLimiter",fallbackMethod = "ratingHotelFallback")
    public ResponseEntity<User> getSingleUser(@PathVariable String userId){
        logger.info("Retry count: {}",retryCount);
        retryCount++;
        User user = userService.getUser(userId);
        return ResponseEntity.ok(user);
    }

    //all user get

    @GetMapping
    @CircuitBreaker(name = "ratingHotelBreaker1",fallbackMethod = "getAllUserFallback")
    public ResponseEntity<List<User>> getAllUser(){
        List<User> allUsers = userService.getAllUser();
        return ResponseEntity.ok(allUsers);
    }

    //creating method for retry

    //creating fallback method for circuit breaker
    public ResponseEntity<List<User>> getAllUserFallback(Exception ex){
        List<User> allUsers = new ArrayList<>();
        User user = User.builder().userId("1234").email("dummy@Gmail.com").about("Not Working").name("Dummy").build();
        allUsers.add(user);
        return ResponseEntity.ok(allUsers);
    }
    //creating fallback method for circuit breaker
    public ResponseEntity<User> ratingHotelFallback(String userId,Exception ex){
        logger.info("Fallback is executed because service is down: ",ex.getMessage());
        User user = User.builder()
                .email("dummy@gmail.com").name("Dummy")
                .about("This user is created dummy because some functions are down")
                .userId("141234")
                .build();
        return new ResponseEntity<>(user,HttpStatus.NOT_FOUND);
    }
}
