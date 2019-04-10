
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="resources/css/osnovna.css" type="text/css"/>
        <title>Dodavanje parkiralista</title>
    </head>
    <body>
        <h1>Dodavanje parkirališta</h1>
        <form method="POST" 
              action="${pageContext.servletContext.contextPath}/DodajParkiraliste">
            <table>
                <tr>
                    <td>Naziv i adresa: 
                        <input name="naziv" value="${naziv}" placeholder="Upiši naziv"/>
                    </td>
                    <td><input name="adresa" value="${adresa}" placeholder="Upiši adresu"/></td>
                    <td><input type="submit" name="geolokacija" value="Geo lokacija"/></td>
                </tr>
                <tr>
                    <td colspan="2">Geo lokacija
                        <input name="lokacija" value="${lokacija}" readonly="readonly" size="60"/> 
                    </td>
                    <td><input type="submit" name="spremi" value="Spremi"/></td>
                  </tr>
                <tr>
                    <td colspan="2"></td>
                    <td><input type="submit" name="meteo" value="Meteo podaci"/></td>
                </tr>
            </table>
            <div>
                ${meteoPodaci}
                ${pogreska}
            </div>
        </form>
    </body>
</html>