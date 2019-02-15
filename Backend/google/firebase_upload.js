const googleStorage = require('@google-cloud/storage');
const Multer = require('multer');
const path = require("path");
const admin = require("firebase-admin");
const format = require('util').format;
const serviceAccount = require("./shippo_admin_key");

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    storageBucket: "shippo-fe85f.appspot.com"
});

const bucket = admin.storage().bucket("shippo-fe85f.appspot.com");

const multer = Multer({
    storage: Multer.memoryStorage(),
    limits: {
        fileSize: 5 * 1024 * 1024 // no larger than 5mb, you can change as needed.
    }
});


function uploadImageToStorage(file){
    let prom = new Promise((resolve, reject) => {
        if (!file) {
            reject('No image file');
        }
        let newFileName = `${file.originalname}`;

        let fileUpload = bucket.file(newFileName);

        const blobStream = fileUpload.createWriteStream({
            metadata: {
                contentType: file.mimetype
            }
        });

        blobStream.on('error', (error) => {
            reject('Something is wrong! Unable to upload at the moment.'+error);
        });

        blobStream.on('finish', () => {
            // The public URL can be used to directly access the file via HTTP.
            fileUpload.makePublic().then(function (success) {
                const url = format(`https://storage.googleapis.com/${bucket.name}/${fileUpload.name}`);
                resolve({data: url, error: null});
            }).catch(function (err) {
                resolve({data: null, error: err});
            });
        });

        blobStream.end(file.buffer);
    });
    return prom;
}

module.exports.Multer =multer;
module.exports.uploadPromise= uploadImageToStorage;
