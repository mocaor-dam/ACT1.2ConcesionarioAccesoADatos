package model;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class Equipamiento {
    //Lo marcamos como un valor
    @XmlValue
    private String nombre;

    public Equipamiento() {}

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "Equipamiento{" +
                "extras=" + nombre +
                '}';
    }
}
