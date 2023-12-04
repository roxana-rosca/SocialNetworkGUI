package ro.ubbcluj.map.socialnetworkgui.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message extends Entity<Long> {
    private Long id;
    private Long id_sender;
    private Long id_receiver;
    private String message;
    private LocalDateTime dateTime;
    private String usernameSender;
    private String usernameReceiver;

    public Message(Long id_sender, Long id_receiver, String message) {
        this.id_sender = id_sender;
        this.id_receiver = id_receiver;
        this.message = message;
        this.dateTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public void setMessage(String message) {
        this.message = message;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy");
        String formattedDateTime = dateTime.format(formatter);
        return "[@" + usernameSender + "]: " + message + " [" + formattedDateTime + "]";
    }

    public Long getIDSender() {
        return id_sender;
    }

    public void setIDSender(Long id_sender) {
        this.id_sender = id_sender;
    }

    public Long getIDReceiver() {
        return id_receiver;
    }

    public void setIDReceiver(Long id_receiver) {
        this.id_receiver = id_receiver;
    }

    public String getUsernameSender() {
        return usernameSender;
    }

    public void setUsernameSender(String usernameSender) {
        this.usernameSender = usernameSender;
    }
}
