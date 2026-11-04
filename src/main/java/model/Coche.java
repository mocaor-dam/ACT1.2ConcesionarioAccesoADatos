package model;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class Coche {
    //Atributos
    //Le ponemos que el nombre del elemento en el fichero Xml va a ser "matricula-coche"
    @XmlElement(name = "matricula-coche")
    private String matricula;

    private String marca;

    private String modelo;

    //Lo marcamos como atributo
    @XmlAttribute
    private int id;

    //Le ponemos que el nombre de la Lista va a ser "equipamiento", y el nombre de cada elemento como "extra"
    @XmlElementWrapper(name = "equipamiento")
    @XmlElement(name = "extra")
    private List<Equipamiento> equipamientos;

    //Constructor
    public Coche() {}

    //Getters y Setters

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Equipamiento> getEquipamientos() {
        return equipamientos;
    }

    public void setEquipamientos(List<Equipamiento> equipamientos) {
        this.equipamientos = equipamientos;
    }


    //ToString
    @Override
    public String toString() {
        return "Coche{" +
                "matricula='" + matricula + '\'' +
                ", marca='" + marca + '\'' +
                ", modelo='" + modelo + '\'' +
                ", id=" + id +
                ", equipamiento=" + equipamientos +
                '}';
    }
}
