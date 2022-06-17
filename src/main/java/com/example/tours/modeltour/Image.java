package com.example.tours.modeltour;

public class Image {
    private String idImage;
    private String path;

    public Image(String idImage, String path) {
        this.idImage = idImage;
        this.path = path;
    }

    public String getIdImage() {
        return idImage;
    }

    public void setIdImage(String idImage) {
        this.idImage = idImage;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
