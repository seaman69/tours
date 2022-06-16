package com.example.tours.modeltour;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public abstract class Producto {
    @Id
    private String id;
    private String path; //path en el server
    private boolean state;
    private String idUsuario;

    public Producto(String id, String path, boolean state,String idUsuario) {
        this.id = id;
        this.path = path;
        this.state = state;
        this.idUsuario=idUsuario;
    }

    public Producto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }
}
