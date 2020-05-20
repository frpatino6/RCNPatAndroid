package com.rcn.pat.ViewModels;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


@Entity
public class ServiceInfo {
    private String CelularConductor;
    private String CelularSolicitante;
    private String DescripcionRecorrido;
    private String FechaFinal;
    private String FechaInicial;
    private String FechaNotification;
    private String FechaPausa;
    private String FechaUltimaNotification;
    private Integer Id;
    private Integer pausedId = 1;
    private String NombreConductor;
    private String NombreModalidadServicio;
    private String NombreProveedor;
    private String NombreTipoVehiculoProgramado;
    private String NombreUsuarioSolicitante;
    private String Observaciones;
    private String Placa;
    private String SolicitudNombre;
    @PrimaryKey(autoGenerate = true)
    private int idService;
    private boolean isNotify = false;//Indica si ya dse notificó que el servicio ha finalizado(LA NOTIICACIÓN PREGUNTA SI DESEA FINALIZAR O CONTINUAR)
    private boolean isPaused = false;
    private boolean isStarted = false;
    private boolean isStoped = true;
    private boolean ishourNotify = false;
    private boolean ishalfhourNotify = false;
    private int lastIdMotivoPausa;
    private String lastLatitude;
    private String lastLongitude;
    private int minutesAfter = 15;//Cantidad de minutos despues de la hora de finalización del servicio para notificar

    public int getLastIdMotivoPausa() {
        return lastIdMotivoPausa;
    }

    public void setLastIdMotivoPausa(int lastIdMotivoPausa) {
        this.lastIdMotivoPausa = lastIdMotivoPausa;
    }

    public String getLastLatitude() {
        return lastLatitude;
    }

    public void setLastLatitude(String lastLatitude) {
        this.lastLatitude = lastLatitude;
    }

    public String getLastLongitude() {
        return lastLongitude;
    }

    public void setLastLongitude(String lastLongitude) {
        this.lastLongitude = lastLongitude;
    }

    public String getFechaUltimaNotification() {
        if (FechaUltimaNotification == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date d = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            String newTime = sdf.format(cal.getTime());
            FechaUltimaNotification = newTime;
        }
        return FechaUltimaNotification;
    }

    public void setFechaUltimaNotification(String fechaUltimaNotification) {
        FechaUltimaNotification = fechaUltimaNotification;
    }
    // Getter Methods

    public int getMinutesAfter() {
        return minutesAfter;
    }

    public void setMinutesAfter(int minutesAfter) {
        this.minutesAfter = minutesAfter;
    }

    public boolean isNotify() {
        return isNotify;
    }

    public void setNotify(boolean notify) {
        isNotify = notify;
    }

    public String getFechaPausa() {
        return FechaPausa == null ? "" : FechaPausa;
    }

    public void setFechaPausa(String fechaPausa) {
        this.FechaPausa = fechaPausa;
    }

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

    public String getFechaNotification() {
        if (FechaNotification == null)
            setNotifyDate();
        return FechaNotification;
    }

    public void setFechaNotification(String fechaNotification) {
        FechaNotification = fechaNotification;
    }

    private void setNotifyDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs
        try {
            Date dtPauseDate = sdf.parse(this.FechaFinal);
            Calendar cal = Calendar.getInstance();
            cal.setTime(dtPauseDate);
            cal.add(Calendar.MINUTE, this.minutesAfter);
            String newTime = sdf.format(cal.getTime());

            FechaNotification = newTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getFechaFinal() {
        return FechaFinal == null ? "" : FechaFinal;
    }

    public void setFechaFinal(String FechaFinal) {
        this.FechaFinal = FechaFinal;
        setNotifyDate();
    }

    public String getFechaInicial() {
        return FechaInicial == null ? "" : FechaInicial;
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
//        pausedId = 0;
        if (isStarted) {
            this.isPaused = false;
            this.isStoped = false;

        }
    }

    public boolean isStoped() {
        return isStoped;
    }

    public void setStoped(boolean stoped) {
        isStoped = stoped;
    }

    public Integer getPausedId() {

        if (!isStoped && pausedId == 0)
            pausedId = 1;
        return pausedId;
    }

    public void setPausedId(Integer pausedId) {
        this.pausedId = pausedId;
    }

    public boolean isIshourNotify() {
        return ishourNotify;
    }

    public void setIshourNotify(boolean ishourNotify) {
        this.ishourNotify = ishourNotify;
    }

    public boolean isIshalfhourNotify() {
        return ishalfhourNotify;
    }

    public void setIshalfhourNotify(boolean ishalfhourNotify) {
        this.ishalfhourNotify = ishalfhourNotify;
    }
}

