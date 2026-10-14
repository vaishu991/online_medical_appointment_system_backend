package com.medicalapp.dto;

import java.util.List;

public class DoctorDashboardDto {
    private long todayAppointmentsCount;
    private long pendingAppointmentsCount;
    private long confirmedAppointmentsCount;
    private long completedAppointmentsCount;
    private long totalAppointmentsCount;
    private List<AppointmentDto> todayAppointments;
    private List<AppointmentDto> upcomingAppointments;

    public DoctorDashboardDto() {
    }

    public long getTodayAppointmentsCount() {
        return todayAppointmentsCount;
    }

    public void setTodayAppointmentsCount(long todayAppointmentsCount) {
        this.todayAppointmentsCount = todayAppointmentsCount;
    }

    public long getPendingAppointmentsCount() {
        return pendingAppointmentsCount;
    }

    public void setPendingAppointmentsCount(long pendingAppointmentsCount) {
        this.pendingAppointmentsCount = pendingAppointmentsCount;
    }

    public long getConfirmedAppointmentsCount() {
        return confirmedAppointmentsCount;
    }

    public void setConfirmedAppointmentsCount(long confirmedAppointmentsCount) {
        this.confirmedAppointmentsCount = confirmedAppointmentsCount;
    }

    public long getCompletedAppointmentsCount() {
        return completedAppointmentsCount;
    }

    public void setCompletedAppointmentsCount(long completedAppointmentsCount) {
        this.completedAppointmentsCount = completedAppointmentsCount;
    }

    public long getTotalAppointmentsCount() {
        return totalAppointmentsCount;
    }

    public void setTotalAppointmentsCount(long totalAppointmentsCount) {
        this.totalAppointmentsCount = totalAppointmentsCount;
    }

    public List<AppointmentDto> getTodayAppointments() {
        return todayAppointments;
    }

    public void setTodayAppointments(List<AppointmentDto> todayAppointments) {
        this.todayAppointments = todayAppointments;
    }

    public List<AppointmentDto> getUpcomingAppointments() {
        return upcomingAppointments;
    }

    public void setUpcomingAppointments(List<AppointmentDto> upcomingAppointments) {
        this.upcomingAppointments = upcomingAppointments;
    }
}
