package kr.edcan.material.model;

import android.graphics.Color;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by LNTCS on 2016-08-11.
 */
public class ColorSet extends RealmObject {
    @PrimaryKey
    private int id;
    private String name;
    private String color;
    private int colorRes;

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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color.toUpperCase();
        this.colorRes = Color.parseColor(color);
    }

    public int getColorRes() {
        return colorRes;
    }

    public void setColorRes(int colorRes) {
        this.colorRes = colorRes;
    }
}
