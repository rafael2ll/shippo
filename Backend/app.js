const express = require('express');
const path = require('path');
const favicon = require('serve-favicon');
const logger = require('morgan');
const cookieParser = require('cookie-parser');
const bodyParser = require('body-parser');
const lessMiddleware = require('less-middleware');
const fs = require('fs');
const index = require('./routes/index');
const users = require('./routes/users');
const shipps = require('./routes/shipps');
const follows = require('./routes/follows');
const location = require('./routes/location');
const comments = require('./routes/comments');
const likes = require('./routes/likes');
const chats = require('./routes/chat');
const mongoose = require('mongoose');


const options = {server: {socketOptions: {keepAlive: 1}}};
// mongoose.connect('mongodb://localhost/shippo', options);
mongoose.connect("mongodb://rafael_backend:avestruz@ds123658.mlab.com:23658/shippo", options);
const app = express();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'jade');

// uncomment after placing your favicon in /public
// app.use(favicon(path.join(__dirname, 'public', 'favicon.ico')));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(lessMiddleware(path.join(__dirname, 'public')));
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', function (req, res, next) {
    console.log("\n\n------------NEW REQUEST-----------\n\n");
    console.log(req.body);
    next();
});
app.use('/', index);
app.get('/privacy_policy.html', function(req, res){
  fs.readFile(__dirname + '/privacy_policy.html', 'utf8', function(err, text){
     res.send(text);
 });
});
app.use('/user', users);
app.use('/chat', chats);
app.use('/shipp', shipps);
app.use('/like', likes);
app.use('/comment', comments);
app.use('/follow', follows);
app.use('/location', location);
// catch 404 and forward to error handler
app.use(function(req, res, next) {
    let err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});



module.exports = app;
