const express = require('express');
const router = express.Router();
const models = require("../models/models");

const Following = models.Following;
const Follower = models.Follower;
const User = models.User;

router.post('/startFollow', function (request, response) {
    let user_id = request.body.user_id;
    let follow_id = request.body.follow_id;
    let notification = new models.Notification({type: models.Types.Follower, users_id:[follow_id], related:[ user_id]});
    Promise.all([
        notification.save(),
        User.update({_id: user_id}, {
            $addToSet: {'following': follow_id},
            $inc: {'following_count': 1}
        }),
        User.update({_id: follow_id}, {
                $addToSet: {'followers': user_id},
                $inc: {'follower_count': 1}
            })
    ]).then(function (results) {
        response.json({'data': true, 'error': null});
    }).catch(function (error) {
        console.log(error);
        response.json({'data': false, 'error': error});
    });
});

router.post('/acceptRequest', function (request, response) {
    let user_id = request.body.user_id;
    let follow_id = request.body.accept_id;
    let notification = new models.Notification({type: models.Types.FollowAccept, users_id:[follow_id], related:[user_id]});
    Promise.all([
        notification.save(),
        models.Notification.remove({type: models.Types.RequestFollow, users_id:[follow_id], related:[user_id]}),
        User.update({_id: user_id}, {
            $pull : {'requests' : follow_id},
            $addToSet: {'following': follow_id},
            $inc: {'following_count': 1}
        }),
        User.update({_id: follow_id}, {
            $pull: {'pending': user_id},
            $addToSet: {'followers': user_id},
            $inc: {'follower_count': 1}
        })
    ]).then(function (results) {
        response.json({'data': true, 'error': null});
    }).catch(function (error) {
        console.log(error);
        response.json({'data': false, 'error': error});
    });
});

router.post('/requestFollow', function (request, response) {
    let user_id = request.body.user_id;
    let follow_id = request.body.follow_id;
    let notification = new models.Notification({type: models.Types.RequestFollow, users_id:[follow_id], related:[ user_id]});
    Promise.all([
        notification.save(),
        User.update({_id: user_id}, {
            $addToSet: {'requests': follow_id}}),
        User.update({_id: follow_id}, {
                $addToSet: {'pending': user_id}})
    ]).then(function (results) {
        response.json({'data': true, 'error': null});
    }).catch(function (error) {
        console.log(error);
        response.json({'data': false, 'error': error});
    });
});

router.post('/stopFollow', function (request, response) {
    let user_id = request.body.user_id;
    let follow_id = request.body.follow_id;

    Promise.all([
        User.update({_id: user_id},
        {
            $pull: {following: follow_id},
            $inc: {'following_count': -1}
        }),
        User.update({_id: follow_id},
                {
                    $pull: {followers: user_id},
                    $inc: {'follower_count': -1}
                })
    ]).then(function (results) {
        response.json({'data': true, 'error': null});
    }).catch(function (error) {
        response.json({'data': false, 'error': final_err});
    });
});

module.exports = router;
