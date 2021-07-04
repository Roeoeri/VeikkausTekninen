# API Dokumentaatio

## GET /players
(Esim https://pelitilidemo.herokuapp.com/players)


Palauttaa tietokantaan tallennetut pelitilit JSON listana. Endpoint on puhtaasti demoa varten, joten se ei ole polun /api/ takana.

## GET /gameEvents
(Esim https://pelitilidemo.herokuapp.com/gameEvents)


Palauttaa tietokantaan tallennetut pelitapahtumat JSON listana. Endpoint on puhtaasti demoa varten, joten se ei ole polun /api/ takana.

## POST /api/deposit 
(Esim https://pelitilidemo.herokuapp.com/api/deposit)

EndPoint voiton maksamista varten. Olettaa, että sille lähetetyssä pyynnössä on rungossa JSON muotoa:

```
{
    "gameEventId": String,   // Pelitapahtuman tunniste. Merkkijono, korkeintaan 255 merkkiä. Samalla tunnisteella ei saa olla toista tapahtumaa.
    "playerId": String,      // Pelaajan tunniste. Merkkijono, korkeintaan 255 merkkiä. Pelaajalla täytyy olla pelitili.
    "amount": Number         // Summa sentteinä.
    
}    
```

Jos rungossa oleva JSON on oikean muotoinen, endpoint palauttaa koodin 200 ja JSON:nin muota 
```
{
  "balance": Number //Pelaajan pelitilin uusi saldo sentteinä tapahtuman jälkeen.
)
```

Muussa tapauksessa palautetaan [Javalinin](https://javalin.io/documentation) BadRequest, eli koodi 400 ja otsikko, joka sisältää virheviestin.


##  POST /api/charge
(Esim https://pelitilidemo.herokuapp.com/api/charge)

EndPoint veloitusta varten. Olettaa, että sille lähetetyssä pyynnössä on rungossa JSON muotoa:
```
{
    "gameEventId": String,   // Pelitapahtuman tunniste. Merkkijono, korkeintaan 255 merkkiä. Samalla tunnisteella ei saa olla toista tapahtumaa.
    "playerId": String,      // Pelaajan tunniste. Merkkijono, korkeintaan 255 merkkiä. Pelaajalla täytyy olla pelitili.
    "amount": Number         // Summa sentteinä.
    
}    
```

Jos rungossa oleva JSON on oikean muotoinen, endpoint palauttaa koodin 200 ja JSON:nin muota 
```
{
  "balance": Number //Pelaajan pelitilin uusi saldo sentteinä tapahtuman jälkeen.
)
```

Muussa tapauksessa palautetaan [Javalinin](https://javalin.io/documentation) BadRequest, eli koodi 400 ja otsikko, joka sisältää virheviestin.






