package com.ft.shippo;

import com.android.volley.error.VolleyError;
import com.ft.shippo.models.ObjectListener;
import com.ft.shippo.models.User;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by rafael on 07/02/18.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        if (User.getCurrentUser() != null) {
            User.getCurrentUser().setFCMToken(getApplicationContext(), FirebaseInstanceId.getInstance().getToken(), new ObjectListener<User>(User.class) {
                @Override
                public void onResult(User data, String error) {
                    data.saveLocally();
                }

                @Override
                public void onError(VolleyError error) {

                }
            });
        }
    }
}
