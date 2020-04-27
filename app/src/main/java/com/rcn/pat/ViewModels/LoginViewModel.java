package com.rcn.pat.ViewModels;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LoginViewModel {
    private boolean Authenticated;
    private boolean Authorized;
    private String Correo_Electronico;
    private String lastLoginDate;
    private String Email;
    @PrimaryKey(autoGenerate = true)
    private long Id;
    private String LastName;
    private String Login;
    private String Name;
    private String Numero_Documento;
    private float idEmpresa;

    public String getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(String lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public boolean isAuthenticated() {
        return Authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        Authenticated = authenticated;
    }

    public boolean isAuthorized() {
        return Authorized;
    }

    public void setAuthorized(boolean authorized) {
        Authorized = authorized;
    }

    public String getCorreo_Electronico() {
        return Correo_Electronico;
    }

    public void setCorreo_Electronico(String correo_Electronico) {
        Correo_Electronico = correo_Electronico;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getLogin() {
        return Login;
    }

    public void setLogin(String login) {
        Login = login;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getNumero_Documento() {
        return Numero_Documento;
    }

    public void setNumero_Documento(String numero_Documento) {
        Numero_Documento = numero_Documento;
    }

    public float getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(float idEmpresa) {
        this.idEmpresa = idEmpresa;
    }
}
