package com.ericmatias.centralautobuses;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String matricula , data;
    ArrayList<LatLng> arrayPosiciones;
    ObtenerUbicaciones ou = new ObtenerUbicaciones();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        matricula = getIntent().getStringExtra("matricula");
        Toast.makeText(MapsActivity.this, matricula, Toast.LENGTH_SHORT).show();

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        ou.execute();

    }
    public void pintarLineaMapa(ArrayList<LatLng> ubicaciones) {
        mMap.addPolyline(new PolylineOptions().addAll(ubicaciones).color(Color.RED));
    }

    private class ObtenerUbicaciones extends AsyncTask<Void, Void, Boolean> {

        protected Boolean doInBackground(Void... params) {

            boolean resul;

            HttpClient httpClient = new DefaultHttpClient();

            HttpGet get =
                    new HttpGet("http://192.168.1.37:8080/WebClientRest/webresources/mapas/cincoUltimasPosiciones/" + matricula);

            get.setHeader("content-type", "application/json");

            try {
                HttpResponse resp = httpClient.execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray arrayPos = new JSONArray(respStr);
                arrayPosiciones = new ArrayList<>();
                for (int i = 0; i < arrayPos.length(); i++) {
                    JSONObject pos = arrayPos.getJSONObject(i);
                    matricula = pos.getString("matricula");
                    double latitud = pos.getDouble("latitud");
                    double longitud = pos.getDouble("longitud");
                    data = pos.getString("data");
                    arrayPosiciones.add(new LatLng(latitud, longitud));
                }
                resul = true;
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                resul = false;
            }
            return resul;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                Toast.makeText(MapsActivity.this, arrayPosiciones.get(0).toString(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MapsActivity.this, "No ubicaciones", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
