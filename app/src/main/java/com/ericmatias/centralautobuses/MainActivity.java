package com.ericmatias.centralautobuses;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnVariosBuses, btnUnBus, btnIniciarMapa;
    ImageButton btnInfo, btnAtras;
    Spinner spin;
    Space space1, space2, space3;
    TextView verMatricula, errorMatricula;
    String matriculaSelect;
    String[] spinDefault = {"Selecciona una matricula"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniciarValores();
        setAdaptador(spinDefault);
        obtenerAutobusesSpinner oas = new obtenerAutobusesSpinner();
        oas.execute();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case (R.id.btnUnBus):
                modificarVisi();
                break;
            case (R.id.btnTodosBuses):
                Intent intent = new Intent(this, MapsActivityTodosBuses.class);
                startActivity(intent);
                break;
            case (R.id.btnInfo):
                if (btnIniciarMapa.getVisibility() != View.VISIBLE) {
                    mostrarInfo(true);
                } else {
                    mostrarInfo(false);
                }
                break;
            case (R.id.btnAtras):
                volver();
                break;
            case (R.id.btnIniciarMapa):
                if (spin.getSelectedItem().toString().equals("Selecciona una matricula")) {
                    errorMatricula.setVisibility(View.VISIBLE);
                } else {
                    Intent i = new Intent(this, MapsActivity.class);
                    matriculaSelect = spin.getSelectedItem().toString();
                    i.putExtra("matricula", matriculaSelect);
                    startActivity(i);
                    break;
                }
        }
    }

    public void mostrarInfo(boolean tipo) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Instrucciones");
        alertDialog.setMessage(tipo ? getString(R.string.instruccion1) : getString(R.string.instruccion2));
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.aceptar),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public void setAdaptador(String[] array) {
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, array);
        spin.setAdapter(adaptador);
    }

    //Tarea Asincrona para llamar al WS de consulta en segundo plano
    private class obtenerAutobusesSpinner extends AsyncTask<Void, Void, Boolean> {

        String[] arrayMatriculas;

        public obtenerAutobusesSpinner() {
        }

        protected Boolean doInBackground(Void... params) {

            boolean correcto;
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("http://192.168.1.37:8080/Webservice/webresources/mapas/todas/matriculas");
            get.setHeader("content-type", "application/json");

            try {
                HttpResponse resp = httpClient.execute(get);
                String respStr = EntityUtils.toString(resp.getEntity());
                JSONArray arrayBus = new JSONArray(respStr);
                arrayMatriculas = new String[arrayBus.length()];
                for (int i = 0; i < arrayBus.length(); i++) {
                    JSONObject bus = arrayBus.getJSONObject(i);
                    String matricula = bus.getString("matricula");
                    arrayMatriculas[i] = matricula;
                }
                correcto = true;
            } catch (Exception ex) {
                Log.e("ServicioRest", "Error!", ex);
                correcto = false;
            }
            return correcto;
        }

        /**
         * Metodo que se realiza despues de ejecutarse el metodo onBackground para decirnos basicamente
         * Si se ha realizado o no el Insert Into
         *
         * @param result
         */
        protected void onPostExecute(Boolean result) {

            if (result) {
                setAdaptador(arrayMatriculas);
            } else {
                Toast.makeText(MainActivity.this, "No se ha podido listar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void iniciarValores() {
        btnUnBus = (Button) findViewById(R.id.btnUnBus);
        btnVariosBuses = (Button) findViewById(R.id.btnTodosBuses);
        btnIniciarMapa = (Button) findViewById(R.id.btnIniciarMapa);
        btnInfo = (ImageButton) findViewById(R.id.btnInfo);
        btnAtras = (ImageButton) findViewById(R.id.btnAtras);
        verMatricula = (TextView) findViewById(R.id.selMatricula);
        errorMatricula = (TextView) findViewById(R.id.errorMatricula);
        spin = (Spinner) findViewById(R.id.spinner);
        space1 = (Space) findViewById(R.id.space1);
        space2 = (Space) findViewById(R.id.space2);
        space3 = (Space) findViewById(R.id.space3);
        btnAtras.setOnClickListener(this);
        btnUnBus.setOnClickListener(this);
        btnVariosBuses.setOnClickListener(this);
        btnInfo.setOnClickListener(this);
        btnIniciarMapa.setOnClickListener(this);
    }

    public void modificarVisi() {
        space2.setVisibility(View.VISIBLE);
        space3.setVisibility(View.VISIBLE);
        btnAtras.setVisibility(View.VISIBLE);
        btnVariosBuses.setVisibility(View.GONE);
        btnUnBus.setVisibility(View.GONE);
        btnIniciarMapa.setVisibility(View.VISIBLE);
        verMatricula.setVisibility(View.VISIBLE);
        spin.setVisibility(View.VISIBLE);
    }

    public void volver() {
        errorMatricula.setVisibility(View.GONE);
        space1.setVisibility(View.VISIBLE);
        space2.setVisibility(View.GONE);
        space3.setVisibility(View.GONE);
        btnAtras.setVisibility(View.GONE);
        btnVariosBuses.setVisibility(View.VISIBLE);
        btnUnBus.setVisibility(View.VISIBLE);
        btnIniciarMapa.setVisibility(View.GONE);
        verMatricula.setVisibility(View.GONE);
        spin.setVisibility(View.GONE);
    }
}
