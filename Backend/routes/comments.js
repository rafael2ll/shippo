const express = require('express');
const router = express.Router();
const schemas = require('../models/models');

const Shipp = schemas.Shipp;
const Comment = schemas.Comment;
const Notification = schemas.Notification;
const Types =schemas.Types;

router.post('/getComments', function (request, response) {
    let id = request.body.shipp_id;
    let user_id = request.body.user_id;
    let page = request.body.page;
    console.log(request.body);
    Comment.find({
        shipp_id: id,
        nested: false
    }).skip(page * 20).sort({created_at: 'desc'}).limit(20).populate('user', 'username photoUri gender')
        .populate({
            path: 'sub_comments',
            model: 'Comment',
            populate: {
                path: 'user',
                select: 'username photoUri gender',
                model: 'User'
            }
        }).exec().then((comments)=>{
          let array = [];
          console.log(comments);
          for(let comment of comments){
              if(comment.likes)
               comment.i_liked = comment.likes.indexOf(user_id) > -1;
            if(comment.dislikes)
               comment.i_disliked = comment.dislikes.indexOf(user_id) > -1;
               array.push(comment);
            }
            console.log(array);
            response.json({data: array, error: null});
        }).catch((error)=>{
          console.log(error);
            response.json({data: null, error: error});
        });
});

router.post('/createComment', function (request, response) {
    let shipp_id = request.body.shipp_id;
    let user_id = request.body.user_id;
    let text = request.body.text;
    let nested = request.body.nested;
    let parent_id = null;
    if (nested) parent_id = request.body.parent_id;
    let comment = new Comment({shipp_id: shipp_id, user: user_id, text: text, nested: nested});
    console.log(request.body);

    let shipp_find = Shipp.findById(shipp_id);
    let notification_query = Notification.findOne({type: Types.Comment, shipp_id: shipp_id});
    let shipp_promise = Shipp.update({_id: shipp_id},
        {
            $inc: {'commentCount': 1}
        });
    let comment_promise = comment.save();
    let action_promise = new schemas.Action({type: schemas.Types.Comment, users_id: [user_id], shipp_id: shipp_id, comment_id: comment._id});
    Promise.all([shipp_promise, comment.save(), action_promise, shipp_find, notification_query]).then(function (results) {
        comment = results[1];
        let notif = results[4];
        let shipp = results[3];
        let nested_promises = [];
        if (nested)
            nested_promises.push(Comment.update({_id: parent_id}, {
                $push: {'sub_comments': comment._id}
            }));

        nested_promises.push(Comment.populate(comment, {
            path: 'user',
            select: 'username gender photoUri'
        }));


        if(notif){
            nested_promises.push(Notification.update({_id: notif._id}, {$addToSet:{related: user_id}}));
        }else{
            nested_promises.push(new Notification({type: Types.Comment, users_id:[shipp.owner, shipp.user1, shipp.user2], shipp_id: shipp._id, related:[user_id]})
                .save());
        }

        Promise.all(nested_promises).then(function(results2){
            response.json({data: comment, error: null});
        }).catch(function (error) {response.json({data: null, error: error});});
    }).catch(function (error) {response.json({data: null, error: error});});
});


//Todo
router.post('/editComment', function (request, response) {
    let item_id = request.body.comment_id;
    let text = request.body.text;
    Comment.findOneAndUpdate({_id: item_id},
        {
            $set: {text: text},
        },{'new' : true}, function (err, comment) {
            response.send(JSON.stringify({data: comment, error: err}));
        });
});


router.post('/like', function (request, response) {
  let comment_id = request.body.comment_id;
  let user_id = request.body.user_id;

  Comment.update({_id: comment_id}, {$inc: {'likeCount': 1}, $addToSet: {'likes': user_id}}).exec()
    .then((success)=>{
    response.json({data:true, error: null});
    }).catch(function (error) {
      response.json({data: false, error: error});
   });
});

router.post('/dislike', function (request, response) {
  let comment_id = request.body.comment_id;
  let user_id = request.body.user_id;

  Comment.update({_id: comment_id}, {$inc: {'unlikeCount': 1}, $addToSet: {'unlikes': user_id}}).exec()
    .then((success)=>{
    response.json({data:true, error: null});
    }).catch(function (error) {
      response.json({data: false, error: error});
   });
});

router.post('/notlike', function (request, response) {
  let shipp_id = request.body.comment_id;
  let user_id = request.body.user_id;

  Comment.update({_id: comment_id}, {$inc: {'likeCount': -1}, $pull: {'likes': user_id}}).exec()
    .then((success)=>{
    response.json({data:true, error: null});
    }).catch(function (error) {
      response.json({data: false, error: error});
    });
});

router.post('/notdislike', function (request, response) {
    let shipp_id = request.body.comment_id;
    let user_id = request.body.user_id;

    Comment.update({_id: comment_id}, {$inc: {'unlikeCount': -1}, $pull: {'unlikes': user_id}}).exec()
    .then((success)=>{
      response.json({data:true, error: null});
    }).catch(function (error) {
        response.json({data: false, error: error});
  });
});

router.post('/removeComment', function (request, response) {
    let item_id = request.body.comment_id;
    let shipp_id = request.body.shipp_id;
    Shipp.update({_id: shipp_id},
        {
            $inc: {'commentCount': -1}
        }).exec();
    Comment.remove({_id: item_id}, function (err) {
        let success = true;
        if (err) success = false;
        response.json({data: success, error: err});
    });
});

module.exports = router;
