package com.hack.he.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hack.he.api.LatLngAPI;
import com.hack.he.api.PlaceAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnInfoWindowClickListener {
    AutoCompleteTextView autocompleteView;
    GoogleMap googleMap;
    MapFragment mapFragment;
    Marker currentLocationMarker;
    public Double destLatitude;
    public Double destLongitude;
    public boolean LAT_LNG_FOR_DEST_TASK_COMPLETE = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        mapView = (MapView)findViewById(R.id.mapID); // Gets the MapView from the XML layout and creates it
  //      mapView.onCreate(savedInstanceState);

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapID);
        //final IncidentsMapAsyncTask asyncTask = new IncidentsMapAsyncTask(MainActivity.this);
        mapFragment.getMapAsync(this);

        autocompleteView = (AutoCompleteTextView) findViewById(R.id.auto_complete_text);
        final PlacesAutoCompleteAdapter autoCompleteAdapter = new PlacesAutoCompleteAdapter(this, R.layout.autocomplete_list_item);
        autocompleteView.setAdapter(autoCompleteAdapter);
        autocompleteView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String placeId = autoCompleteAdapter.findPlaceId(parent.getItemAtPosition(position).toString());
                new LatLngForDestAsyncTask(MainActivity.this).execute(placeId);
            }
        });

    }
    public void finishGettingLatLngForDest()
    {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapID);
        mapFragment.getMapAsync(this);

    }
    public void setLatLng(final Double lat,final Double lng)
    {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapID);
       mapFragment.getMapAsync(this);

        addCircle(new LatLng(lat, lng),1000);
        final float start=1;                              //you need to give starting value of SeekBar
        final float end=10;                 //you need to give end value of SeekBar
        final float discrete = 1;
        SeekBar seek=(SeekBar) findViewById(R.id.seekBar1);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                Toast.makeText(getBaseContext(), "Radius = " + String.valueOf(discrete*5000/1000)+"km", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                float temp = progress;
                float dis = end - start;
                float radius = (start + ((temp / 100) * dis));
                addCircle(new LatLng(lat, lng),radius*500);
            }
        });
        TextView suggestionId = (TextView)findViewById(R.id.suggestionID);
        suggestionId.setText("Do not walk alone on side road\nPreferable to avoid 09:00-12:00");

    }
    public void setLAT_LNG_FOR_DEST_TASK_COMPLETE()
    {
        this.LAT_LNG_FOR_DEST_TASK_COMPLETE = true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if(googleMap==null) {
            googleMap = map;
            googleMap.setMyLocationEnabled(true);
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = lm.getBestProvider(criteria, true);
            Location myLocation = null;
            try {
                myLocation = (Location) lm.getLastKnownLocation(lm.NETWORK_PROVIDER);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            final LatLng currentLocationPoint = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            googleMap.getUiSettings().setZoomGesturesEnabled(true);
            googleMap.setTrafficEnabled(true);

            BitmapDrawable d = (BitmapDrawable) getResources().getDrawable(R.drawable.marker1);
            d.setLevel(1234);
            BitmapDrawable bd = (BitmapDrawable) d.getCurrent();
            Bitmap b = bd.getBitmap();
            Bitmap bhalfsize = Bitmap.createScaledBitmap(b, b.getWidth() / 5, b.getHeight() / 5, false);



            MarkerOptions currentLocationMarkerOptions = new MarkerOptions()
                    .position(currentLocationPoint)
                    .title("CURRENT LOCATION")
                    .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize));
            currentLocationMarkerOptions.draggable(true);
            Marker currentLocationMarker = googleMap.addMarker(currentLocationMarkerOptions);

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocationPoint, 14));

        }
        }
    public void addCircle(final LatLng point,float radius)
    {
        int strokeColor = 0xffff0000;
        int shadeColor = 0x44ff0000;

    googleMap.clear();

    CircleOptions circleOptions = new CircleOptions().center(point).radius(radius).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(2);

        BitmapDrawable d = (BitmapDrawable) getResources().getDrawable(R.drawable.marker1);
        d.setLevel(1234);
        BitmapDrawable bd = (BitmapDrawable) d.getCurrent();
        Bitmap b = bd.getBitmap();
        Bitmap bhalfsize = Bitmap.createScaledBitmap(b, b.getWidth() / 5, b.getHeight() / 5, false);


        MarkerOptions markerOptions = new MarkerOptions()
            .position(point)
            .title("DESTINATION POINT")
                .icon(BitmapDescriptorFactory.fromBitmap(bhalfsize));
    markerOptions.draggable(true);
        CameraPosition pos = CameraPosition.builder().target(point).zoom(14).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));


        googleMap.addMarker(markerOptions);
        googleMap.addCircle(circleOptions);
        addMarker(12.922359, 77.58884, "Snatching while on the move (Accused on bike) from front\n Side Road\n 15:00-18:00");
        addMarker(12.935111, 77.580769, "Snatching by walk & escaping with bike\nOn Side Road\n09:00-12:00");
        addMarker(12.923937, 77.589322, "Snatching by walk & escaping with bike\nIn-front of Victims house\n12:00-15:00");
        addMarker(12.940313, 77.58401, "Snatching while on the move (Accused on bike) from back\nOn Side Road\n15:00-18:00");
        addMarker(12.9854957464885, 77.51337048, "Snatching by walk & escaping with bike\nIn-front of Victims house\n18:00-21:00");
        addMarker(12.9788530863274, 77.53876496, "Snatching while on the move (Accused on bike) from back\nOn Side Road\n12:00-15:00");
        addMarker(12.97647598, 77.53196623, "Snatching by walk & escaping with bike\nIn-front of Victims house\n09:00-12:00");
        addMarker(12.96266831, 77.53833594, "Snatching by walk & escaping with bike\nOther Places\n15:00-18:00");

        googleMap.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
        googleMap.setOnInfoWindowClickListener(this);
    }



    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_LONG).show();
    }

    private void addMarker(double lat, double lon,String snippet) {
        googleMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
                .title("MUGGING INCIDENT")
                .snippet(snippet));
    }
    public void createMarker(double lat,double lng,String incident,String loc,String timeslot)
    {
        final LatLng point = new LatLng(lat,lng);
        final MarkerOptions markerOptions = new MarkerOptions()
                .position(point)
                .title("MUGGING INCIDENT").snippet(incident+"<br>"+loc+"<br>"+timeslot);
        googleMap.addMarker(markerOptions);
    }


}

class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    ArrayList<String> resultList;
    HashMap<String,String> placeIdMap;


    Context mContext;
    int mResource;

    PlaceAPI mPlaceAPI = new PlaceAPI();
    LatLngAPI latLngAPI = new LatLngAPI();


    public PlacesAutoCompleteAdapter(Context context, int resource) {
        super(context, resource);

        mContext = context;
        mResource = resource;
    }

    @Override
    public int getCount() {
        // Last item will be the footer
        return resultList.size();
    }

    @Override
    public String getItem(int position) {
        return resultList.get(position);
    }

    public String findPlaceId(String key)
    {
        for(Map.Entry<String,String> item : placeIdMap.entrySet())
        {
            if(item.getKey().equals(key)) {
                return item.getValue();
            }
        }
        return null;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    resultList = mPlaceAPI.autocomplete(constraint.toString());

                    filterResults.values = resultList;
                    filterResults.count = resultList.size();

                    placeIdMap = mPlaceAPI.getPlaceIdMap();
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };

        return filter;
    }


}
