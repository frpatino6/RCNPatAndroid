package com.rcn.pat.ViewModels;

public class LoginViewModel {
    private String Login;
    private String Name;
    private String LastName;
    private String Id;
    private String Email;
    private boolean Authenticated;
    private float idEmpresa;
    private boolean Authorized;
    private String Numero_Documento;
    private String Correo_Electronico;


    // Getter Methods

    public String getLogin() {
        return Login;
    }

    public String getName() {
        return Name;
    }

    public String getLastName() {
        return LastName;
    }

    public String getId() {
        return Id;
    }

    public String getEmail() {
        return Email;
    }

    public boolean getAuthenticated() {
        return Authenticated;
    }

    public float getIdEmpresa() {
        return idEmpresa;
    }

    public boolean getAuthorized() {
        return Authorized;
    }

    public String getNumero_Documento() {
        return Numero_Documento;
    }

    public String getCorreo_Electronico() {
        return Correo_Electronico;
    }

    // Setter Methods

    public void setLogin(String Login) {
        this.Login = Login;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public void setLastName(String LastName) {
        this.LastName = LastName;
    }

    public void setId(String Id) {
        this.Id = Id;
    }

    public void setEmail(String Email) {
        this.Email = Email;
    }

    public void setAuthenticated(boolean Authenticated) {
        this.Authenticated = Authenticated;
    }

    public void setIdEmpresa(float idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public void setAuthorized(boolean Authorized) {
        this.Authorized = Authorized;
    }

    public void setNumero_Documento(String Numero_Documento) {
        this.Numero_Documento = Numero_Documento;
    }

    public void setCorreo_Electronico(String Correo_Electronico) {
        this.Correo_Electronico = Correo_Electronico;
    }
}
