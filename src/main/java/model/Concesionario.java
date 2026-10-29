package model;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "concesionario-de-coches")
@XmlAccessorType(XmlAccessType.FIELD)
public class Concesionario {

    @XmlElementWrapper(name = "listado-coches")
    @XmlElement(name = "coche")
    private List<Coche> coches;

    public Concesionario() {
        this.coches = new ArrayList<>();
    }


    public List<Coche> getCoches() {
        return coches;
    }

    public void setCoches(List<Coche> coches) {
        this.coches = coches;
    }


    @Override
    public String toString() {
        return "Concesionario{" +
                "coches=" + coches +
                '}';
    }


    public boolean existeMatricula(String matricula){
        for (Coche c : this.coches){
            if (c.getMatricula() != null && c.getMatricula().equalsIgnoreCase(matricula)){
                return true;
            }
        }
        return false;
    }
}
