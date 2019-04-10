package org.foi.nwtis.hcosic2.rest.serveri;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.foi.nwtis.hcosic2.rest.klijenti.GMKlijent;
import org.foi.nwtis.hcosic2.web.podaci.Lokacija;
import org.foi.nwtis.hcosic2.web.podaci.Parkiraliste;
import org.foi.nwtis.hcosic2.web.slusaci.SlusacAplikacije;

/**
 * Rest servis koji prima zahtjeve i na temelju zahtjeva izvršava sljedeće radnje:
 * vraća popis svih parkirališta, njihovih adesa i geo lokacija,
 * dodaje parkirališta u bazu podataka, za izabrano parkiralište ažurira podatke,
 * na bazi putanje {id} briše izabrano parkiralište.
 * @author Hrvoje
 */


@Path("meteo")
public class MeteoREST {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of MeteoREST
     */
    public MeteoREST() {
    }

    /**
     * Retrieves representation of an instance of org.foi.nwtis.hcosic2.rest.serveri.MeteoREST
     * @return an instance of java.lang.String
     */
    
    /**
     * Funkcija koja vraća sve podatke iz tablice parkiralista u bazi podataka
     * @return vraća odgovor oke u json formatu
     * @throws SQLException 
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson() throws SQLException {
        Connection veza = SlusacAplikacije.getConnection();
        Statement statement = veza.createStatement();
        String upit = "SELECT * FROM parkiralista";
        ResultSet rezultat = statement.executeQuery(upit);
        JsonArrayBuilder jab = Json.createArrayBuilder();
        while(rezultat.next()){
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("id", rezultat.getString("ID"));
            job.add("naziv",rezultat.getString("NAZIV"));
            job.add("adresa",rezultat.getString("ADRESA"));
            job.add("latitude",rezultat.getString("LATITUDE"));
            job.add("longitude",rezultat.getString("LONGITUDE"));
            jab.add(job);
        }
        return odgovor(jab.build().toString(), "OK", " ") ;
    }
    
    /**
     * Funkcija dodaje novo parkiralište u bazu podataka u tabliuc parkiralista
     * prije nego doda novi zapis provjerava postoji li taj zapis već u bazi
     * @param podaci prima podatke u json formatu naziv i adresu prema kojima 
     * dohvaća latitude i longitude te sve zajedno sprema u bazu
     * @return 
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String postJson(String podaci) {
        try {
            Connection veza = SlusacAplikacije.getConnection();
            Statement statement = veza.createStatement();
            JsonReader reader = Json.createReader(new StringReader(podaci));
            JsonObject jo = reader.readObject();
            GMKlijent gmk = new GMKlijent(SlusacAplikacije.getGmgeokey());
            Lokacija lok = gmk.getGeoLocation(jo.getString("adresa"));
            String naziv = jo.getString("naziv");
            String adresa = jo.getString("adresa");
            String upit = "SELECT * FROM parkiralista WHERE naziv='"+naziv+"'";
            ResultSet rezultat = statement.executeQuery(upit);
            if(rezultat.next()){
                return odgovor("[]", "ERR", "Parkiraliste vec postoji u bazi!");
            }
            String upit2 = "INSERT INTO parkiralista values(default,'"+naziv+"','"+adresa+"',"+lok.getLatitude()+","+lok.getLongitude()+")";
            statement.executeUpdate(upit2);
            return odgovor("[]", "OK", " ");
        } catch (SQLException ex) {
            Logger.getLogger(MeteoREST.class.getName()).log(Level.SEVERE, null, ex);
            return odgovor("[]", "ERR", "Parkiraliste vec postoji u bazi!");
        }
    }
    
    /**
     * Funkcija koja vraća poruku o grešci da tu funkciju nije dozvoljeno pozvati
     * @param podaci prima podatke u json obliku
     * @return vraća grešku
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String putJson(String podaci) {
        return odgovor("[]", "ERR", "Nije dozvoljeno!");
    }
    
    /**
     * Funkcija koja vraća poruku o grešci da tu funkciju nije dozvoljeno pozvati
     * @return vraća grešku
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String deleteJson() {
        return odgovor("[]", "ERR", "Nije dozvoljeno!");
    }
    
    /**
     * Funkcija koja vraća podatke o parkiralištima za prosljeđeni id u json obliku
     * @param id prima id parkiralista
     * @return vraća poruku o grešci ukoliko parkiraliste ne postoji u bazi ili
     * vraća podatke o parkiralistima u json obliku ukoliko parkiralište postoji u bazi
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public String getJson(@PathParam("id") String id) {
        try {
            Connection veza = SlusacAplikacije.getConnection();
            Statement statement = veza.createStatement();
            String upit = "SELECT * FROM parkiralista WHERE id="+id;
            ResultSet rezultat = statement.executeQuery(upit);
            if(rezultat.next()){
                JsonObjectBuilder job = Json.createObjectBuilder();
                job.add("id", rezultat.getString("ID"));
                job.add("naziv",rezultat.getString("NAZIV"));
                job.add("adresa",rezultat.getString("ADRESA"));
                job.add("latitude",rezultat.getString("LATITUDE"));
                job.add("longitude",rezultat.getString("LONGITUDE"));
                return odgovor(job.build().toString(), "OK", " ") ;
            }
            return odgovor("[]", "ERR", "Trazeno parkiraliste ne postoji!");
        } catch (SQLException ex) {
            Logger.getLogger(MeteoREST.class.getName()).log(Level.SEVERE, null, ex);
            return odgovor("[]", "ERR", "SQL exception");
        }
    }
    
    /**
     * Funkcija koja vraća poruku o grešci da tu funkciju nije dozvoljeno pozvati
     * @param podaci prima podatke u json obliku
     * @return vraća grešku
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public String postJson(@PathParam("id") String id, String podaci) {
        return odgovor("[]", "ERR", "Nije dozvoljeno!");
    }

    /**
     * Funkcija koja update podatke o parkiralištu ukoliko to parkiralište postoji 
     * u bazi
     * @param id id parkirališta koje se update-a
     * @param podaci podaci koji će se unjeti odnosno update-ati u bazu
     * @return vraća oke ukoliko se parkiraliste update-alo u bazi ili
     * grešku ukoliko parkiraliste ne postoji u bazi
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public String putJson(@PathParam("id") String id, String podaci) {
        try {
            Connection veza = SlusacAplikacije.getConnection();
            Statement statement = veza.createStatement();
            JsonReader reader = Json.createReader(new StringReader(podaci));
            JsonObject jo = reader.readObject();
            GMKlijent gmk = new GMKlijent(SlusacAplikacije.getGmgeokey());
            Lokacija lok = gmk.getGeoLocation(jo.getString("adresa"));
            String upit = "SELECT * FROM parkiralista where id="+id;
            ResultSet rezultat = statement.executeQuery(upit);
            if(rezultat.next()){
                String upit2 = "UPDATE parkiralista SET naziv='"+jo.getString("naziv")+"', adresa='"+jo.getString("adresa")+
                        "', latitude="+lok.getLatitude()+", longitude="+lok.getLongitude()+" WHERE id="+id;
                statement.executeUpdate(upit2);
                return odgovor("[]", "OK", " ");
            }else
                return odgovor("[]", "ERR", "Parkiraliste ne postoji u bazi!");
            
        } catch (SQLException ex) {
            Logger.getLogger(MeteoREST.class.getName()).log(Level.SEVERE, null, ex);
            return odgovor("[]", "ERR", "SQL exception!");
        }
    }
    
    /**
     * Funkcija koja briše određeni zapis iz tablice parkiralište prije toga
     * mora obrisati podatke za navedeno parkiralište u tablici meteo jer je 
     * tablica parkiralište odnosnu roditelj-dijete s tablicom meteo
     * @param id prima id parkiralista koje je potrebno obrisati
     * @return vraća poruku ok ukoliko je parkiraliste obrisano ili gresku
     * ukoliko navedeno parkiraliste ne postoji u bazi podataka
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public String deleteJson(@PathParam("id") String id) {
        try {
            Connection veza = SlusacAplikacije.getConnection();
            Statement statement = veza.createStatement();
            String upit = "DELETE FROM meteo WHERE id="+id;
            statement.execute(upit);
            Connection veza2 = SlusacAplikacije.getConnection();
            Statement statement2 = veza2.createStatement();
            String upit2 = "DELETE FROM parkiralista WHERE id="+id;
            statement2.execute(upit2);
            System.out.println(upit);
            return odgovor("[]", "OK", " ");
        } catch (SQLException ex) {
            Logger.getLogger(MeteoREST.class.getName()).log(Level.SEVERE, null, ex);
            return odgovor("[]", "OK", "Parkiraliste ne postoji!");
        }
    }
    
    /**
     * Funkcija koja pravi json format za poruku koju funkcije vraćaju
     * @param odgovor odgovor koji poruka sadrži
     * @param vrstaPoruke vrsta pourke ok ili err
     * @param poruka poruka koja se ispisuje
     * @return vraća poruku u json obliku
     */
    public String odgovor(String odgovor, String vrstaPoruke, String poruka){
        JsonObjectBuilder job = Json.createObjectBuilder();
        if(vrstaPoruke == "OK"){
            job.add("odgovor", odgovor);
            job.add("status", vrstaPoruke);
            return job.build().toString();
        }else{
            job.add("odgovor", odgovor);
            job.add("status", vrstaPoruke);
            job.add("poruka", poruka);
            return job.build().toString();
        }
            
    }
}