package com.example.tours.repository;

import com.example.tours.modeltour.Tour;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TourRepo extends MongoRepository<Tour,String>{
    Tour findTourById(String s);
}
