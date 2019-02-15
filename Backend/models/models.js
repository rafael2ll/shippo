const express = require('express');
const router = express.Router();
const mongoose = require('mongoose');
const mongoosePaginate = require('mongoose-paginate');
const gcm = require('node-gcm');

// Set up the sender with your GCM/FCM API key (declare this once for multiple messages)
const sender = new gcm.Sender('AAAA_UUclZE:APA91bFX6xXKs3DLtnPLF2Em5VPAo5ZbIndzbvH5OQkNLPiODCuaJ8j6ddBvYB_kiOVxQxYUahHib9aLh6UozTvzeOnlypd-cTeY-xrU0vkcIHVJXS9zCluRA4JiyIMBTUTbAElUn5vQ');

const Schema = mongoose.Schema;

const commentSchema = Schema({
    shipp_id :{type: Schema.Types.ObjectId, ref: 'Shipp', index: true},
    nested: {type: Boolean, default: false},
    user: {type: Schema.Types.ObjectId, ref: 'User'},
    text: Schema.Types.String,
    sub_comments: [{type: Schema.Types.ObjectId, ref: 'Comment', select: false}],
    likeCount: {type: Number, default: 0},
    unlikeCount: {type: Number, default: 0},
    likes: [{type:Schema.ObjectId, ref:'User'}],
    unlikes: [{type:Schema.ObjectId, ref:'User'}],
},  {timestamps: {createdAt: 'created_at'}});


const shippSchema = Schema({
    owner: {type: Schema.Types.ObjectId, ref: 'User', index:  true},
    user1: {type: Schema.Types.ObjectId, ref: 'User', index: true},
    user2: {type: Schema.Types.ObjectId, ref: 'User', index: true},
    label: String,
    usernames: {type: String , lowercase: true, select: false},
    location: [{type: Number, index: '2d', select: false}],
    country: [{type: Schema.ObjectId, ref: 'Country', sparse: true}],
    likeCount: {type: Number, default: 0},
    unlikeCount: {type: Number, default: 0},
    likes: [{type: Schema.ObjectId, ref: 'User'}],
    unlikes: [{type: Schema.ObjectId, ref: 'User'}],
    commentCount: {type: Number, default: 0},
}, {timestamps: {createdAt: 'created_at'}});

const actionSchema = Schema({
    type: Number,
    shipp_id : {type: Schema.ObjectId, ref: 'Shipp'},
    user_id: [{type: Schema.ObjectId, ref:'User'}],
    comment_id: {type: Schema.ObjectId, ref:'Comment'}
}, {timestamps:{createdAt: 'created_at'}});

const notificationSchema = Schema({
    type: Number,
    shipp_id : {type: Schema.ObjectId, ref: 'Shipp'},
    users_id: [{type: Schema.ObjectId, ref:'User'}],
    comment_id: {type: Schema.ObjectId, ref:'Comment'},
    related: [{type: Schema.ObjectId, ref:'User'}]
}, {timestamps: {createdAt: 'created_at'}});

const followSchema = Schema({
    user: {type: Schema.Types.ObjectId, ref: 'User'},
}, {timestamps: {createdAt: 'created_at'}});

const likeSchema= Schema({
    shipp_id: {type: Schema.Types.ObjectId, ref: 'Shipp'},
}, {timestamps: {createdAt: 'created_at'}});

const userScheme = Schema({
        username: {type: String, index:true, required: true},
        bio: String,
        birth: String,
        gender: String,
        is_private: {type: Boolean, default: false},
        email: {type: String, unique: true},
        fb_token: String,
        is_online: {type: Boolean, default: false},
        fb_id: {type: String, unique: false, sparse: true},
        fcm_token: String,
        password:  { type: String, select: false },
        photoUri: String,
        city_name: String,
        country_name: String,
        city: {type: Schema.Types.ObjectId, ref: 'City', sparse: true},
        country: {type: Schema.Types.ObjectId, ref: 'Country'},
        location:{type: [Number], index : '2d'},
        shipps : [{type: Schema.Types.ObjectId, ref : 'Shipp'}],
        activity: [{type: Schema.Types.ObjectId, ref: 'Action'}],
        following_count : {type: Number, default: 0},
        follower_count : {type: Number, default: 0},
        requests: [{type: Schema.ObjectId,ref:'User'}],
        pending: [{type: Schema.ObjectId, ref:'User'}],
        following: [{type: Schema.Types.ObjectId, ref: 'User'}],
        followers: [{type: Schema.Types.ObjectId, ref: 'User'}],
        action:[{type: Schema.Types.ObjectId, ref:'Action'}],
        likes: [{type: Schema.Types.ObjectId, ref: 'Shipp'}],
        dislikes: [{type: Schema.Types.ObjectId, ref: 'Shipp'}],
        favorites: [{type: Schema.Types.ObjectId, ref: 'Shipp'}],
        blocked: [{type: Schema.Types.ObjectId, ref: 'User'}]}
    , {timestamps: {createdAt: 'created_at'}});

const countrySchema = new Schema({
        name: String,
        code: String,
        phone: [Number],
        continent: String,
        capital: String,
        currency: String
});

const citySchema = new Schema({
    name: {type: String, index: true},
    country_code: String,
    country: {type: Schema.ObjectId, ref: 'Country'},
    location: {type: [Number], index:'2d'},
});

const chatSchema = new Schema({
    users: [{type: Schema.Types.ObjectId, ref:'User'}]
},{timestamps:{createdAt: 'created_at'}});

const messageSchema = new Schema({
        owner: {type: Schema.Types.ObjectId, ref: 'User'},
        seen_by: [{type: Schema.Types.ObjectId, ref: 'User'}],
        chat_id:{type: Schema.Types.ObjectId, ref:'Chat', index: true},
        metadata: String,
        body: String,
        status :{type: String, default: 'unread'},
        content_type: String,
},{timestamps:{createdAt: 'created_at'}});

citySchema.index({name: 1}, {unique:true});
countrySchema.index({name: 1}, {unique: true});
shippSchema.index({usernames: 'text'});
commentSchema.plugin(mongoosePaginate);
shippSchema.plugin(mongoosePaginate);
userScheme.plugin(mongoosePaginate);

userScheme.methods.findNear = function(cb) {
    return this.model('User').find({geo: { $nearSphere: this.geo, $maxDistance: 0.01} }, cb);
};


shippSchema.statics.findById = function(id){
    return this.model('Shipp').findOne({_id: id});
};

notificationSchema.virtual('total_count').get(function () {
    return this.related.length;
});

notificationSchema.virtual('latest').get(function () {
   return this.related.slice(Math.max(this.related.length -4, 0));
});

notificationSchema.set('toJSON', {virtuals: true});
 function handleNotification(doc) {
    Notification.populate(doc,[{
        path: 'users_id',
        select: 'username photoUri',
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
    }], function (err, notification) {
        let users_tokens = [];
        console.log("notification: "+ notification);
        notification.users_id.forEach(function (user) {
            users_tokens.push(user.fcm_token);
        });
        let gcm_message = new gcm.Message({
            'data': {
                'notification': JSON.stringify(notification),
                'type': 'NOTIFICATION'
            }
        });
        sender.send(gcm_message, {registrationTokens: users_tokens}, function (error, res) {
            console.log(res);
        });
    });
};
notificationSchema.post('save', function (doc) {
    handleNotification(doc);
});


const Action = mongoose.model('Action', actionSchema);
const Notification = mongoose.model('Notification', notificationSchema);
const Country = mongoose.model('Country', countrySchema);
const City = mongoose.model('City', citySchema);
const User = mongoose.model('User', userScheme);
const Shipp = mongoose.model('Shipp', shippSchema);
const Comment = mongoose.model('Comment', commentSchema);
const Following = mongoose.model('Following', followSchema);
const Follower = mongoose.model('Followers', followSchema);
const Like = mongoose.model('Like', likeSchema);
const Chat = mongoose.model('Chat', chatSchema);
const Message = mongoose.model('Message', messageSchema);

const Types = {
    'Shipp' : 0, //new shipp with you
    'Like' : 1, // you liked something
    'Dislike': 2, // you disliked something
    'Comment' : 3, // you commmented something
    'Follow' : 4, // you start following someone
    'Follower' : 5, // someone start to follow you
    'RequestFollow' : 6,
    'FollowAccept' : 7,
    'Share' : 8
};
const schemas ={'Types': Types, 'Notification': Notification, 'User': User, 'Shipp': Shipp, 'Comment': Comment,
                'Follower': Follower, 'Following': Following, 'Like': Like,'Chat': Chat, 'Message': Message,  'Country' : Country, 'City': City, 'Action' : Action};

module.exports = schemas;
