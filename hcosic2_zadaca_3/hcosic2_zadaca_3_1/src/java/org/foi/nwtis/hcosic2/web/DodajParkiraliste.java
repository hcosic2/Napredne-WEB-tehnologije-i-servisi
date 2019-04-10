package org.foi.nwtis.hcosic2.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.hcosic2.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.hcosic2.rest.klijenti.GMKlijent;
import org.foi.nwtis.hcosic2.rest.klijenti.OWMKlijent;
import org.foi.nwtis.hcosic2.web.podaci.Lokacija;
import org.foi.nwtis.hcosic2.web.podaci.MeteoPodaci;
import org.foi.nwtis.hcosic2.web.slusaci.SlusacAplikacije;

/**
 * Korisnički dio u kojem je potrebno unositi pojedinačna parkirališta za koje će
 * se preuzimati metorološki podaci. Prvo se unese naziv parkirališta i adresa,
 * zatim se pokrene akcija koja preuzima njeni geolokacijski podaci putem Google
 * Maps API. Slijedi pokretanje akcije za prikaz geolokacijski podataka. Nakon
 * toga je akcija za spremanje podataka o parkiralištu u tablicu baze podataka
 * (PARKIRALISTA). Zadnja akcija je preuzimanje važećih meteoroloških podataka
 * za parkiralište na bazi njegovih geolokacijskih podataka te njihov prikaz na
 * ekranu korisnika
 * 
 * @author Hrvoje
 */

@WebServlet(name = "DodajParkiraliste", urlPatterns = {"/DodajParkiraliste"})
public class DodajParkiraliste extends HttpServlet {
    
    private Connection veza;
    private Statement statement;
    private Lokacija lokacija = null;
    private String naziv;
    private String adresa;
    private String pogreska;
    

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            naziv = request.getParameter("naziv");
            adresa = request.getParameter("adresa");
            veza = SlusacAplikacije.getConnection();
            statement = this.veza.createStatement();
            if (request.getParameter("geolokacija") != null) {
                dohvatiGeoLokaciju(request, response);
            } else if (request.getParameter("spremi") != null) {
                spremiParkirališteUBazu(request, response);
            } else if (request.getParameter("meteo") != null) {
                ispisiMeteoPodatkeOParkiralistu(request, response);
            }
            veza.close();
        } catch (SQLException ex) {
            Logger.getLogger(DodajParkiraliste.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Funkcija koja dohvaća geo lokaciju za prosljeđenu adresu
     * @param request
     * @param response 
     */
    private void dohvatiGeoLokaciju(HttpServletRequest request, HttpServletResponse response) {
        if (adresa.equals("") || naziv.equals("")) {
            try {
                pogreska = "Obavezno upišite naziv i adresu!";
                request.setAttribute("pogreska", pogreska);
                request.getRequestDispatcher("/index.jsp").forward(request, response);
            } catch (ServletException | IOException ex) {
                Logger.getLogger(DodajParkiraliste.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                GMKlijent gmk = new GMKlijent(SlusacAplikacije.getGmgeokey());
                lokacija = gmk.getGeoLocation(adresa);
                request.setAttribute("lokacija", lokacija.getLatitude() + " " + lokacija.getLongitude());
                request.setAttribute("naziv", naziv);
                request.setAttribute("adresa", adresa);
                request.getRequestDispatcher("/index.jsp").forward(request, response);
            } catch (ServletException | IOException ex) {
                Logger.getLogger(DodajParkiraliste.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Funkcija koja sprema u bazu parkiraliste koje je uneseno
     * @param request
     * @param response 
     */
    private void spremiParkirališteUBazu(HttpServletRequest request, HttpServletResponse response) {
        if (adresa.equals("") || naziv.equals("") || request.getParameter("lokacija").equals("")) {
            try {
                pogreska = "Obavezno upišite naziv i adresu te dohvatite geo lokaciju pritiskom na gumb Geo lokacija!";
                request.setAttribute("pogreska", pogreska);
                request.getRequestDispatcher("/index.jsp").forward(request, response);
            } catch (ServletException | IOException ex) {
                Logger.getLogger(DodajParkiraliste.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                String upit = "INSERT INTO parkiralista(naziv, adresa, latitude, longitude)"
                        + " VALUES('" + naziv + "','" + adresa + "'," + lokacija.getLatitude() + "," + lokacija.getLongitude() + ")";
                statement.execute(upit);
                statement.close();
                request.setAttribute("lokacija", lokacija.getLatitude() + " " + lokacija.getLongitude());
                request.setAttribute("naziv", naziv);
                request.setAttribute("adresa", adresa);
                request.getRequestDispatcher("/index.jsp").forward(request, response);
            } catch (SQLException | ServletException | IOException ex) {
                Logger.getLogger(DodajParkiraliste.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Funkcija koja ispisuje dohvaćene meteo podatke za određeno parkiraliste
     * prema prosljeđenoj geo lokaciji
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException 
     */
    private void ispisiMeteoPodatkeOParkiralistu(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
            if (adresa.equals("") || naziv.equals("") || request.getParameter("lokacija").equals("")) {
                pogreska = "Obavezno upišite naziv i adresu, dohvatite geo lokaciju pritiskom na gumb Geo lokacija!";
                request.setAttribute("pogreska", pogreska);
                request.getRequestDispatcher("/index.jsp").forward(request, response);
            }else{
            OWMKlijent owmk = new OWMKlijent(SlusacAplikacije.getOwmkey());
            MeteoPodaci mp = owmk.getRealTimeWeather(lokacija.getLatitude(), lokacija.getLongitude());
            String meteoPodaci = "Temperatura: " + mp.getTemperatureValue() + mp.getTemperatureUnit() + "<br />";
            meteoPodaci += "Vlaga: " + mp.getHumidityValue() + mp.getHumidityUnit() + "<br />";
            meteoPodaci += "Tlak: " + mp.getPressureValue() + mp.getPressureUnit() + "<br />";
            meteoPodaci += "Minimalna temperatura: " + mp.getTemperatureMin()+  mp.getTemperatureUnit() + "<br />";
            meteoPodaci += "Maksimalna temperatura: " + mp.getTemperatureMax() + mp.getTemperatureUnit() + "<br />";
            meteoPodaci += "Brzina vjetra: " + mp.getWindSpeedValue() + mp.getWindSpeedName() + "<br />";
            meteoPodaci += "Smjer vjetra: " + mp.getWindDirectionValue() + mp.getWindDirectionCode() + "<br />";
            request.setAttribute("meteoPodaci", meteoPodaci);
            request.getRequestDispatcher("/index.jsp").forward(request, response);
            }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
