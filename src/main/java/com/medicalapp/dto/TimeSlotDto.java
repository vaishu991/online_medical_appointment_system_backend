package com.medicalapp.dto;

import java.time.LocalTime;

public class TimeSlotDto {
    private LocalTime time;
    private String formattedTime;
    private boolean available;
    private String reason;

    public TimeSlotDto() {
    }

    public TimeSlotDto(LocalTime time, String formattedTime, boolean available, String reason) {
        this.time = time;
        this.formattedTime = formattedTime;
        this.available = available;
        this.reason = reason;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
