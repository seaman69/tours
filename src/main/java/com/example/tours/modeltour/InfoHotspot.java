package com.example.tours.modeltour;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@RequiredArgsConstructor
@Document(collection = "tours")
public class InfoHotspot {

    @Id
    private int id;
    private double Yaw;
    private double Pitch;
    private String Title;
    private String Text;

    public InfoHotspot(int id, double yaw, double pitch, String title, String text) {
        this.id = id;
        Yaw = yaw;
        Pitch = pitch;
        Title = title;
        Text = text;
    }

    public InfoHotspot() {
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

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getText() {
        return Text;
    }

    public void setText(String text) {
        Text = text;
    }
}
