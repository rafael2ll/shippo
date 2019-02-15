package com.ft.shippo;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.ft.shippo.models.User;
import com.mindorks.butterknifelite.ButterKnifeLite;
import com.mindorks.butterknifelite.annotations.BindView;

import org.json.JSONArray;

/**
 * Created by rafael on 25/02/18.
 */

public class SuggestFriendsActivity extends AppCompatActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.recycler);
        ButterKnifeLite.bind(this);
        toolbar.setTitle(R.string.people_suggest);
        GraphRequest request =  GraphRequest.newGraphPathRequest(AccessToken.getCurrentAccessToken(), String.format("/%s/friends", User.getCurrentUser().getFbId()), new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                try {
                   if(response.getError() == null){
                       JSONArray usersArray = response.getJSONObject().getJSONArray("data");
                       Log.d("users", usersArray.toString());

                   }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        request.executeAsync();
    }
}
