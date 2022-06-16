package com.example.tours.repository;

import com.example.loginjwt.modeltour.Objeto;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ObjetosRepo extends MongoRepository<Objeto,String> {

    List<Objeto> findByIdUsuario(String idusuario);
}
