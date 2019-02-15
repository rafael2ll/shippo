const express = require('express');
const router = express.Router();
const schemas = require('../models/models');
const gcm = require('node-gcm');
const uploadThings = require("../google/firebase_upload");


// Set up the sender with your GCM/FCM API key (declare this once for multiple messages)
const sender = new gcm.Sender('AAAA_UUclZE:APA91bFX6xXKs3DLtnPLF2Em5VPAo5ZbIndzbvH5OQkNLPiODCuaJ8j6ddBvYB_kiOVxQxYUahHib9aLh6UozTvzeOnlypd-cTeY-xrU0vkcIHVJXS9zCluRA4JiyIMBTUTbAElUn5vQ');

const multer = uploadThings.Multer;

const Chat = schemas.Chat;
const Message = schemas.Message;
const User = schemas.User;

//Chat functions

router.post('/create', function (request, response) {
    let users_id = request.body.users_id.split(",");
    let chat = new Chat({users: users_id});
    console.log(request.body);
    console.log(users_id);
    Chat.findOne({users:{ $all: users_id}}).populate({ path: 'users', select: 'username photoUri', model: 'User'}).exec().then((chat_ex)=>{
        if(chat_ex){
            console.log('chat_ex: '+chat_ex);
            response.json({data: chat_ex, error: null});
        }
        else{
        chat.save(function (err, chat_saved) {
        console.log(chat_saved);
        if (err) response.json({data: false, error: err});
        else {
            chat_saved.populate({ path: 'users', select: 'username photoUri', model: 'User'}, function (sub_error) {
                User.update({
                    $id: {$in: users_id}
                }, {
                    $addToSet: {chats: chat_saved._id}
                }).exec(function (error, done) {
                    if (err) response.json({data: false, error: error});
                    else response.json({data: chat_saved, error: null});
                });
            });
        }
    });
    }
});
});

router.post('/list', function (request, response) {
    let user_id = request.body.user_id;
    Chat.find({users: {$in: [user_id]}}).populate('users', 'username photoUri is_online').exec(function (e, chats) {
        response.json({data: chats, error: e});
    });

});
router.post('/update', function (request, response) {
    let chat_id = request.body.chat_id;
    Chat.find({_id: chat_id}).populate('users', 'username photoUri is_online').exec(function (e, chats) {
        response.json({data: chats, error: e});
    });

});
router.post('/delete', function (request, response) {
    let user_id = request.body.user_id;
    let chat_id = request.body.chat_id;


    Chat.remove({_id: chat_id, users: {$in: [user_id]}}).exec().then((success)=>{
         Message.remove({chat_id: chat_id}).exec().then((success)=>{
             response.json({data: true, error: null});
             }).catch((error)=>{
                 response.json({data: false, error: error});
             });
    }).catch((error)=>{
        response.json({data: false, error: error});
    });

});

router.post('/messages', function (request, response) {
    let user_id = request.body.user_id;
    let conversation_id = request.body.conversation_id;
    let message_id = request.body.start_at;
    Message.find({chat_id: conversation_id, status: 'unread', owner: {$not: user_id}}).where('_id').populate('owner', 'username photoUri ').exec(function (e, messages) {
        response.json({data: messages, error: e});
    });

});

//Message functions

router.post('/createMessage', function (request, response) {
    let owner = request.body.owner;
    let body = request.body.text;
    let chat_id = request.body.chat_id;

    let message = new Message({owner: owner, chat_id: chat_id, body: body});
    message.seen_by.push(owner);
    message.content_type = "TEXT";
    message.save(function (err, msg) {
        if (err) {
            response.json({data: null, error: err});
            return;
        }
        msg.populate('owner', 'username photoUri', function(populated){
        Chat.findOne({_id: chat_id}).populate('users', 'fcm_token').exec(function (err, chat) {
            let users_tokens = [];
            chat.users.forEach(function (user){
                if(owner != user._id)users_tokens.push(user.fcm_token);
            });

            let gcm_message = new gcm.Message({
                'data': {
                    'message': JSON.stringify(msg),
                    'type': 'NEW_MESSAGE'
                }
            });
            sender.send(gcm_message, {registrationTokens: users_tokens}, function (err, res) {
                response.json({data: msg, error: err});
            })
        });
        });
    });
});


router.post('/newAudioMessage', multer.single('file'), (request, response) => {
    console.log('Upload Audio');
    let file = request.file;
    let owner = request.body.owner;
    let chat_id = request.body.chat_id;
    let metadata = request.body.metadata;

    if (file) {
        uploadThings.uploadPromise(file).then((success) => {
            let message = new Message({owner: owner, chat_id: chat_id});
            message.seen_by.push(owner);
            message.content_type = "AUDIO";
            message.body = success.data;
            message.metadata = metadata;
            message.save(function (err, msg) {
                if (err) {
                    response.json({data: null, error: err});
                    return;
                }
                msg.populate('owner', 'username photoUri', function(populated){
                    Chat.findOne({_id: chat_id}).populate('users', 'fcm_token').exec(function (err, chat) {
                        let users_tokens = [];
                        chat.users.forEach(function (user){
                            if(owner != user._id)users_tokens.push(user.fcm_token);
                        });

                        let gcm_message = new gcm.Message({
                            'data': {
                                'message': JSON.stringify(msg),
                                'type': 'NEW_MESSAGE'
                            }
                        });
                        sender.send(gcm_message, {registrationTokens: users_tokens}, function (err, res) {
                            console.log(res);
                            response.json({data: msg, error: err});
                        })
                    });
                });
            });
        }).catch((error) => {
            request.send({data: null, error: error});
        });
    }
});
router.post('/newImageMessage', multer.single('file'), (request, response) => {
    console.log('Upload Image');
    let file = request.file;
    let owner = request.body.owner;
    let chat_id = request.body.chat_id;
    let metadata = request.body.metadata;

    if (file) {
        uploadThings.uploadPromise(file).then((success) => {
            let message = new Message({owner: owner, chat_id: chat_id});
            message.seen_by.push(owner);
            message.content_type = "PICTURE";
            message.body = success.data;
            message.metadata = metadata;
            message.save(function (err, msg) {
                if (err) {
                    response.json({data: null, error: err});
                    return;
                }
                msg.populate('owner', 'username photoUri', function(populated){
                    Chat.findOne({_id: chat_id}).populate('users', 'fcm_token').exec(function (err, chat) {
                        let users_tokens = [];
                        chat.users.forEach(function (user){
                            if(owner != user._id)users_tokens.push(user.fcm_token);
                        });

                        let gcm_message = new gcm.Message({
                            'data': {
                                'message': JSON.stringify(msg),
                                'type': 'NEW_MESSAGE'
                            }
                        });
                        sender.send(gcm_message, {registrationTokens: users_tokens}, function (err, res) {
                            console.log(msg);
                            response.json({data: msg, error: err});
                        });
                    });
                });
            });
        }).catch((error) => {
            console.log(error);
            response.json({data: null, error: error});
        });
    }
});

router.post('/messageSeen', function (request, response) {
    let user_id = request.body.user_id;
    let chat_id = request.body.chat_id;
    let message_id = request.body.message_id;
    console.log(request.body);
    Message.findOneAndUpdate({_id: message_id},
        {
            $push: {
                seen_by: user_id
            },
            status: 'read'
        }, {'new' : true}).populate('owner', 'username photoUri').exec(function (err, msg) {
        if (err) {
            response.json({data: null, error: err});
            return;
        }
        Chat.findOne({_id: chat_id}).populate('users', 'fcm_token').limit(1).exec(function (err, chat) {
            let users_tokens = [];
             chat.users.forEach(function (user){
                  if(user_id != user._id)users_tokens.push(user.fcm_token);
             });
            console.log(users_tokens);
            let gcm_message = new gcm.Message({
                data: {
                    message_id: message_id,
                    read_by: user_id,
                    type: 'MESSAGE_READ'
                }
            });
            sender.send(gcm_message, {registrationTokens: users_tokens}, function (err, res) {
                console.log(msg);
                response.json({data: msg, error: err});
            })
        });
    });
});


module.exports = router;
