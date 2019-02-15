const express = require('express');
const router = express.Router();
const schemas = require('../models/models');
const mongoose = require('mongoose');
const uploadThings = require("../google/firebase_upload");

const User = schemas.User;
const Shipp = schemas.Shipp;
const Action = schemas.Action;
const Notification = schemas.Notification;
const Types = schemas.Types;
const multer = uploadThings.Multer;

router.post('/createUser', function (request, response) {
    let user = new User(request.body);
    user.save(function (err, user_saved) {
        if (err) user_saved = null;
        response.json({data: user_saved, error: err});
    });
});

router.post('/setFBasPhoto', function(request, response){
    let user_id = request.body.user_id;
    let photo = request.body.photo;
    User.update({_id: user_id}, {photoUri: photo})
    .exec().then((user)=>{
        response.json({data:true, error: null});
    }).catch((error) =>{
      response.json({data:false, error: error});
    });
});

router.post('/updateBio', function(request, response){
    let user_id = request.body.user_id;
    let bio = request.body.bio;
    User.update({_id: user_id}, {bio: bio})
    .exec().then((user)=>{
        response.json({data:true, error: null});
    }).catch((error) =>{
      response.json({data:false, error: error});
    });
});

router.get('/getSimpleUser/:id?', function (req, res) {
    User.findOne({_id: req.params.id}).select('username bio is_private fb_id country country_name city_name photoUri gender').exec(function (err, user) {
        if (err) user = null;
        res.json({data: user, error: err});
    });
});

router.post('/getUserWithParams', function (request, response) {
    let id = request.params.id;
    let params = request.params.fields;
    User.findOne({_id: id}).select(params).exec(function (err, user) {
        if (err) user = null;
        response.json({data: user, error: err});
    })
});

router.post('/activity', function (request, response) {
    let id = request.body.user_id;
    let my_id = request.body.my_id;
    let page = request.body.page || 0;
    let date = request.body.date || new Date();

    User.findOne({_id: my_id}).exec().then((me)=>{
      User.findOne({_id: id}).select('activity')
          .populate({
          path: 'activity',
          match: {'created_at': {$lte: date}},
          options:{limit: 50, sort:{created_at:-1}},
          model: 'Action',
          populate: [{
              path: 'user_id',
              select: 'username photoUri gender',
              model: 'User'
          }, {
              path: 'shipp_id',
              model: 'Shipp',
              populate: {
                  path: ' owner user1 user2',
                  select: 'username gender photoUri country city_name birth',
                  model: 'User'
              }
          }, {
              path: 'comment_id',
              select: 'username photoUri gender',
              model: 'Comment',
              populate: {
                  path: 'user',
                  select: 'username gender photoUri country city_name birth',
                  model: 'User'
              }
          }]
      }).exec()
          .then(function (user) {
              let activity = [];
             let mod_activity = JSON.parse(JSON.stringify(user.activity));
              for(let act of mod_activity){
                  if(activity.indexOf(act) > -1)continue;
                  if(act.shipp_id){
                         act.shipp_id.i_liked = me.likes.indexOf(act.shipp_id._id) > -1;
                         act.shipp_id.i_disliked = me.dislikes.indexOf(act.shipp_id._id) > -1;
                  }
                  activity.push(act);
              }
              response.json({data: activity, error: null});
          }).catch(function (err) {
          response.json({data: null, error: err});
      });
    });

});

router.post('/activity2', function (request, response) {
    let id = request.body.user_id;
    let my_id = request.body.my_id;
    let page = request.body.page || 0;
    let date = request.body.date || new Date();

    User.findOne({_id: my_id}).select('likes dislikes following').exec().then((me)=>{
      User.findOne({_id: id}).select('activity')
          .populate({
          path: 'activity',
          match: {'created_at': {$lte: date}},
          options:{limit: 50, sort:{created_at:-1}},
          model: 'Action',
          populate: [{
              path: 'user_id',
              select: 'username photoUri gender',
              model: 'User'
          }, {
              path: 'shipp_id',
              model: 'Shipp',
              populate: {
                  path: ' owner user1 user2',
                  select: 'username gender photoUri country city_name birth',
                  model: 'User'
              }
          }, {
              path: 'comment_id',
              select: 'username photoUri gender',
              model: 'Comment',
              populate: {
                  path: 'user',
                  select: 'username gender photoUri country city_name birth',
                  model: 'User'
              }
          }]
      }).exec()
          .then(function (user) {
              let activity = [];
             let mod_activity = JSON.parse(JSON.stringify(user.activity));
              for(let act of mod_activity){
                  if(activity.indexOf(act) > -1)continue;
                  if(act.shipp_id){
                         act.shipp_id.i_liked = me.likes.indexOf(act.shipp_id._id) > -1;
                         act.shipp_id.i_disliked = me.dislikes.indexOf(act.shipp_id._id) > -1;
                  }
                  activity.push(act);
              }
              response.json({data: activity, error: null});
          }).catch(function (err) {
          response.json({data: null, error: err});
      });
    });

});
router.post('findFriends', function (request, response) {
    let user_id = request.body.user_id;
    let fb_ids_string = request.body.fb_ids;
    let fb_ids = fb_ids_string.split(",");

    let error = function (error) {
        response.json({data: null, error: error});
    };

    User.findOne({_id: user_id}).select('city').exec()
        .then(function (user) {
            let aggregate = User.aggregate();
            aggregate.near({
                near: [user.city.location[0], user.city.location[1]],
                distanceField: "dist.calculated", // required
                maxDistance: 100,
                query: {type: "public"},
                includeLocs: "dist.location",
                uniqueDocs: true,
                num: 5
            });
            aggregate.match({fb_id: {$in: fb_ids}});
            aggregate.append({
                $project: {
                    following_me: {$in: [mongoose.Types.ObjectId(user_id), '$followers']},
                    username: 1, gender: 1, photoUri: 1, city_name: 1, birth: 1, country: 1
                }
            });
            aggregate.exec()
                .then(function (results) {

                }).catch(error);
        }).catch(error);
});

router.post('/login', function(request, response){
  let email = request.body.email;
  let password = request.body.password;
  let params = 'username gender  country photoUri  fb_id birth city city_name bio country_name country city_name city following_count follower_count';
    User.findOne({email: email, password: password}).select(params).exec(function (err, user) {
        if (user) response.json({data: user, error: err});
        else response.json({data: null, error: "user does not exist"});
    });
});

router.post('/findByFieldAndValue', function (request, response) {
    let id = request.body.value;
    let field = request.body.child;
    let user_id = request.body.user_id;

    let params = 'username gender  country photoUri  is_private birth city city_name bio country_name country city_name city following_count follower_count';
    if (request.body.params) params = request.body.params;
    if(params.indexOf('password') > -1) response.json({data: null});
    const json = {[field]: id};

    User.findOne({_id: user_id}).select('following').exec()
    .then((me)=>{
      User.findOne(json).select(params).exec(function (err, user) {
          if (err) user = null;
          if(user){
          let user_mod = JSON.parse(JSON.stringify(user));
          user_mod.following_me = me.following.indexOf(user._id) > -1;
          if (user) response.json({data: user_mod, error: err});
        }else response.json({data: null, error: "user does not exist"});
      });
    });
});

router.post('/listUsersByName', function (request, response) {
    let page = request.body.page;
    let text = request.body.text;
    let user_id = request.body.user_id;
    console.log(user_id);
   // User.update({}, {$set:{'pending': mongoose.Types.Array(), 'requests': mongoose.Types.Array()}}, {upsert: false, multi: true}).exec().then((sucess)=>{
    User.aggregate([
        {
            $match:
                {username: {$regex : new RegExp(text, "i")}}
        },
        {
            $project: {
                following_me: {$in: [mongoose.Types.ObjectId(user_id), '$followers']},
                is_pending:  {$in:  [mongoose.Types.ObjectId(user_id), '$pending']},
                username: 1, gender: 1, photoUri: 1, city_name: 1, birth: 1, country: 1, is_private : 1,
            }
        },
        {$limit: 20},
        {$skip: page * 20}
    ], function (err, docs) {
        console.log(docs);
        if (err) response.json({data: null, error: err});
        else response.json({data: docs, error: null});
    });
});

router.post('/location', function (request, response) {
    let lat_lng = [request.body.lat, request.body.lng];
    let user_id = request.body.user_id;
    User.findOneAndUpdate({_id: user_id}, {
        $set: {
            location: lat_lng
        }
    }).select('username photoUri gender location birth  fb_id fcm_token bio follower_count following_count email country is_private city country_name city_name')
        .exec(function (err, user) {
            if (err) {
                user = null;
                console.log(err);
            }
            response.json({data: user, error: err})
        });
});

router.post('/updateFCM', function (request, response) {
    let fcm = request.body.fcm_token;
    let user_id = request.body.user_id;
    User.findOneAndUpdate({_id: user_id}, {
        $set: {
            fcm_token: fcm
        }
    }).select('username photoUri gender fcm_token fb_id location birth bio follower_count following_count email country is_private city country_name city_name')
        .exec(function (err, user) {
            if (err) {
                user = null;
                console.log(err);
            }
            response.json({data: user, error: err})
        });
});
router.post('/updatePrivate', function (request, response) {
    let is_private = request.body.is_private;
    let user_id = request.body.user_id;
    User.update({_id: user_id}, {
        $set: {
            is_private: is_private
        }
    }).exec().then((success)=>{
        response.json({data: true, error: null});
    }).catch((error)=>{
        response.json({data: false, error: error});
    });
});
router.post('/getFeedShipps', function (request, response) {
      let id = request.body.user_id;
      let page = request.body.page || 0;
      let date = request.body.date || Date();

       User.findOne({_id: id}).select('activity following followers likes dislikes').exec()
       .then((me)=>{
            let ids = [];
            ids.push(me._id);
            if(me.following)ids.push(...me.following);
            if(me.followers)ids.push(...me.followers);
            console.log(ids);
           User.find({_id:{$in: ids}})
           .populate({
           path: 'activity',
           model: 'Action',
           match: {'created_at':{ $lte: date}},
           options:{limit: 20,sort: { created_at: -1 }},
           populate: [{
               path: 'user_id',
               select: 'username photoUri gender',
               model: 'User'
           }, {
               path: 'shipp_id',
               model: 'Shipp',
               populate: {
                   path: ' owner user1 user2',
                   select: 'username gender photoUri country city_name birth',
                   model: 'User'
               }
           }, {
               path: 'comment_id',
               select: 'username photoUri gender',
               model: 'Comment',
               populate: {
                   path: 'user',
                   select: 'username gender photoUri country city_name birth',
                   model: 'User'
               }
           }]
       }).exec()
           .then(function (users) {
             let activity = [];
                console.log(users);
               for(let user of users){
                    let mod_activity = JSON.parse(JSON.stringify(user.activity));
                    for(let act of mod_activity){
                        if(activity.indexOf(act) > -1)continue;
                        if(act.shipp_id){
                               if(me.likes)act.shipp_id.i_liked = me.likes.indexOf(act.shipp_id._id) > -1;
                               if(me.unlikes)act.shipp_id.i_disliked = me.dislikes.indexOf(act.shipp_id._id) > -1;
                        }
                        activity.push(act);
                    }
               }
               console.log(activity);
               activity.sort(function(a,b){
                               // Turn your strings into dates, and then subtract them
                               // to get a value that is either negative, positive, or zero.
                               return new Date(b.created_at) - new Date(a.created_at);
                });

               if(page == 0)response.json({data: activity.slice(0, 20), error: null});
                else response.json({data: activity.slice(3, 23), error: null});
           }).catch(function (err) {
           console.log(err);
           response.json({data: null, error: err});
       });
    });
});

router.post('/online', function(request, response){
    let user_id = request.body.user_id;
    User.update({_id: user_id}, {is_online: true}).exec().then((success)=>{
            response.json({data: true, error: null});
    }).catch((error)=>{
            response.json({data:false, error: error});
    });
});

router.post('/unlinkWithFacebook', function(request, response){
    let user_id = request.body.user_id;
    User.update({_id: user_id}, {fb_id: "", fb_token: ""}).exec().then((success)=>{
            response.json({data: true, error: null});
    }).catch((error)=>{
            response.json({data:false, error: error});
    });
});

router.post('/linkWithFacebook', function(request, response){
    let user_id = request.body.user_id;
    let fb_id = request.body.fb_id;
    let fb_token = request.body.fb_token;
    User.update({_id: user_id}, {fb_id: fb_id, fb_token: fb_token}).exec().then((success)=>{
            response.json({data: true, error: null});
    }).catch((error)=>{
            response.json({data:false, error: error});
    });
});
router.post('/offline', function(request, response){
    let user_id = request.body.user_id;
    User.update({_id: user_id}, {is_online: false}).exec().then((success)=>{
            response.json({data: true, error: null});
    }).catch((error)=>{
            response.json({data:false, error: error});
    });
});
router.post('/getFollowers', function (request, response) {
    let finding_id = request.body.finding_id;
    let my_id = request.body.my_id;
    let page = request.body.page;
    User.findOne({_id:  my_id}).select('following requests').exec()
    .then((me)=>{
      User.findOne({_id: finding_id}).slice('followers', [page * 50, 50]).select('followers').populate('followers', 'is_private username gender photoUri country city_name')
          .exec(function (err, user) {
              let followers =  JSON.parse(JSON.stringify(user.followers));
              for(let follow of followers){
                  follow.following_me = me.following.indexOf(follow._id) > -1;
                  follow.is_pending = me.requests.indexOf(follow._id) > -1;
              }
              response.json({data: followers, error: err});
          });
    });
});

router.post('/getFollowing', function (request, response) {
    let finding_id = request.body.finding_id;
    let page = request.body.page;
    let my_id = request.body.my_id;
    User.findOne({_id: my_id}).select('following requests').exec()
    .then((me)=>{
      User.findOne({_id: finding_id}).slice('following', [page * 50, 50]).select('following').populate('following', 'is_private username gender photoUri country city_name')
          .exec(function (err, user) {
            let follows =  JSON.parse(JSON.stringify(user.following));
            for(let follow of follows){
                follow.following_me = me.following.indexOf(follow._id) > -1;
                follow.is_pending = me.requests.indexOf(follow._id) > -1;
            }
              response.json({data: follows, error: err});
          });
    });
});

router.get('/standardUsers', function(request, response){
    let country_name = request.query.country;
    User.find({country_name: country_name}).select('followers following').exec()
    .then((users)=>{
          let promises = []
          users.forEach((u)=>{
            let fer = u.followers.length;
            let fing = u.following.length;
            let promise = User.update({_id : u._id},{ $set:{ follower_count: fer, following_count:fing}});
            promises.push(promise);
          });
          Promise.all(promises).then((success)=>{
            response.json({status: 'ok', count:promises.length});
          }).catch((error)=>{
              response.json({status: 'error', users: users, error: error});
          });
    });
});
router.post('/likes', function (request, response) {
    let user_id = request.body.user_id;
    let page = request.body.page;

    User.findOne({_id: user_id}).slice('likes', [page * 20, 20]).select('likes').populate({
        path: 'likes',
        model: 'Shipp',
        populate: {
            path: ' owner user1 user2',
            select: 'username gender photoUri country city_name birth',
            model: 'User'
        }
    })
        .exec(function (err, user) {
            response.json({data: user.likes, error: err});
        });
});
router.post('/exist', function (request, response) {
    let email = request.body.email;
    User.count({'email': email}, function (err, count) {
        let exists = false;
        if (count > 0) exists = true;
        response.json({data: exists, error: err});

    });
});


router.post('/notifications', function (request, response) {
    let user_id = request.body.user_id;

    Notification.find({users_id:{$in: [user_id]}}).
        populate([{
            path: 'users_id',
            select: 'username photoUri is_private',
            model: 'User'
        },{
            path: 'related',
            select: 'username photoUri',
            model: 'User'
        }, {
            path: 'shipp_id',
            model: 'Shipp',
            populate: {
                path: ' owner user1 user2',
                select: 'username  photoUri',
                model: 'User'
            }
        }]).limit(100).sort({updated_at : -1}).exec()
        .then(function(notifications){
            console.log(notifications.keys());
            response.json({data: notifications, error: null});
        }).catch(function (error) {
            response.json({data: null, error: error});
        });
});
router.get('/fakeData', function (request, response) {
    let fs = require('fs');
    let times = request.query.times;
    let epoch = 1;
    fs.readFile('/media/rafael/Projetos/Web/ShippoBackend/routes/fakeusers.json', 'utf8', function (err, data) {
        if (err) throw err; // we'll not consider error handling for now
        let obj = JSON.parse(data);
        for (let i = 0; i < obj.length * times; i++) {
            let u = new User(obj[i / epoch]);
            u.save(function (err, user) {
                console.log(err);
            });
            if (i / obj.length == epoch + 1) epoch += 1;
        }
    });
});

router.post('/imLucky', function (request, response) {
    let user_id = request.body.user_id;

    User.findOne({_id: user_id}).select('follower_count following_count followers following').exec(function (err, user) {
        let max = user.follower_count + user.following_count;
        let user1 = Math.floor(Math.random() * max);
        let user2 = Math.floor(Math.random() * max);
        let p_users = [];
        console.log(user.follower_count+ "::" + user.following_count + " => "+user1+" :: "+ user2);

       let user1_id = user1 < user.follower_count ? user.followers[user1] : user.following[user1 - user.follower_count];
       let user2_id = user2 < user.follower_count ? user.followers[user2] : user.following[user2 - user.follower_count];
       p_users.push(User.findOne({_id: user1_id}).select('username photoUri birth city_name country gender'));
       p_users.push(User.findOne({_id: user2_id}).select('username photoUri birth city_name country gender'));
        Promise.all(p_users).then((results)=>{
               console.log(results);

               response.json({data: results, error: null});
        }).catch((error)=>{
                console.log(error);
                response.json({data: null, error: error});
        });
    });
});

router.post('/search', function (request, response) {
    let finding_id = request.body.user_id;
    let text = request.body.text;
    let page = request.body.page;
    let results = {};
    User.findOne({_id: finding_id}).select('followers following requests location').exec(function (err, user) {
        let following_ids = user.following;
        let followers_ids = user.followers;

        let promise_following = User.find({
            _id: {$in: following_ids},
            username: {$regex: text, $options: "i"}/*,
            location:{
                $near: user.location,
            }*/
        }).sort({'username': 1}).select('username photoUri is_private country city_name birth gender bio');

        let promise_follower = User.find({
            _id: {$in: followers_ids},
            username: {$regex: text, $options: "i"},
            location: {
                $near: user.location,
            }
        }).sort({'username': 1}).select('username photoUri is_private country city_name birth gender bio');
        let promise_other = User.aggregate([
            {
                $match:
                    {username: {$regex: text}}
            },
            {
                $project: {
                    following_me: {$in: [mongoose.Types.ObjectId(finding_id), '$followers']},
                    is_pending : {$in: [mongoose.Types.ObjectId(finding_id), '$pending']},
                    username: 1, gender: 1, photoUri: 1, city_name: 1, birth: 1, country: 1,is_private:1, distance: 1
                }
            },
            {$limit: 20},
            {$skip: page * 20}
        ]).sort({'username': 1});

        Promise.all([promise_following, promise_follower, promise_other]).then(function (results) {
            let array0 = JSON.parse(JSON.stringify(results[0]));
            let array1 = JSON.parse(JSON.stringify(results[0]));

            for(let u of array0){
              u.following_me = user.following.indexOf(u._id) > -1;
              u.is_pending = user.requests.indexOf(u._id) > -1;
            }
            for(let u of array1){
              u.following_me = user.following.indexOf(u._id) > -1;
              u.is_pending = user.requests.indexOf(u._id) > -1;

            }
            let users_array = [array0, array1, results[2]];
            response.json({data: users_array, error: 'null'});
        }).catch(function (error) {
            console.log(error);
            response.json({data: null, error: error});
        });
    });
});

router.post('/uploadPhoto', multer.single('file'), (request, response) => {
    console.log('Upload Image');
    let file = request.file;
    let user_id = request.body.user_id;

    if (file) {
     uploadThings.uploadPromise(file).then((success) => {
            let im_path = success.data;
            User.update({_id: user_id}, {photoUri : im_path})
            .exec()
            .then((user) => {
                   response.json({data:true, error: null});
            }).catch((error) =>{
                console.error(error);
                response.json({data:false, error: error});
            });
     });
    }
});

function sendError(response, error) {
    response.json({data: null, error: error});
};

module.exports = router;
