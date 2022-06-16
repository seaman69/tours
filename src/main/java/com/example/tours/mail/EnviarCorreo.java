package com.example.tours.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EnviarCorreo implements Runnable{
    private String correo1;

    private String mensaje;

    private String asunto;
    @Autowired
    JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    String correo;

    public void crearCorreo(String correo1,String mensaje1,String asunto){
        this.correo1=correo1;

        this.mensaje=mensaje1;

        this.asunto=asunto;
    }
    @Override
    public void run() {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(correo);
        message.setTo(correo1);
        message.setSubject(asunto);
        message.setText(mensaje);
        javaMailSender.send(message);
    }
}
