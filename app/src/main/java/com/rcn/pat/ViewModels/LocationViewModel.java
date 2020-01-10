package com.rcn.pat.ViewModels;

public class LocationViewModel {
    private String latitude;
    private String longitude;
    private String fechaHora;
    private Integer id;

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocationViewModel(String latitude, String longitude, String fechaHora, Integer id) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.fechaHora = fechaHora;
        this.id = id;
    }
}
