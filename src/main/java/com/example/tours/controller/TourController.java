package com.example.tours.controller;


import com.example.tours.Exceptions.DataAlraedyExist;
import com.example.tours.Exceptions.DataNotFound;
import com.example.tours.mail.EnviarCorreo;
import com.example.tours.modeltour.*;
import com.example.tours.modeltour.services.SiguienteServicio;
import com.example.tours.repository.TourRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/tours")
public class TourController {
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
    //
    @PostMapping("/nuevaescena/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public int addescena(@PathVariable("id")String id){
        if(tourRepo.findById(id).isPresent()){
            Tour tour=tourRepo.findById(id).get();
            int escena=tour.getNumescenas()+1;
            //tour.setNumescenas(escena+1); //analizar si quitar o no
            String sceneName = "Scene "+ escena;
            Scene scene = new Scene(escena,sceneName,tour.getPath()+"/"+escena);
            tour.addScene(scene);
            System.out.println("holi");
            tourRepo.save(tour);

            return 0;
        }else{
            throw new DataNotFound("str");
        }
    }
    //esta cosa sube las escenas tomadas con la camara del celular
    @PostMapping("/subirimagenesescena/{idtour}/{escena}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public void subirImagenes(@RequestParam("file") MultipartFile file,@RequestParam("nombre")String nombre, @PathVariable("idtour")String idTour,@PathVariable("escena")String escena){
        if(tourRepo.existsById(idTour)){
            Archivo archivo= new Archivo(tourRepo.findById(idTour).get().getPath()+"/"+escena+"/",nombre,file);
            archivo.saveFile(file);
        }else{
            throw new DataNotFound("str");
        }
    }
    @GetMapping("stitch")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public int stitch(@RequestParam("carpeta")String carpeta,@RequestParam("escena")String escena,@RequestParam("correo")String correo)  {
        //Process process = Runtime.getRuntime().exec("python3 /home/daniel/PycharmProjects/Dog/main.py /home/daniel/imagenestest/"+carpeta);
        EnviarCorreo enviarCorreo= new EnviarCorreo();
        ///meter en un hilo de aqui para abajo si hace falta
        String userHomeDir = System.getProperty("user.home");
        //System.out.printf("The User Home Directory is %s", userHomeDir);
        System.out.println();
        Archivo archivo=new Archivo();
        for( int i=1;i<9;i++){
            try {
                archivo.descomprimir(i,carpeta,escena);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Process proc = null;
        try {
            proc = new ProcessBuilder("python3", userHomeDir+"/PycharmProjects/Dog/50PStitch/50PStitch.py", userHomeDir+"/imagenestest/"+carpeta+"/"+escena+"/fotos")
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try(InputStreamReader isr = new InputStreamReader(proc.getInputStream())) {
            int c;
            String line="";
            int i=0;
            while((c = isr.read()) >= 0) {
                System.out.print((char) c);
                line=line+((char) c);
                if(i==2 || line.equals(" **")){
                    System.out.println("error");

                    proc.destroy();
                    proc.destroyForcibly();
                    enviarCorreo.crearCorreo(correo,"Test Stitch","fallo por error DCLAS etc: "+carpeta);
                    Thread thread= new Thread(enviarCorreo);
                    thread.start();
                    return 1;
                }
                i=i+1;
                /*if(c==' '){
                    System.out.println("error");
                    return 1;
                }*/
                System.out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader errores=new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        String str="";
        int error=0;
        try{
            while((str = errores.readLine()) != null) {
                System.out.println("Error: "+str);
                error=1;
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        if(error==1){

            enviarCorreo.crearCorreo(correo,"Test Stitch","fallo por razones normales: "+carpeta);
            Thread thread= new Thread(enviarCorreo);
            thread.start();
            return 1;
        }
        enviarCorreo.crearCorreo(correo,"Test Stitch","Si hizo: "+ carpeta);
        Thread thread= new Thread(enviarCorreo);
        thread.start();
        return 0;


    }
    //carpeta es = idUsuario
    @GetMapping("getResultado/{idusuario}/{idproducto}/{escena}")
    //@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @ResponseBody
    public HttpEntity<byte[]> getResultado(@PathVariable("idusuario") String idusuario, @PathVariable("escena")String escena, @PathVariable("idproducto")String idproducto){
        System.out.println(idusuario+"/"+idproducto+"/"+escena);
        String userHomeDir = System.getProperty("user.home");
        //System.out.printf("The User Home Directory is %s", userHomeDir);
        File file=new  File(userHomeDir+"/toursVirtuales/"+idusuario+"/"+idproducto+"/"+escena+"/fotos/resized.jpg");
        //todo cambiar al los nuevos paths
        try {
            byte[] image = Files.readAllBytes(file.toPath());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(image.length);
            return new HttpEntity<>(image,headers);
        } catch (Exception e) {
            throw new DataNotFound("No existe imagen ");
            //return  new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }
    @GetMapping("gettoursusuario/{idusuario}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public HashMap<Integer,String> getTodos(@PathVariable("idusuario")String idusuario){
        String userHomeDir = System.getProperty("user.home");
        File dir=new File(userHomeDir+"/toursVirtuales/"+idusuario);
        HashMap<Integer,String> map= new HashMap<>();
        int i=0;
        try {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    System.out.println("directory:" + file.getCanonicalPath());
                    map.put(i,file.getName());
                    i++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
    @GetMapping("/numescenas/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public int getescenas(@PathVariable("id")String id){
        if(tourRepo.findById(id).isPresent()){
            Tour tour=tourRepo.findById(id).get();
            return tour.getNumescenas();
        }else{
            return -1;
        }
    }
    @PostMapping("/finalizar/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public int finalizar(@PathVariable("id")String id){
        if(tourRepo.findById(id).isPresent()){
            Tour tour=tourRepo.findById(id).get();
            tour.setState(true);
            tourRepo.save(tour);
            return 0;
        }else{
            throw new DataNotFound("str");
        }


    }
    @GetMapping("/estado/tour/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public boolean estado(@PathVariable("id")String id){
        if(tourRepo.findById(id).isPresent()){
            Tour tour=tourRepo.findById(id).get();
            return tour.isState();
        }else{
            throw new DataNotFound("str");
        }
    }

    //TODO: Fix gettour entry point to take user as parameter
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
    @PostMapping("/subirimagenestour/{idtour}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public int guardar360(@RequestParam("file")MultipartFile file,@RequestParam("nombre")String nombre, @PathVariable("idtour")String idtour){
        if(tourRepo.existsById(idtour)){
            Archivo archivo= new Archivo(tourRepo.findById(idtour).get().getPath()+"/",nombre,file);////
            archivo.saveFile(file);
            try {
                archivo.unzip(tourRepo.findById(idtour).get().getPath()+"/"+nombre,tourRepo.findById(idtour).get().getPath()+"/");
                System.out.println(tourRepo.findById(idtour).get().getPath()+"/"+nombre);
                File file1= new File(tourRepo.findById(idtour).get().getPath()+"/"+nombre);
                file1.delete();
                String[] pathnames;

                // Creates a new File instance by converting the given pathname string
                // into an abstract pathname
                File f = new File(tourRepo.findById(idtour).get().getPath()+"/");

                // Populates the array with names of files and directories
                pathnames = f.list();

                int i=1;
                for (String pathname : pathnames) {
                    // Print the names of files and directories
                    String [] aux=pathname.split("\\.");
                    if (aux.length>1){
                        System.out.println(pathname);
                        File dir=new File(tourRepo.findById(idtour).get().getPath()+"/"+i+"/fotos");
                        dir.mkdirs();
                        InputStream inputStream= Files.newInputStream(Paths.get(tourRepo.findById(idtour).get().getPath() + "/" + pathname));
                        Files.copy(inputStream, Paths.get(dir.getAbsolutePath()+"/resized.jpg"), StandardCopyOption.REPLACE_EXISTING);
                        inputStream.close();
                        i=i+1;
                    }


                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }




        }else{
            throw new DataNotFound("str");
        }
        return 0;
    }

    @PostMapping(value = "/updatescenename/{id}",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public Tour updateLinkHotspot(@PathVariable("id") String tourId, @RequestBody Scene newSceneData){

        Tour tour;
        try {
            tour = tourRepo.findById(tourId).get();
            Scene scene = tour.getSceneById(newSceneData.getId());
//            actualScene.setLinkHotspotById(hotspot);
            scene.setName(newSceneData.getName());
            tourRepo.save(tour);
        }catch (DataNotFound e){
            throw new DataNotFound("str");
        }
        return tour;
    }



}
