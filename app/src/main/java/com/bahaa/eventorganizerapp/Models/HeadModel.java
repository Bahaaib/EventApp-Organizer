package com.bahaa.eventorganizerapp.Models;

import java.io.Serializable;

public class HeadModel implements Serializable {

    private String name;

    private String phone;

    private String privilege;

    private Boolean status;

    private String key;

    public HeadModel() {
        //Required Empty Constructor
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPrivilege() {
        return privilege;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
