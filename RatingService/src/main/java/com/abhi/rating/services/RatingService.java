package com.abhi.rating.services;

import com.abhi.rating.entities.Rating;

import java.util.List;

public interface RatingService {

    //create
    Rating create(Rating rating);

    //getall
    List<Rating> getRatings();

    //get all by userId
    List<Rating> getRatingByUserId(String UserId);

    //get all by hotel
    List<Rating> getRatingByHotelId(String HotelId);

}
