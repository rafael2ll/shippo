package com.ft.shippo.models;

import android.content.Context;
import android.location.Location;

import com.android.volley.Request;
import com.android.volley.error.VolleyError;
import com.ft.shippo.utils.Requester;

import org.json.JSONObject;

/**
 * Created by rafael on 31/01/18.
 */

public class City extends BaseObject {
    private String name;
    private String country_name;
    private Country country;
    private double location[] = new double[2];

    public City(String name, String country_name) {
        this.name = name;
        this.country_name = country_name;
    }

    public static void findCities(Context ctx, String text, String country, Location currentLocation, ListObjectListener<City> listObjectListener) {
        try {
            JSONObject object = new JSONObject()
                    .put("text", text)
                    .put("country", country)
                    .put("limit", 10);
            if (currentLocation != null) {
                object.put("lng", currentLocation.getLongitude())
                        .put("lat", currentLocation.getLatitude());
            }
            Request request = Requester.createPostRequest("/location/cities", object.toString(), listObjectListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            listObjectListener.onError(new VolleyError(e.getMessage()));
        }
    }

    public static void findCity(Context ctx, Location currentLocation, ObjectListener<City> listObjectListener) {
        try {
            JSONObject object = new JSONObject();
            if (currentLocation != null) {
                object.put("lng", currentLocation.getLongitude())
                        .put("lat", currentLocation.getLatitude());
            }
            Request request = Requester.createPostRequest("/location/city", object.toString(), listObjectListener);
            Requester.getInstance(ctx).addToRequestQueue(request);
        } catch (Exception e) {
            listObjectListener.onError(new VolleyError(e.getMessage()));
        }
    }

    public String getName() {
        return name;
    }

    public String getCountryName() {
        return country_name;
    }

    public double[] getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return getName();
    }

    public Country getCountry() {
        return country;
    }
}
