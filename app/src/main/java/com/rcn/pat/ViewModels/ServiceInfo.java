package com.rcn.pat.ViewModels;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


@Entity
public class ServiceInfo {
    @PrimaryKey(autoGenerate = true)
    private int idService;
    private String CelularConductor;
    private String CelularSolicitante;
    private String DescripcionRecorrido;
    private String FechaFinal;
    private String FechaInicial;
    private Integer Id;
    private String NombreConductor;
    private String NombreModalidadServicio;
    private String NombreProveedor;
    private String NombreTipoVehiculoProgramado;
    private String NombreUsuarioSolicitante;
    private String Observaciones;
    private String Placa;
    private String SolicitudNombre;
    private boolean isPaused = false;
    private boolean isStarted = false;
    private boolean isStoped = true;
    // Getter Methods

    public int getIdService() {
        return idService;
    }

    public void setIdService(int idService) {
        this.idService = idService;
    }

    public String getCelularConductor() {
        return CelularConductor;
    }

    public void setCelularConductor(String CelularConductor) {
        this.CelularConductor = CelularConductor;
    }

    public String getCelularSolicitante() {
        return CelularSolicitante;
    }

    public void setCelularSolicitante(String CelularSolicitante) {
        this.CelularSolicitante = CelularSolicitante;
    }

    public String getDescripcionRecorrido() {
        return DescripcionRecorrido;
    }

    public void setDescripcionRecorrido(String DescripcionRecorrido) {
        this.DescripcionRecorrido = DescripcionRecorrido;
    }

    public String getFechaFinal() {
        String inputPattern = "yyyy-MM-dd'T'HH:mm:ss";
        String outputPattern = "HH:mm";

        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(FechaFinal);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public void setFechaFinal(String FechaFinal) {
        this.FechaFinal = FechaFinal;
    }

    public String getFechaInicial() {
        String inputPattern = "yyyy-MM-dd'T'HH:mm:ss";
        String outputPattern = "HH:mm";

        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {

            if (FechaInicial != null) {
                date = inputFormat.parse(FechaInicial);
                str = outputFormat.format(date);
            } else {
                return "";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public void setFechaInicial(String FechaInicial) {
        this.FechaInicial = FechaInicial;
    }

    public Integer getId() {
        return Id;
    }

    public void setId(Integer Id) {
        this.Id = Id;
    }

    public String getNombreConductor() {
        return NombreConductor;
    }

    public void setNombreConductor(String NombreConductor) {
        this.NombreConductor = NombreConductor;
    }

    // Setter Methods

    public String getNombreModalidadServicio() {
        return NombreModalidadServicio;
    }

    public void setNombreModalidadServicio(String NombreModalidadServicio) {
        this.NombreModalidadServicio = NombreModalidadServicio;
    }

    public String getNombreProveedor() {
        return NombreProveedor;
    }

    public void setNombreProveedor(String NombreProveedor) {
        this.NombreProveedor = NombreProveedor;
    }

    public String getNombreTipoVehiculoProgramado() {
        return NombreTipoVehiculoProgramado;
    }

    public void setNombreTipoVehiculoProgramado(String NombreTipoVehiculoProgramado) {
        this.NombreTipoVehiculoProgramado = NombreTipoVehiculoProgramado;
    }

    public String getNombreUsuarioSolicitante() {
        return NombreUsuarioSolicitante;
    }

    public void setNombreUsuarioSolicitante(String NombreUsuarioSolicitante) {
        this.NombreUsuarioSolicitante = NombreUsuarioSolicitante;
    }

    public String getObservaciones() {
        return Observaciones;
    }

    public void setObservaciones(String Observaciones) {
        this.Observaciones = Observaciones;
    }

    public String getPlaca() {
        return Placa;
    }

    public void setPlaca(String Placa) {
        this.Placa = Placa;
    }

    public String getSolicitudNombre() {
        return SolicitudNombre;
    }

    public void setSolicitudNombre(String SolicitudNombre) {
        this.SolicitudNombre = SolicitudNombre;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public boolean isStoped() {
        return isStoped;
    }

    public void setStoped(boolean stoped) {
        isStoped = stoped;
    }
}
