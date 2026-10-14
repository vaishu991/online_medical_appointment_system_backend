package com.medicalapp.dto;

import com.medicalapp.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public class AppointmentStatusRequest {

    @NotNull(message = "Status is required")
    private AppointmentStatus status;

    private String notes;

    private String cancellationReason;

    public AppointmentStatusRequest() {
    }

    public AppointmentStatusRequest(AppointmentStatus status, String notes, String cancellationReason) {
        this.status = status;
        this.notes = notes;
        this.cancellationReason = cancellationReason;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
