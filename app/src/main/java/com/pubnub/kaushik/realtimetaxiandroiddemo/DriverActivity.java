package com.pubnub.kaushik.realtimetaxiandroiddemo;

import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.kaushik.realtimetaxiandroiddemo.util.Constants;

import java.util.LinkedHashMap;

public class DriverActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;

    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setSmallestDisplacement(10);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        sendUpdatedLocationMessage();
    }

    /*
        This method gets user's current location and publishes message to channel.
     */
    private void sendUpdatedLocationMessage() {
        try {
            mFusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    Location location = locationResult.getLastLocation();
                    LinkedHashMap<String, String> message = getNewLocationMessage(location.getLatitude(), location.getLongitude());
                    MainActivity.pubnub.publish()
                            .message(message)
                            .channel(Constants.PUBNUB_CHANNEL_NAME)
                            .async(new PNCallback<PNPublishResult>() {
                                @Override
                                public void onResponse(PNPublishResult result, PNStatus status) {
                                    // handle publish result, status always present, result if successful
                                    // status.isError() to see if error happened
                                    if (!status.isError()) {
                                        System.out.println("pub timetoken: " + result.getTimetoken());
                                    }
                                    System.out.println("pub status code: " + status.getStatusCode());
                                }
                            });
                }
            }, Looper.myLooper());

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /*
        Helper method that takes in latitude and longitude as parameter and returns a LinkedHashMap representing this data.
        This LinkedHashMap will be the message published by driver.
     */
    private LinkedHashMap<String, String> getNewLocationMessage(double lat, double lng) {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("lat", String.valueOf(lat));
        map.put("lng", String.valueOf(lng));
        return map;
    }
}

