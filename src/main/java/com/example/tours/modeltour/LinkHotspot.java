package com.example.tours.modeltour;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@RequiredArgsConstructor
@Document(collection = "tours")
public class LinkHotspot {

    @Id
    private int id;
    private double Yaw;
    private double Pitch;
    private double Rotation;
    private int TargetID;

    public LinkHotspot() {
    }

    public LinkHotspot(int id, double yaw, double pitch, float rotation, int targetID) {
        this.id = id;
        Yaw = yaw;
        Pitch = pitch;
        Rotation = rotation;
        TargetID = targetID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getYaw() {
        return Yaw;
    }

    public void setYaw(double yaw) {
        Yaw = yaw;
    }

    public double getPitch() {
        return Pitch;
    }

    public void setPitch(double pitch) {
        Pitch = pitch;
    }

    public double getRotation() {
        return Rotation;
    }

    public void setRotation(float rotation) {
        Rotation = rotation;
    }

    public int getTargetID() {
        return TargetID;
    }

    public void setTargetID(int targetID) {
        TargetID = targetID;
    }
}
