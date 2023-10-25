package com.abhi.user.service.external.services;

import com.abhi.user.service.entities.Rating;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "RATING-SERVICE")
public interface RatingService {

    @GetMapping("/ratings/users/{userId}")
    Rating[] getRating(@PathVariable String userId);

   /* @PostMapping("/ratings")
    Rating createRating(Rating values);*/



}
