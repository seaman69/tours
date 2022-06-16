package com.example.tours.repository;

import com.example.loginjwt.modeltour.Tour;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TourRepo extends MongoRepository<Tour,String>{

}
