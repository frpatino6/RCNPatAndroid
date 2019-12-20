package com.rcn.pat.ViewModels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ListUserServices {
    private float Id;
    private String SolicitudNombre;
    private String FechaInicial;
    private String FechaFinal;
    private String NombreUsuarioSolicitante;
    private String CelularSolicitante;
    private String DescripcionRecorrido;
    private String Observaciones;
    private String NombreTipoVehiculoProgramado;
    private String NombreModalidadServicio;
    private String NombreProveedor;
    private String Placa;
    private String NombreConductor;
    private String CelularConductor;


    // Getter Methods

    public float getId() {
        return Id;
    }

    public String getSolicitudNombre() {
        return SolicitudNombre;
    }

    public String getFechaInicial() {
        String inputPattern = "yyyy-MM-dd'T'HH:mm:ss";
        String outputPattern = "dd/MM/yyyy HH:mm";

        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(FechaInicial);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    public String getFechaFinal() {
        String inputPattern = "yyyy-MM-dd'T'HH:mm:ss";
        String outputPattern = "dd/MM/yyyy HH:mm";

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

    public String getNombreUsuarioSolicitante() {
        return NombreUsuarioSolicitante;
    }

    public String getCelularSolicitante() {
        return CelularSolicitante;
    }

    public String getDescripcionRecorrido() {
        return DescripcionRecorrido;
    }

    public String getObservaciones() {
        return Observaciones;
    }

    public String getNombreTipoVehiculoProgramado() {
        return NombreTipoVehiculoProgramado;
    }

    public String getNombreModalidadServicio() {
        return NombreModalidadServicio;
    }

    public String getNombreProveedor() {
        return NombreProveedor;
    }

    public String getPlaca() {
        return Placa;
    }

    public String getNombreConductor() {
        return NombreConductor;
    }

    public String getCelularConductor() {
        return CelularConductor;
    }

    // Setter Methods

    public void setId(float Id) {
        this.Id = Id;
    }

    public void setSolicitudNombre(String SolicitudNombre) {
        this.SolicitudNombre = SolicitudNombre;
    }

    public void setFechaInicial(String FechaInicial) {
        this.FechaInicial = FechaInicial;
    }

    public void setFechaFinal(String FechaFinal) {
        this.FechaFinal = FechaFinal;
    }

    public void setNombreUsuarioSolicitante(String NombreUsuarioSolicitante) {
        this.NombreUsuarioSolicitante = NombreUsuarioSolicitante;
    }

    public void setCelularSolicitante(String CelularSolicitante) {
        this.CelularSolicitante = CelularSolicitante;
    }

    public void setDescripcionRecorrido(String DescripcionRecorrido) {
        this.DescripcionRecorrido = DescripcionRecorrido;
    }

    public void setObservaciones(String Observaciones) {
        this.Observaciones = Observaciones;
    }

    public void setNombreTipoVehiculoProgramado(String NombreTipoVehiculoProgramado) {
        this.NombreTipoVehiculoProgramado = NombreTipoVehiculoProgramado;
    }

    public void setNombreModalidadServicio(String NombreModalidadServicio) {
        this.NombreModalidadServicio = NombreModalidadServicio;
    }

    public void setNombreProveedor(String NombreProveedor) {
        this.NombreProveedor = NombreProveedor;
    }

    public void setPlaca(String Placa) {
        this.Placa = Placa;
    }

    public void setNombreConductor(String NombreConductor) {
        this.NombreConductor = NombreConductor;
    }

    public void setCelularConductor(String CelularConductor) {
        this.CelularConductor = CelularConductor;
    }
}
