package com.home;

/**
 * Created by Dds on 13.05.2016.
 */
public class Param {

    private String name;
    private String translation;
    private String type;

    public Param () {
        this.name = "";
        this.translation = "";
        this.type = "";
    }

    public Param (String name, String translation, String type) {
        this.name = name;
        this.translation = translation;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
