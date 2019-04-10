/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.hcosic2.ejb.eb;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Klasa entiteta Parkiralista koja koja definira upite
 * @author Hrvoje
 */
@Entity
@Table(name = "PARKIRALISTA")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Parkiralista.findAll", query = "SELECT p FROM Parkiralista p")
    , @NamedQuery(name = "Parkiralista.findById", query = "SELECT p FROM Parkiralista p WHERE p.id = :id")
    , @NamedQuery(name = "Parkiralista.findByNaziv", query = "SELECT p FROM Parkiralista p WHERE p.naziv = :naziv")
    , @NamedQuery(name = "Parkiralista.findByAdresa", query = "SELECT p FROM Parkiralista p WHERE p.adresa = :adresa")
    , @NamedQuery(name = "Parkiralista.findByLatitude", query = "SELECT p FROM Parkiralista p WHERE p.latitude = :latitude")
    , @NamedQuery(name = "Parkiralista.findByLongitude", query = "SELECT p FROM Parkiralista p WHERE p.longitude = :longitude")})
public class Parkiralista implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 99)
    @Column(name = "NAZIV")
    private String naziv;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "ADRESA")
    private String adresa;
    @Basic(optional = false)
    @NotNull
    @Column(name = "LATITUDE")
    private float latitude;
    @Basic(optional = false)
    @NotNull
    @Column(name = "LONGITUDE")
    private float longitude;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "id")
    private List<Meteo> meteoList;

    public Parkiralista() {
    }

    public Parkiralista(Integer id) {
        this.id = id;
    }

    public Parkiralista(Integer id, String naziv, String adresa, float latitude, float longitude) {
        this.id = id;
        this.naziv = naziv;
        this.adresa = adresa;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    @XmlTransient
    public List<Meteo> getMeteoList() {
        return meteoList;
    }

    public void setMeteoList(List<Meteo> meteoList) {
        this.meteoList = meteoList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    /**
     * Funckija za usporedbu objekta s prosljeđenim objektom
     * @param object objekt koji treba usporediti
     * @return 
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Parkiralista)) {
            return false;
        }
        Parkiralista other = (Parkiralista) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    /**
     * Fukcija koja ispisuje objekt u obliku stringa
     * @return string objekta i njegovo ID-a
     */
    @Override
    public String toString() {
        return "org.foi.nwtis.hcosic2.ejb.eb.Parkiralista[ id=" + id + " ]";
    }
    
}
