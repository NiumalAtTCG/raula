package lk.javainstitute.raula.model;

import com.google.firebase.Timestamp;


public class Feedback {
    private String userId;
    private String appointmentId;
    private String message;
    private float rating;
    private Timestamp timestamp; // Define timestamp as a Timestamp object

    // Constructor
    public Feedback(String userId, String appointmentId, String message, float rating, Timestamp timestamp) {
        this.userId = userId;
        this.appointmentId = appointmentId;
        this.message = message;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getMessage() {
        return message;
    }

    public float getRating() {
        return rating;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
