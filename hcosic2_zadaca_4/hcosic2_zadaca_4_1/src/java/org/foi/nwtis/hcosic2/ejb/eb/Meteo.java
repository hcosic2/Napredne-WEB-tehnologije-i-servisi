/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.hcosic2.ejb.eb;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Klasa entiteta Meteo koja koja definira upite
 * @author Hrvoje
 */
@Entity
@Table(name = "METEO")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Meteo.findAll", query = "SELECT m FROM Meteo m")
    , @NamedQuery(name = "Meteo.findByIdmeteo", query = "SELECT m FROM Meteo m WHERE m.idmeteo = :idmeteo")
    , @NamedQuery(name = "Meteo.findByAdresastanice", query = "SELECT m FROM Meteo m WHERE m.adresastanice = :adresastanice")
    , @NamedQuery(name = "Meteo.findByLatitude", query = "SELECT m FROM Meteo m WHERE m.latitude = :latitude")
    , @NamedQuery(name = "Meteo.findByLongitude", query = "SELECT m FROM Meteo m WHERE m.longitude = :longitude")
    , @NamedQuery(name = "Meteo.findByVrijeme", query = "SELECT m FROM Meteo m WHERE m.vrijeme = :vrijeme")
    , @NamedQuery(name = "Meteo.findByVrijemeopis", query = "SELECT m FROM Meteo m WHERE m.vrijemeopis = :vrijemeopis")
    , @NamedQuery(name = "Meteo.findByTemp", query = "SELECT m FROM Meteo m WHERE m.temp = :temp")
    , @NamedQuery(name = "Meteo.findByTempmin", query = "SELECT m FROM Meteo m WHERE m.tempmin = :tempmin")
    , @NamedQuery(name = "Meteo.findByTempmax", query = "SELECT m FROM Meteo m WHERE m.tempmax = :tempmax")
    , @NamedQuery(name = "Meteo.findByVlaga", query = "SELECT m FROM Meteo m WHERE m.vlaga = :vlaga")
    , @NamedQuery(name = "Meteo.findByTlak", query = "SELECT m FROM Meteo m WHERE m.tlak = :tlak")
    , @NamedQuery(name = "Meteo.findByVjetar", query = "SELECT m FROM Meteo m WHERE m.vjetar = :vjetar")
    , @NamedQuery(name = "Meteo.findByVjetarsmjer", query = "SELECT m FROM Meteo m WHERE m.vjetarsmjer = :vjetarsmjer")
    , @NamedQuery(name = "Meteo.findByPreuzeto", query = "SELECT m FROM Meteo m WHERE m.preuzeto = :preuzeto")})
public class Meteo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "IDMETEO")
    private Integer idmeteo;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "ADRESASTANICE")
    private String adresastanice;
    @Basic(optional = false)
    @NotNull
    @Column(name = "LATITUDE")
    private float latitude;
    @Basic(optional = false)
    @NotNull
    @Column(name = "LONGITUDE")
    private float longitude;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 25)
    @Column(name = "VRIJEME")
    private String vrijeme;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 25)
    @Column(name = "VRIJEMEOPIS")
    private String vrijemeopis;
    @Basic(optional = false)
    @NotNull
    @Column(name = "TEMP")
    private double temp;
    @Basic(optional = false)
    @NotNull
    @Column(name = "TEMPMIN")
    private double tempmin;
    @Basic(optional = false)
    @NotNull
    @Column(name = "TEMPMAX")
    private double tempmax;
    @Basic(optional = false)
    @NotNull
    @Column(name = "VLAGA")
    private double vlaga;
    @Basic(optional = false)
    @NotNull
    @Column(name = "TLAK")
    private double tlak;
    @Basic(optional = false)
    @NotNull
    @Column(name = "VJETAR")
    private double vjetar;
    @Basic(optional = false)
    @NotNull
    @Column(name = "VJETARSMJER")
    private double vjetarsmjer;
    @Basic(optional = false)
    @NotNull
    @Column(name = "PREUZETO")
    @Temporal(TemporalType.TIMESTAMP)
    private Date preuzeto;
    @JoinColumn(name = "ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Parkiralista id;

    public Meteo() {
    }

    public Meteo(Integer idmeteo) {
        this.idmeteo = idmeteo;
    }

    public Meteo(Integer idmeteo, String adresastanice, float latitude, float longitude, String vrijeme, String vrijemeopis, double temp, double tempmin, double tempmax, double vlaga, double tlak, double vjetar, double vjetarsmjer, Date preuzeto) {
        this.idmeteo = idmeteo;
        this.adresastanice = adresastanice;
        this.latitude = latitude;
        this.longitude = longitude;
        this.vrijeme = vrijeme;
        this.vrijemeopis = vrijemeopis;
        this.temp = temp;
        this.tempmin = tempmin;
        this.tempmax = tempmax;
        this.vlaga = vlaga;
        this.tlak = tlak;
        this.vjetar = vjetar;
        this.vjetarsmjer = vjetarsmjer;
        this.preuzeto = preuzeto;
    }

    public Integer getIdmeteo() {
        return idmeteo;
    }

    public void setIdmeteo(Integer idmeteo) {
        this.idmeteo = idmeteo;
    }

    public String getAdresastanice() {
        return adresastanice;
    }

    public void setAdresastanice(String adresastanice) {
        this.adresastanice = adresastanice;
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

    public String getVrijeme() {
        return vrijeme;
    }

    public void setVrijeme(String vrijeme) {
        this.vrijeme = vrijeme;
    }

    public String getVrijemeopis() {
        return vrijemeopis;
    }

    public void setVrijemeopis(String vrijemeopis) {
        this.vrijemeopis = vrijemeopis;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getTempmin() {
        return tempmin;
    }

    public void setTempmin(double tempmin) {
        this.tempmin = tempmin;
    }

    public double getTempmax() {
        return tempmax;
    }

    public void setTempmax(double tempmax) {
        this.tempmax = tempmax;
    }

    public double getVlaga() {
        return vlaga;
    }

    public void setVlaga(double vlaga) {
        this.vlaga = vlaga;
    }

    public double getTlak() {
        return tlak;
    }

    public void setTlak(double tlak) {
        this.tlak = tlak;
    }

    public double getVjetar() {
        return vjetar;
    }

    public void setVjetar(double vjetar) {
        this.vjetar = vjetar;
    }

    public double getVjetarsmjer() {
        return vjetarsmjer;
    }

    public void setVjetarsmjer(double vjetarsmjer) {
        this.vjetarsmjer = vjetarsmjer;
    }

    public Date getPreuzeto() {
        return preuzeto;
    }

    public void setPreuzeto(Date preuzeto) {
        this.preuzeto = preuzeto;
    }

    public Parkiralista getId() {
        return id;
    }

    public void setId(Parkiralista id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idmeteo != null ? idmeteo.hashCode() : 0);
        return hash;
    }

    /**
     * Funckija za usporedbu objekta s prosljeđenim objektom
     * @param object objekt koji treba usporediti
     * @return 
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Meteo)) {
            return false;
        }
        Meteo other = (Meteo) object;
        if ((this.idmeteo == null && other.idmeteo != null) || (this.idmeteo != null && !this.idmeteo.equals(other.idmeteo))) {
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
        return "org.foi.nwtis.hcosic2.ejb.eb.Meteo[ idmeteo=" + idmeteo + " ]";
    }
    
}
