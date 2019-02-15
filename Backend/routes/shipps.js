const express = require('express');
const router = express.Router();
const schemas = require('../models/models');

const Shipp = schemas.Shipp;
const User = schemas.User;
const Types = schemas.Types;
const Action = schemas.Action;
const Notfication = schemas.Notification;
const Comment = schemas.Comment;

router.post('/create', function (request, response) {
    let owner = request.body.owner;
    let user1 = request.body.user1;
    let user2 = request.body.user2;
    let lat = request.body.lat;
    let lng = request.body.lng;
    let label = request.body.label;
    let country0 = request.body.country_0;
    let country1 = request.body.country_1;
    let country2 = request.body.country_2;
    let usernames = request.body.usernames;
    let shipp = new Shipp();
    shipp.owner = owner;
    shipp.user1 = user1;
    shipp.user2 = user2;
    shipp.label = label;
    shipp.usernames= usernames;
    shipp.location = [lat, lng];
    shipp.country = [country0, country1, country2];

    shipp.save(function (err, shipp_saved) {
        if (err) {
            console.log(err);
            response.json({data: null, error: err});
        }
        else {
            let notification = new schemas.Notification({
                type: Types.Shipp,
                users_id: [user1, user2],
                shipp_id: shipp_saved._id
            });

            let act1 = new Action({type: Types.Shipp, user_id:[owner, user1, user2], shipp_id: shipp_saved._id});
            let usersCommomUpdate = User.update({_id: [owner, user1, user2]}, {
                $push: {'shipps': shipp_saved._id}
            });
            Promise.all([act1.save(), usersCommomUpdate, notification.save()])
                .then(function (actions) {
                    let up1 = User.update({_id: user1}, {$addToSet: {'activity': actions[0]._id}});
                    let up2 = User.update({_id: user2}, {$addToSet: {'activity': actions[0]._id}});
                    let upOwner = User.update({_id: owner}, {$addToSet: {'activity': actions[0]._id}});

                    Promise.all([up1, up2, upOwner]).then(function (shipp_done) {
                        response.json({data: true, error: null});
                    }).catch(function (error) {
                        response.json({data: false, error: error});

                    });
                }).catch(function (error) {
                response.json({data: null, error: error});
            });
        }
    });
});

router.post('/delete', function(request, response){
    let shipp_id = request.body.shipp_id;
    let user_id = request.body.user_id;
    Shipp.findOne({_id: shipp_id}).exec()
    .then((shipp)=>{
        if(user_id == shipp.owner || user_id == shipp.user1 || user_id == shipp.user2){
          let comment_remove = Comment.remove({shipp_id});
          let action_remove = Action.remove({shipp_id: shipp_id});
          let notification_remove =schemas.Notification.remove({shipp_id: shipp_id});
          let shipp_remove = Shipp.remove({_id: shipp_id});
          Promise.all([comment_remove, shipp_remove, action_remove, notification_remove]).then((success)=>{
              response.json({data: true, error: null});
          }).catch((error)=>{
              response.json({data: false, error: error});
          });
        }
    });
});
router.post('/share', function(request, response){
    let shipp_id = request.body.shipp_id;
    let user_id = request.body.user_id;
    let act = new Action({type: Types.Share, user_id:[user_id], shipp_id: shipp_id});
    act.save().then((success)=>{
      User.update({_id: user_id}, {$addToSet: {'activity': act._id}}).then((succ)=>{
                 response.json({data: true, error: null});
        }).catch((error)=>{
                      response.json({data: false, error: error});
              });
    }).catch((error)=>{
            response.json({data: false, error: error});
    });
});
router.get('/getShipp/:id?', function (req, res) {
    Shipp.findOne({_id: req.params.id}).select('user1 user2 owner likeCount unlikeCount commentCount')
        .populate('user1').populate('user2')
        .populate('owner').exec(function (err, user) {
        if (err) user = null;
        res.send(JSON.stringify({data: user, error: err}));
    });
});


router.post('/findUserShipps', function (request, response) {
    let page = request.body.page;
    let user_id = request.body.user_id;
    let locale = request.body.locale;
    Shipp.find({}).or([{owner: user_id}, {user1: user_id}, {user2: user_id}]).limit(20).sort({created_at: -1}).select('user1 user2 owner likeCount unlikeCount commentCount created_at label')
        .populate('user1', 'username gender birth photoUri city_name country').populate('user2', 'username gender birth photoUri city_name country')
        .populate('owner', 'username gender birth photoUri').skip(page * 20)
        .exec(function (err, shipps) {
            if (err) shipps = [];
            response.send(JSON.stringify({data: shipps, error: err}));
        });
});

router.post('/find', function (request, response) {
    let page = request.body.page;
    let user_id = request.body.user_id;
    let username = request.body.usernames.toLowerCase();
    Shipp.find({$text:{$search: username}}).limit(20).sort({likeCount: -1, created_at: -1}).select('user1 user2 owner likeCount unlikeCount commentCount created_at label')
        .populate('user1', 'username gender birth photoUri city_name country').populate('user2', 'username gender birth photoUri city_name country')
        .populate('owner', 'username gender birth photoUri').skip(page * 20)
        .exec(function (err, shipps) {
            if (err) shipps = [];
            response.send(JSON.stringify({data: shipps, error: err}));
        });
});

router.post('/trending', function (request, response) {
    let page = request.body.page || 0;
    let user_id = request.body.user_id;
    let country_id = request.body.country;
    let city = request.body.city;
    let lat = request.body.lat;
    let lng = request.body.lng;

    let onlyFriends = request.body.my_friends || false;
    let query;

    User.findOne({_id: user_id}).select('followers following likes dislikes').exec()
        .then(function (user) {
            let ids = [];
            ids.push(...user.following);
            ids.push(...user.followers);
            if (onlyFriends) {
              console.log("only friends");
                query = Shipp.find({$or: [{owner: {$in: ids}}, {user1: {$in: ids}}, {user2: {$in: ids}}]});
            } else if (country_id) {
              console.log("country");
                query = Shipp.find({country: {$in: [country_id]}}).sort({likeCount: -1}).limit(20).skip(page * 20);
            } else if(lat){
              console.log("location");
                query = Shipp.find({location:{ $near:[lat, lng], $maxDistance: 500}});
            }else {
              console.log("normal");
                query = Shipp.find().sort({likeCount: -1});
            }
            query.limit(20).sort({likeCount: -1}).skip(page*20);
            query.select('user1 user2 owner likeCount unlikeCount commentCount created_at label');
            query.populate("user1 user2 owner", 'username gender photoUri city_name city birth');
            query.exec().then(function (results) {
                let docs = JSON.parse(JSON.stringify(results));
                for (let shipp of docs) {
                    shipp.i_liked = user.likes.indexOf(shipp._id) > -1;
                    shipp.i_disliked = user.dislikes.indexOf(shipp._id) > -1;
                }
                response.json({data: docs, error: null});
            }).catch(function (error) {
                response.json({data: null, error: error})
            });
        });
});


module.exports = router;
