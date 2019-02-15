const express = require('express');
const router = express.Router();
const schemas = require('../models/models');

const Shipp = schemas.Shipp;
const User = schemas.User;
const Notification = schemas.Notification;
const Types = schemas.Types;
const Action = schemas.Action;

router.post('/like', function (request, response) {
    let shipp_id = request.body.shipp_id;
    let user_id = request.body.user_id;

    const error_callback =  function (error) {
        console.log(error);
        response.json({data: false, error: error});
    };
    Action.findOne({type: Types.Like, shipp_id: shipp_id, user_id : user_id}).exec()
    .then((action_exists)=>{
      if(action_exists){
        console.log("ja existe");
        response.json({data: true, error: null});
        return;
      }

      let shipp_find = Shipp.findById(shipp_id);
      let notification_query = Notification.findOne({type: Types.Like, shipp_id: shipp_id});
      let other_action_promise = Action.findOne({type: Types.Unlike, shipp_id: shipp_id, user_id : user_id});
      Promise.all([shipp_find, notification_query, other_action_promise]).then(function (results) {
          let promises = [];
          let shipp = results[0];
          let notif = results[1];
          let other_action = results[2];
          let action = new Action({type: Types.Like, shipp_id: shipp_id, user_id: [user_id]});
          let updates = {};
          updates.$addToSet = { likes :shipp_id, activity : action._id};
          if(other_action){
            updates.$pull = {unlikes : shipp_id, activity:  other_action._id};
            promises.push(Action.remove({_id: other_action._id}));
            promises.push(Shipp.update({_id : shipp_id}, {$addToSet:{ likes: user_id},$pull: {unlikes: user_id}, $inc:{unlikeCount: -1, likeCount: 1}}));
          }else {
            promises.push(Shipp.update({_id: shipp_id}, {$addToSet:{ likes: user_id}, $inc: {'likeCount': 1}}));
          }
          console.log(updates);
          promises.push(action.save());
        promises.push(User.update({_id: user_id}, updates));

          let not_update = notif ? Notification.update({_id: notif._id}, {$addToSet:{related: user_id}}) :
              new Notification({type: Types.Like, users_id:[shipp.owner, shipp.user1, shipp.user2], shipp_id: shipp._id, related:[user_id]})
                  .save();
          promises.push(not_update);
          Promise.all(promises).then(function (results) {
             response.json({data: true, error: null})
          }).catch(error_callback);
      }).catch(error_callback);
    });
});

router.post('/dislike', function (request, response) {
    let shipp_id = request.body.shipp_id;
    let user_id = request.body.user_id;
    const error_callback =  function (error) {
        console.log(error);
        response.json({data: false, error: error});
    };
     Action.findOne({type: Types.Dislike, shipp_id: shipp_id, user_id: user_id}).exec().then((action_exists)=>{
       if(action_exists){
          response.json({data: true, error: null});
          return;
      }
      let shipp_find = Shipp.findById(shipp_id);
      let notification_query = Notification.findOne({type: Types.Dislike, shipp_id: shipp_id});
      let other_action_promise = Action.findOne({type: Types.Like, shipp_id: shipp_id, user_id: user_id});
       Promise.all([shipp_find, notification_query, other_action_promise]).then(function (results) {
           let promises = [];
           let shipp = results[0];
           let notif = results[1];
           let other_action = results[2];
           let action = new Action({type: Types.Dislike, shipp_id: shipp_id, user_id: [user_id]});
           let updates = {};
           updates.$addToSet = { unlikes :shipp_id, activity : action._id};
           if(other_action){
             updates.$pull = {likes : shipp_id, activity:  other_action._id};
             promises.push(Action.remove({type: Types.Like, shipp_id: shipp_id, user_id: user_id}));
             promises.push(Shipp.update({_id : shipp_id}, {$addToSet :{ unlikes: user_id}, $pull: { likes: user_id}, $inc: {likeCount: -1, unlikeCount: 1}}));
           }else
            promises.push(Shipp.update({_id: shipp_id}, {$addToSet: {unlikes: shipp_id}, $inc: {'unlikeCount': 1}}));
           console.log(updates);
           promises.push(action.save());
           promises.push(User.update({_id: user_id}, updates));

           let not_update = notif ? Notification.update({_id: notif._id}, {$addToSet:{related: user_id}}) :
               new Notification({type: Types.Unlike, users_id:[shipp.owner, shipp.user1, shipp.user2], shipp_id: shipp._id, related:[user_id]})
                   .save();
           promises.push(not_update);
           Promise.all(promises).then(function (results) {
               response.json({data: true, error: null})
           }).catch(error_callback);
       }).catch(error_callback);
     }).catch(error_callback);
});

router.post('/notlike', function (request, response) {
    let shipp_id = request.body.shipp_id;
    let user_id = request.body.user_id;
    Action.findOne({type: Types.Like, shipp_id: shipp_id, user_id : user_id}).exec()
    .then((action_exists)=>{
      if(action_exists){
     let shipp_promise = Shipp.update({_id: shipp_id}, {$inc: {'likeCount': -1}, $pull:{likes: user_id}});
     let action_remove = Action.findOne({type: Types.Like, user_id: user_id, shipp_id: shipp_id});
    Promise.all([shipp_promise, action_remove]).then(function (results) {
        Action.remove({_id: results[1]._id}).exec();
        User.update({_id: user_id}, {$pull: {'likes': shipp_id, 'activity': results[1]._id}}).exec()
            .then(function(result){
                response.json({data: true, error: null});
            }).catch(function (error) {
            response.json({data: false, error: error});
        });
    }).catch(function (error) {
        response.json({data: false, error: error});
    });
  }else response.json({data: true, error: null});
});
});

router.post('/notdislike', function (request, response) {
    let shipp_id = request.body.shipp_id;
    let user_id = request.body.user_id;
    Action.findOne({type: Types.Dislike, shipp_id: shipp_id, user_id : user_id}).exec()
    .then((action_exists)=>{
      if(action_exists){
    let shipp_promise = Shipp.update({_id: shipp_id}, {$inc: {'unlikeCount': -1}, $pull:{unlikes: user_id}});
    let action_remove = Action.findOne({type: Types.Dislike, user_id: user_id, shipp_id: shipp_id});

    Promise.all([shipp_promise, action_remove]).then(function (results) {
        Action.remove({_id: results[1]._id}).exec();
        User.update({_id: user_id}, {$pull: {'dislikes': shipp_id, 'activity': results[1]._id}}).exec()
            .then(function(result){
            response.json({data: true, error: null});
        }).catch(function (error) {
            response.json({data: false, error: error});
        });
    }).catch(function (error) {
        response.json({data: false, error: error});
    });
}else request.json({data: true, error: false});
});
});

router.post('/isLiked', function (request, response) {
    let shipp_id = request.body.shipp_id;
    let user_id = request.body.user_id;
    User.find({
        _id: user_id,
        likes: {
            $elemMatch: {shipp_id: shipp_id}
        }
    }).select('username').exec(function (err, docs) {
        if (docs) response.json({data: true, error: null});
        else response.json({data: false, err: err});

    })
});

router.post('/isDisliked', function (request, response) {
    let shipp_id = request.body.shipp_id;
    let user_id = request.body.user_id;
    User.find({
        _id: user_id,
        dislikes: {
            $elemMatch: {shipp_id: shipp_id}
        }
    }).select('username').exec(function (err, docs) {
        if (docs) response.json({data: true, error: null});
        else response.json({data: false, err: err});

    })
});
module.exports = router;
