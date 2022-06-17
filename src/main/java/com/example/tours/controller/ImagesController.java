package com.example.tours.controller;


import com.example.tours.Exceptions.DataNotFound;
import com.example.tours.mail.EnviarCorreo;
import com.example.tours.modeltour.Archivo;
import com.example.tours.modeltour.Image;
import com.example.tours.modeltour.Scene;
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
@RequestMapping("/")
public class ImagesController {

    @Autowired
    TourRepo tourRepo;

    @PostMapping("save")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public int guardarImages(@RequestParam("file") MultipartFile file,@RequestParam("carpeta") String carpeta,@RequestParam("nombre") String nombre,@RequestParam("escena")String escena){
        String userHomeDir = System.getProperty("user.home");
        Archivo archivo =new Archivo(userHomeDir+"/imagenestest/"+carpeta+"/"+escena+"/",nombre,file);
        archivo.saveFile(file);
        System.out.println("Zip recibido");

        return 0;
    }
    //este es para fotos tomadas de la camara antes de hacer stitc
    @PostMapping("/saveimagesceneprestitich/{idtour}/{nombreescena}/{idimagen}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public void subirImagenes(@RequestParam("file") MultipartFile file,@RequestParam("nombre")String nombre, @PathVariable("idtour")String idTour,@PathVariable("nombreescena")String escena, @PathVariable("idimagen")String idimagen){
        if(tourRepo.existsById(idTour)){
            Image imagen=tourRepo.findById(idTour).get().getScenebyName(nombre).getImagebyId(idimagen);
            Archivo archivo= new Archivo(imagen.getPath(),nombre,file);
            archivo.saveFile(file);
        }else{
            throw new DataNotFound("str");
        }
    }


    @PostMapping("/subirimagenesescenas/{idtour}/{nombreescena}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public int guardar360(@RequestParam("file") MultipartFile file, @RequestParam("nombre")String nombre, @PathVariable("idtour")String idtour,@PathVariable("nombreescena")String nombreescena){
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

                Scene scene=tourRepo.findById(idtour).get().getScenebyName(nombreescena);
                ArrayList<Image> images = scene.getImages();
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
    @GetMapping("escenas/{idtour}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public HashMap<Integer,String> getEscenas(@PathVariable("idtour")String idtour){
        String userHomeDir = System.getProperty("user.home");
        File dir=new File(userHomeDir+"/imagenestest/"+idtour);
        HashMap<Integer,String> map= new HashMap<>();
        int i=0;
        try {
            File[] files = dir.listFiles();

            for (File file :  files) {
                if (file.isDirectory()) {
                    System.out.println("directory:" + file.getCanonicalPath());
                    map.put(i,file.getCanonicalPath());
                    i++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }catch (NullPointerException ae){
            throw new DataNotFound("");
        }
        return map;
    }
    @GetMapping("realizados/{carpeta}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public HashMap<Integer,String> realizados(@PathVariable("carpeta")String carpeta){
        String userHomeDir = System.getProperty("user.home");
        File file=new  File(userHomeDir+"/imagenestest/"+carpeta+"/fotos/resized.jpg");
        File file2=new  File(userHomeDir+"/imagenestest/"+carpeta+"/fotos/resized2.jpg");
        File file3=new  File(userHomeDir+"/imagenestest/"+carpeta+"/fotos/resized3.jpg");
        HashMap<Integer,String> map=new HashMap<>();
        if(file.exists()){
            map.put(1,"getResultado/"+carpeta);
        }
        if(file2.exists()){
            map.put(2,"getResultado2/"+carpeta);
        }
        if(file3.exists()){
            map.put(3,"getResultado3/"+carpeta);
        }
        return map;
    }
    @GetMapping("stitch")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public int stitch(@RequestParam("carpeta")String carpeta,@RequestParam("escena")String escena,@RequestParam("correo")String correo)  {
        //Process process = Runtime.getRuntime().exec("python3 /home/daniel/PycharmProjects/Dog/main.py /home/daniel/imagenestest/"+carpeta);
        EnviarCorreo enviarCorreo= new EnviarCorreo();
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
        /*Process process= new ProcessBuilder("python3", "/home/daniel/PycharmProjects/Dog/main.py", "/home/daniel/imagenestest/"+carpeta).start();

        try {
            printResults(process);
            return 0;
        } catch (CantStitchException e) {
            System.out.println(e.getMessage());
            return 1;
        }*/

    }

    @GetMapping("getResultado/{carpeta}/{escena}")
    //@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @ResponseBody
    public HttpEntity<byte[]> getResultado(@PathVariable("carpeta") String carpeta, @PathVariable("escena")String escena){
        String userHomeDir = System.getProperty("user.home");
        //System.out.printf("The User Home Directory is %s", userHomeDir);
        File file=new  File(userHomeDir+"/imagenestest/"+carpeta+"/"+escena+"/fotos/resized50P.jpg");

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
    @GetMapping("getResultado2/{carpeta}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    @ResponseBody
    public HttpEntity<byte[]> getResultado2(@PathVariable("carpeta") String carpeta){
        String userHomeDir = System.getProperty("user.home");
        System.out.printf("The User Home Directory is %s", userHomeDir);
        File file=new  File(userHomeDir+"/imagenestest/"+carpeta+"/fotos/resized2.jpg");

        try {
            byte[] image = Files.readAllBytes(file.toPath());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(image.length);
            return new HttpEntity<>(image,headers);
        } catch (IOException e) {
            throw new DataNotFound("No existe imagen ");
        }

    }
    @GetMapping("getTodos")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public HashMap<Integer,String> getTodos(){
        String userHomeDir = System.getProperty("user.home");
        File dir=new File(userHomeDir+"/imagenestest");
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
    @GetMapping("getResultado3/{carpeta}")
    @ResponseBody
    public HttpEntity<byte[]> getResultado3(@PathVariable("carpeta") String carpeta) throws DataNotFound {
        String userHomeDir = System.getProperty("user.home");
        System.out.printf("The User Home Directory 1 is %s", userHomeDir);
        File file=new  File(userHomeDir+"/imagenestest/"+carpeta+"/fotos/resized3.jpg");

        try {
            byte[] image = Files.readAllBytes(file.toPath());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setContentLength(image.length);
            return new HttpEntity<>(image,headers);
        } catch (IOException e) {
            throw new DataNotFound("No existe imagen ");
        }

    }

}
