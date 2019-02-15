const express = require('express');
const router = express.Router();
const models = require("../models/models");

const Country = models.Country;
const City = models.City;
const User = models.User;

router.post('/cities', function(request, response){
  let text = request.body.text;
  let country = request.body.country;
  console.log(country);
  let loc = [];
  loc[0] = 0;
  loc[1] = 0;
  if(request.body.lat){
        loc[0] = request.body.lat;
        loc[1] = request.body.lng;
  }
  console.log(loc);
    query  = {name:{$regex : text},
           'country_code': country};
    if(request.body.lat)  query.location = {$near: loc};
    console.log(query);
      City.find(query).limit(20).populate('country', 'name').exec(function (err, docs) {
             console.log(docs);
                if(err) response.json({data: null, error: err});
                else response.json({data: docs, error: null});
            });
});

router.post('/city', function(request, response){

  let loc = [];
  loc[0] = 0;
  loc[1] = 0;
  if(request.body.lat){
        loc[0] = request.body.lat;
        loc[1] = request.body.lng;
  }
  City.findOne({location:{
                $near: loc
             }
             }).populate('country', 'name').exec(function (err, docs) {
             console.log(docs);
                if(err) response.json({data: null, error: err});
                else response.json({data: docs, error: null});
            });
});
module.exports = router;
