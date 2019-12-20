package com.rcn.pat.ViewModels;

public class UserViewModel {
    private int id ;
    private String name ;
    private String userName ;
    private int roleId ;
    private String roleName ;
    private String adminTypeElementId;

    public String getAdminTypeElementId() {
        return adminTypeElementId;
    }

    public void setAdminTypeElementId(String adminTypeElementId) {
        adminTypeElementId = adminTypeElementId;
    }

    public UserViewModel() {
    }

    public UserViewModel(int id, String name, String userName, int roleId, String roleName) {
        this.id = id;
        this.name = name;
        this.userName = userName;
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
