package com.example.tours.modeltour;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "objetos")
public class Objeto extends Producto{
    private String tipo;

    @Transient
    public static final String SEQUENCE_NAME = "objetos_sequence";
    public Objeto(String id,String path,boolean state,String tipo,String idusuario){
        super(id,path,state,idusuario);
        this.tipo=tipo;


    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
