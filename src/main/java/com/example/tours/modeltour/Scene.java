package com.example.tours.modeltour;


import com.example.tours.Exceptions.DataAlraedyExist;
import com.example.tours.Exceptions.DataNotFound;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@RequiredArgsConstructor
@Document(collection = "tours")
public class Scene {

    @Id
    private int id;
    private String name;

    //TODO: Asses need for levels, faceSize and initialViewParameters attributes
    private ArrayList<Image> images;
    private ArrayList<LinkHotspot> linkHotspotsList;
    private ArrayList<InfoHotspot> infoHotspotsList;

    public Scene(int id, String name) {
        this.id = id;
        this.name = name;
        this.images=new ArrayList<>();
        this.linkHotspotsList = new ArrayList<>();
        this.infoHotspotsList = new ArrayList<>();
    }

    public Scene(int id, String name, ArrayList<LinkHotspot> linkHotspotsList, ArrayList<InfoHotspot> infoHotspotsList) {
        this.id = id;
        this.name = name;
        this.linkHotspotsList = linkHotspotsList;
        this.infoHotspotsList = infoHotspotsList;
    }

    /*public Scene(String path){
        this.path=path;
    }*/

    public ArrayList<Image> getImages() {
        return this.images;
    }
    public void addImage(Image image){
        this.images.add(image);
    }
    public Image getImagebyId(String idImagen){
        for (Image image : images) {
            if (image.getIdImage().equals(idImagen)) {
                return image;
            }
        }
        return null;
    }

    public void updateInfoHotspot(HashMap<String,Object> newInfo){

        try {
            InfoHotspot auxHotspot = null;
            int i = 0;
            for (  ; i < this.infoHotspotsList.size(); i++) {
                if (this.infoHotspotsList.get(i).getId() == (int) newInfo.get("id")) {
                    auxHotspot = this.infoHotspotsList.get(i);
                    break;
                }
            }
            newInfo.remove("id");

            for (Field f: auxHotspot.getClass().getDeclaredFields()){
                if( newInfo.containsKey(f.getName().toLowerCase())){
                    f.setAccessible(true);
                    Object newFieldValue = newInfo.get(f.getName().toLowerCase());
                    f.set(auxHotspot, newFieldValue);
                }
            }

            this.infoHotspotsList.set(i,auxHotspot);

        }catch (Exception e){
            throw new DataNotFound("Bad Formatted data");
        }

    }

    public void updateLinkHotspot(HashMap<String,Object> newInfo){

        try {
            LinkHotspot auxHotspot = null;
            int i = 0;
            for (  ; i < this.linkHotspotsList.size(); i++) {
                if (this.linkHotspotsList.get(i).getId() == (int) newInfo.get("id")) {
                    auxHotspot = this.linkHotspotsList.get(i);
                    break;
                }
            }
            newInfo.remove("id");

            Set<String> keys = newInfo.keySet().stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());

            for (Field f: auxHotspot.getClass().getDeclaredFields()){
                if( keys.contains(f.getName().toLowerCase())){
                    f.setAccessible(true);
                    Object newFieldValue = newInfo.get(f.getName().toLowerCase());
                    f.set(auxHotspot, newFieldValue);
                }
            }

            this.linkHotspotsList.set(i,auxHotspot);

        }catch (Exception e){
            throw new DataNotFound("Bad Formatted data");
        }

    }



    public boolean setInfoHotspotById(InfoHotspot newHotspot){

        for (int i=0; i < this.infoHotspotsList.size() ; i++){
            if (this.infoHotspotsList.get(i).getId()== newHotspot.getId()){
                this.infoHotspotsList.set(i,newHotspot);
                return true;
            }
        }
        return false;
    }

    public boolean setLinkHotspotById(LinkHotspot newHotspot){

        for (int i=0; i < this.linkHotspotsList.size() ; i++){
            if (this.linkHotspotsList.get(i).getId()== newHotspot.getId()){
                this.linkHotspotsList.set(i,newHotspot);
                return true;
            }
        }
        return false;
    }


    public boolean infoHotspotExist(InfoHotspot hotspot){
        for (int i=0; i < this.infoHotspotsList.size() ; i++){
            if (this.infoHotspotsList.get(i).getId()== hotspot.getId()){
                return true;
            }
        }

        return false;
    }

    public boolean linkHotspotExist(LinkHotspot hotspot){
        for (int i=0; i < this.linkHotspotsList.size() ; i++){
            if (this.linkHotspotsList.get(i).getId()== hotspot.getId()){
                return true;
            }
        }

        return false;
    }


    public boolean addInfoHotspot(InfoHotspot hotspot) throws DataAlraedyExist {

        if (!infoHotspotExist(hotspot))
            this.infoHotspotsList.add(hotspot);
        else{
            throw new DataAlraedyExist("InfoHotspot Already Exist, try update");
        }
        return true;
    }
    public boolean addLinkHotspot(LinkHotspot hotspot){

        if (!linkHotspotExist(hotspot))
            this.linkHotspotsList.add(hotspot);
        else
            throw new DataAlraedyExist("LinkHotspot Already Exist, try update");
        return true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<LinkHotspot> getLinkHotspotsList() {
        return linkHotspotsList;
    }

    public void setLinkHotspotsList(ArrayList<LinkHotspot> linkHotspotsList) {
        this.linkHotspotsList = linkHotspotsList;
    }

    public ArrayList<InfoHotspot> getInfoHotspotsList() {
        return infoHotspotsList;
    }

    public void setInfoHotspotsList(ArrayList<InfoHotspot> infoHotspotsList) {
        this.infoHotspotsList = infoHotspotsList;
    }
}
