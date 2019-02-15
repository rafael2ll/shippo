const express = require('express');
const router = express.Router();
const fs = require('fs');
const schemas = require('../models/models');
const  path = require('path');
let cities;
const Country = schemas.Country;
const City = schemas.City;
/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'Express' });
});

router.get('/writeCountries', function(request, response){

    fs.readFile(path.join(__dirname, '..', 'countries.json'), 'utf8', function (err, data) {
        if (err) throw err; // we'll not consider error handling for now
        let obj = JSON.parse(data);
        let i =0;
        for(let country_key in obj.countries){
           let country = new Country(obj.countries[country_key]);
           if(obj.countries[country_key].phone.indexOf(',') >= 0){
                country.phone = obj.countries[country_key].phone.split(',');
                console.log(country);
           }
           country.code = country_key;
           country.save(function(err, done){
                if(err) console.log(err.message);
           });
           i++;
        }
        console.log(i);
    });
    response.json({data: 'ok'});
});
router.get('/writeCities', function(request, response){

    fs.readFile(path.join(__dirname, '..','cities.json'), 'utf8', function (err, data) {
        if (err) throw err; // we'll not consider error handling for now
        cities = JSON.parse(data);
        console.log(cities.length);
    /*    for(i; i< cities.length; i++){
            if(cities[i].name == 'Berekua')break;
        }
        i++;
        console.log(cities[i]);*/
       nextCity(0);
    });
});

function nextCity(i){
    let c = cities[i];
    Country.findOne({code:c.country}, function(err, country){
              let city = new City();
              city.name =c.name;
              city.location = [c.lat, c.lng]
              city.country = country._id;
              city.country_code = c.country;
              city.save(function(err, city_saved){
                    nextCity(++i);
                    if(err) console.log(err.message);
              });
           });
}
module.exports = router;
