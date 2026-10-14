package com.medicalapp.dto;

import java.util.List;

public class PatientDashboardDto {
    private long totalAppointments;
    private long upcomingAppointments;
    private long completedAppointments;
    private long pendingAppointments;
    private List<AppointmentDto> nextAppointments;
    private List<AppointmentDto> recentHistory;

    public PatientDashboardDto() {
    }

    public long getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(long totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public long getUpcomingAppointments() {
        return upcomingAppointments;
    }

    public void setUpcomingAppointments(long upcomingAppointments) {
        this.upcomingAppointments = upcomingAppointments;
    }

    public long getCompletedAppointments() {
        return completedAppointments;
    }

    public void setCompletedAppointments(long completedAppointments) {
        this.completedAppointments = completedAppointments;
    }

    public long getPendingAppointments() {
        return pendingAppointments;
    }

    public void setPendingAppointments(long pendingAppointments) {
        this.pendingAppointments = pendingAppointments;
    }

    public List<AppointmentDto> getNextAppointments() {
        return nextAppointments;
    }

    public void setNextAppointments(List<AppointmentDto> nextAppointments) {
        this.nextAppointments = nextAppointments;
    }

    public List<AppointmentDto> getRecentHistory() {
        return recentHistory;
    }

    public void setRecentHistory(List<AppointmentDto> recentHistory) {
        this.recentHistory = recentHistory;
    }
}
