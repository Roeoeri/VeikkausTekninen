# Kooditehtävä

## Käyttöohjeet

### Pelitilin ajaminen
Pelitiliä pääsee testikäyttämään osoitteesta: https://pelitilidemo.herokuapp.com/. API-kuvaus löytyy [täältä](https://github.com/Roeoeri/VeikkausTekninen/blob/main/Apidokumentaatio.md). Jos pelitiliä haluaa ajaa lokaalisti, voi toimia näin:

1. Jos sinulla ei ole Mavenia, asenna se (https://maven.apache.org)
2. Navigoi komentorivillä kansioon, johon haluat pelitilin ladata.
3. Aja komento: `git clone git@github.com:Roeoeri/VeikkausTekninen.git`
4. Aja komento: `cd VeikkausTekninen/Pelitili` 
5. Aja komento: `mvn clean compile exec:java`
6. Pelitili on käytettävissä osoitteesta: http://localhost:3000
7. API-kuvaus löytyy [täältä](https://github.com/Roeoeri/VeikkausTekninen/blob/main/Apidokumentaatio.md)

### Testien ajaminen

Testit ajetaan automaattisesti, kun tähän repositorioon tehdään muutoksia. Jos haluat ajaa ne lokaalisti: 

1. Seuraa ylempiä ohjeita kohtaan 4
2. Aja komento: `mvn test`


