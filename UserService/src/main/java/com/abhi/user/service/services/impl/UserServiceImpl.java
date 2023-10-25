package com.abhi.user.service.services.impl;

import com.abhi.rating.entities.Hotel;
import com.abhi.user.service.entities.Rating;
import com.abhi.user.service.entities.User;
import com.abhi.user.service.event.UserPlacedEvent;
import com.abhi.user.service.exceptions.ResourceNotFoundException;
import com.abhi.user.service.external.services.HotelService;
import com.abhi.user.service.external.services.RatingService;
import com.abhi.user.service.repositories.UserRepository;
import com.abhi.user.service.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KafkaTemplate<String, UserPlacedEvent> kafkaTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RatingService ratingService;
    @Autowired
    private HotelService hotelService;

    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Override
    public User saveUser(User user) {
        //Generate Unique UserID
        String randomUserId = UUID.randomUUID().toString();
        user.setUserId(randomUserId);
        kafkaTemplate.send("notificationTopic",new UserPlacedEvent(user.getName()));
        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUser() {
        List<User> users = userRepository.findAll();
        for(User user: users) {
            /*ArrayList<Rating> ratingOfUser = restTemplate.getForObject("http://localhost:8091/ratings/users/" + user.getUserId(), ArrayList.class);
            logger.info("{} ", ratingOfUser);
            user.setRatings(ratingOfUser);*/
            //Load balancer can only be used in RestTemplate so have to use it somewhere

            Rating[] ratingOfUser = restTemplate.getForObject("http://RATING-SERVICE/ratings/users/"+user.getUserId(), Rating[].class);

            //Using Feign Client
            //Rating[] ratingOfUser = ratingService.getRating(user.getUserId());

            logger.info("{} ",ratingOfUser);

            List<Rating> ratings = Arrays.stream(ratingOfUser).toList();

            List<Rating> ratingList=ratings.stream().map(rating -> {

                //ResponseEntity<Hotel> forEntity = restTemplate.getForEntity("http://HOTEL-SERVICE/hotels/"+rating.getHotelId(), Hotel.class);
                //Hotel hotel = forEntity.getBody();
                //Using Feign Client

                Hotel hotel = hotelService.getHotel(rating.getHotelId());
                //logger.info("response status code: {} ",forEntity.getStatusCode());
                rating.setHotel(hotel);
                return rating;
            }).collect(Collectors.toList());

            user.setRatings(ratings);

        }
        return users;
    }

    @Override
    public User getUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("User with given id is not found on server"));
        Rating[] ratingOfUser = restTemplate.getForObject("http://RATING-SERVICE/ratings/users/"+user.getUserId(), Rating[].class);
        //Open-feign-client

        //Rating[] ratingOfUser = ratingService.getRating(user.getUserId());

        logger.info("{} ",ratingOfUser);
        List<Rating> ratings = Arrays.stream(ratingOfUser).toList();

        List<Rating> ratingList=ratings.stream().map(rating -> {
            //ResponseEntity<Hotel> forEntity = restTemplate.getForEntity("http://HOTEL-SERVICE/hotels/"+rating.getHotelId(), Hotel.class);
        //Hotel hotel = forEntity.getBody();

        //Open-Feign-Client
        Hotel hotel = hotelService.getHotel(rating.getHotelId());
        //logger.info("response status code: {} ",forEntity.getStatusCode());
        rating.setHotel(hotel);
            return rating;
        }).collect(Collectors.toList());

        user.setRatings(ratings);

        return user;
    }
}
