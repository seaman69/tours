package com.example.tours.controller;

import com.example.tours.Exceptions.DataAlraedyExist;
import com.example.tours.Exceptions.DataNotFound;
import com.example.tours.modeltour.*;
import com.example.tours.modeltour.services.SiguienteServicio;
import com.example.tours.repository.TourRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/tours2")
public class TourController2 {
    @Autowired
    private SiguienteServicio siguienteServicio;
    @Autowired
    TourRepo tourRepo;


    @PostMapping("/addtour/{idusuario}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public String insertar(@PathVariable("idusuario")String idusuario){
        String idTour= String.valueOf(siguienteServicio.generateSequence(Tour.SEQUENCE_NAME));
        String path=System.getProperty("user.home")+"/toursVirtuales/"+idusuario+"/"+idTour;
        Tour tour=new Tour(idTour,path,false,idusuario);
        tourRepo.save(tour);
        return "redpanda.sytes.net:3000?id="+idTour;
    }


    @GetMapping("/getlinktour/{idtour}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public String getLink(@PathVariable("idtour")String tour){
        if(tourRepo.existsById(tour)){
            return "redpanda.sytes.net:3000?id="+tour;
        }else{
            throw new DataNotFound("");
        }
    }


    @PostMapping("/newscene/{idtour}/{nombreescena}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public int addescena(@PathVariable("idtour")String id,@PathVariable("nombreescena")String nombreescena){
        if(tourRepo.findById(id).isPresent()){
            Tour tour=tourRepo.findById(id).get();
            int escena=tour.getNumescenas()+1;
            //tour.setNumescenas(escena+1); //analizar si quitar o no
            Scene scene = new Scene(escena, nombreescena);
            tour.addScene(scene);
            //System.out.println("holi");
            tourRepo.save(tour);
            return 0;
        }else{
            throw new DataNotFound("str");
        }
    }
    @PostMapping("/newimagescene/{idtour}/{nombreescena}/{idimagen}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public int nuevaimagenescena(@PathVariable("idtour")String idtour,@PathVariable("nombreescena")String nombreescena,@PathVariable("idimagen")String idimagen){
        if(tourRepo.existsById(idtour)){
            Tour tour= tourRepo.findById(idtour).get();
            String path=tour.getPath()+"/"+nombreescena+"/"+idimagen;
            Image image=new Image(idimagen,path);
            Scene scene=tour.getScenebyName(nombreescena);
            scene.addImage(image); //si no se guardan los cambios el error esta aqui
            tourRepo.save(tour);
            return 0;
        }else{
            throw new DataNotFound("");
        }

    }
    @PostMapping("/deleteimagescene/{idtour}/{nombreescena}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public int eliminarScena(@PathVariable("idtour")String idtour,@PathVariable("nombreescena")String nombreescena){
        if(tourRepo.existsById(idtour)){
            Tour tour= tourRepo.findById(idtour).get();
            tour.deleteScenebyName(nombreescena);
            //si no se guardan los cambios el error esta aqui
            tourRepo.save(tour);
            return 0;
        }else{
            throw new DataNotFound("");
        }
    }

    @GetMapping("getimagesscens/{idtour}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public TreeMap<Integer,ArrayList<Image>> getImages(@PathVariable("idtour")String idtour){
        if(tourRepo.existsById(idtour)){
            Tour tour=tourRepo.findById(idtour).get();
            ArrayList<Scene> arrayList= tour.getScenesList();
            TreeMap<Integer,ArrayList<Image>> map=new TreeMap<>();
            for(Scene escena: arrayList ){
                map.put(escena.getId(),escena.getImages());
            }
            return map;
        }else{
            throw new DataNotFound("");
        }
    }


    //inicio cosas del visualizador
    @GetMapping("/gettour/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public Tour getTour(@PathVariable("id")String id){
        Tour tour;
        //System.out.println(tourRepo);
        try{
            tour=tourRepo.findById(id).get();
        }catch (Exception e){
            throw new DataNotFound("str");
        }
        return tour;
    }
    @PostMapping(
            value = "/createscene/{id}",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public Tour createScene(@PathVariable("id") String tourId,@RequestBody Scene scene){

        if (scene.getInfoHotspotsList() == null){
            scene.setInfoHotspotsList(new ArrayList<>());
        }

        if (scene.getLinkHotspotsList() == null){
            scene.setLinkHotspotsList(new ArrayList<>());
        }

        Tour tour;
        try {
            tour = tourRepo.findById(tourId).get();
            tour.addScene(scene);
            tourRepo.save(tour);
        }catch (Exception e){
            throw new DataNotFound("str");
        }

        return tour;
    }


    @PostMapping(value = "/createinfohotspot/{id}/{scene}",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public Tour createInfoHotspot(@PathVariable("id") String tourId,@PathVariable("scene") int sceneId , @RequestBody InfoHotspot hotspot) {
        Tour tour;
        try {
            tour = tourRepo.findById(tourId).get();
            Scene actualScene = tour.getSceneById(sceneId);
            actualScene.addInfoHotspot(hotspot);
            tourRepo.save(tour);
        }catch (DataNotFound e){
            throw new DataNotFound("str");
        }catch (DataAlraedyExist e){
            throw e;
        }
        return tour;
    }

    //    @CrossOrigin
    @PostMapping(value = "/updateinfohotspot/{id}/{scene}",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public Tour updateInfoHotspot(@PathVariable("id") String tourId,@PathVariable("scene") int sceneId , @RequestBody HashMap<String,Object> hotspot) {
        Tour tour;
        try {
            tour = tourRepo.findById(tourId).get();
            Scene actualScene = tour.getSceneById(sceneId);
//            actualScene.setInfoHotspotById(hotspot);
            actualScene.updateInfoHotspot(hotspot);
            tourRepo.save(tour);
        }catch (DataNotFound e){
            throw new DataNotFound("str");
        }
        return tour;
    }


    @PostMapping(value = "/createlinkhotspot/{id}/{scene}",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public Tour createLinkHotspot(@PathVariable("id") String tourId,@PathVariable("scene") int sceneId , @RequestBody LinkHotspot hotspot){

        Tour tour;
        try {
            tour = tourRepo.findById(tourId).get();
            Scene actualScene = tour.getSceneById(sceneId);
            actualScene.addLinkHotspot(hotspot);
            tourRepo.save(tour);
        }catch (DataNotFound e){
            throw new DataNotFound("str");
        }catch (DataAlraedyExist e) {
            throw e;
        }
        return tour;
    }

    @PostMapping(value = "/updatelinkhotspot/{id}/{scene}",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public Tour updateLinkHotspot(@PathVariable("id") String tourId,@PathVariable("scene") int sceneId , @RequestBody HashMap<String,Object> hotspot){

        Tour tour;
        try {
            tour = tourRepo.findById(tourId).get();
            Scene actualScene = tour.getSceneById(sceneId);
//            actualScene.setLinkHotspotById(hotspot);
            actualScene.updateLinkHotspot(hotspot);
            tourRepo.save(tour);
        }catch (DataNotFound e){
            throw new DataNotFound("str");
        }
        return tour;
    }
    //fin cosas del visualizador
}
