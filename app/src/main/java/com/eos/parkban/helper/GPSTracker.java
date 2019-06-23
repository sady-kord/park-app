package com.eos.parkban.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import java.sql.Date;
import java.util.List;

import com.eos.parkban.LoginActivity;
import com.eos.parkban.R;
import com.eos.parkban.dialogs.GPSDialog;

public class GPSTracker implements LocationListener {

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;// 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 15000; // 1000 * 60 * 1; // 1 minute

    private boolean isGPSEnabled = false;
    boolean isNetworkEnabled = false;
    private Location location;
    public static double latitude;
    public static double longitude;
    private Activity context;
    protected LocationManager locationManager;
    ILocationChanged locationChanged;
    private boolean canGetLocation;

    public GPSTracker(Activity context) {
        this.context = context;
        this.locationChanged = locationChanged;
        //getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) context
                    .getSystemService(context.LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled || !isNetworkEnabled) {
                showGpsDialog();
                return null;
            }
            this.canGetLocation = true;

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            100);

                    return null;
                }
            }
            if (location == null) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                if (locationManager != null) {
                    location = locationManager
                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        onLocationChanged(location);

                        if (locationChanged != null)
                            locationChanged.LocationChanged(location);
                    }

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (location == null)
            if (isGPSEnabled) {
                Messenger.shortWarningMessage(context, "لطفا تا دریافت کامل موقعیت شکیبا باشید");
                return null;
            }

        return location;
    }

    public void showGpsDialog() {
        try {
            GPSDialog dialogFragment = new GPSDialog();
            dialogFragment.show((context).getFragmentManager(), "");
        } catch (Exception e) {
            return;
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        boolean fakeLocation = false;

        Log.i("=========>","isMockLocationEnabled" + isMockLocationEnabled(context));
        Log.i("=========>","areThereMockPermissionApps" + areThereMockPermissionApps(context));

        if (location != null) {

            if (isMockLocationEnabled(context)) {

                fakeLocation = true;
//
//                if (Build.VERSION.SDK_INT >= 18)
//                    fakeLocation = isMockLocation(location);
//                else
//                    fakeLocation = isMockSettingsON(context);
            }else
                fakeLocation = false;

            if (fakeLocation) {
                ShowToast.getInstance().showError(context, R.string.fake_location);

                Intent i = new Intent(context, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(i);
            } else {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                if (locationChanged != null)
                    locationChanged.LocationChanged(location);
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
//        getLocation();
    }

    @Override
    public void onProviderEnabled(String provider) {
        getLocation();
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        showGpsDialog();
        latitude = 0.0;
        longitude = 0.0;
    }

    public interface ILocationChanged {
        void LocationChanged(Location location);
    }

    public float getDistanceBetween(Location locationA, Location locationB) {
        return locationA.distanceTo(locationB);
    }

    public Date getDateAndTime(Location location) {
        Date date = null;
        if (location != null) {
            long time = location.getTime();
            //java.util.Date date = DateTimeHelper.parseDate(DateTimeHelper.DateToStr(new Date(time)));
            date = new Date(time);
        }
        return date;
    }

    public static boolean isMockSettingsON(Context context) {
        // returns true if mock location enabled, false if not enabled.
        if (Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"))
            return false;
        else
            return true;
    }

    public boolean isMockLocation(Location location) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && location != null && location.isFromMockProvider();
    }

    public static Boolean isMockLocationEnabled(Context context) {
        return !Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
    }

    public static boolean areThereMockPermissionApps(Context context) {
        int count = 0;

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages =
                pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName,
                        PackageManager.GET_PERMISSIONS);

                // Get Permissions
                String[] requestedPermissions = packageInfo.requestedPermissions;

                if (requestedPermissions != null) {
                    for (int i = 0; i < requestedPermissions.length; i++) {
                        if (requestedPermissions[i]
                                .equals("android.permission.ACCESS_MOCK_LOCATION")
                                && !applicationInfo.packageName.equals(context.getPackageName())) {
                            count++;
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("Got exception " , e.getMessage());
            }
        }

        if (count > 0)
            return true;
        return false;
    }
}