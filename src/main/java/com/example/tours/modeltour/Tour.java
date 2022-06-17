package com.example.tours.modeltour;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;


//@NoArgsConstructor
//@AllArgsConstructor
//@RequiredArgsConstructor
@Document(collection = "tours")
public class Tour extends Producto{
    @Transient
    public static final String SEQUENCE_NAME = "tours_sequence";

    private int numescenas;
    private ArrayList<Scene> scenesList;



    public Tour(String id, String path,boolean state,String idUsuario) {
        super(id,path,state,idUsuario);
        this.scenesList = new ArrayList<>();
        this.numescenas = this.scenesList.size();
    }


    public Tour() {
    }



    public ArrayList<Scene> getScenesList() {
        return scenesList;
    }

    public void setScenesList(ArrayList<Scene> scenesList) {
        this.scenesList = scenesList;
    }

    public Scene getSceneById(int sceneId){

        for (Scene scene: this.scenesList){
            if (scene.getId() == sceneId ){
                return scene;
            }
        }

        return null;
    }
    public void deleteScenebyName(String nombre){
        for(int i=0;i<this.scenesList.size();i++){
            if(scenesList.get(i).getName().equalsIgnoreCase(nombre)){
                scenesList.remove(i);
                return;
            }
        }
    }
    public Scene getScenebyName(String nombre){
        for(Scene scene: this.scenesList){
            if(scene.getName().equalsIgnoreCase(nombre)){
                return scene;
            }
        }
        return null;
    }
    public void addScene(Scene scene){
        this.scenesList.add(scene);
        this.numescenas = this.scenesList.size();
    }



    public int getNumescenas() {
        return numescenas;
    }

    public void setNumescenas(int numescenas) {
        this.numescenas = numescenas;
    }


/* public Tour(String id, String path) {
        this.id = id;
        this.path = path;
    }*/
}
