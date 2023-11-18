package it.uniroma2.pjdm.bookapp.adapter;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import it.uniroma2.pjdm.bookapp.R;

public class SpinnerAdapter {

    private String selectedFilter;

    public SpinnerAdapter(Context context, Spinner spinner) {
        selectedFilter = "";

        //Recupera le categorie dall'array di stringhe
        String[] categorie = context.getResources().getStringArray(R.array.filter);

        //Configura un adapter per lo Spinner
        ArrayAdapter < String > adapter = new ArrayAdapter < > (context, android.R.layout.simple_spinner_item, categorie);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Imposta l'adapter per lo Spinner
        spinner.setAdapter(adapter);

        //Configura un listener per gestione dell'evento di selezione delle categorie
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView < ? > parentView, View selectedItemView, int position, long id) {
                selectedFilter = (String) parentView.getItemAtPosition(position); // Aggiorna il filtro selezionato
            }

            @Override
            public void onNothingSelected(AdapterView < ? > parentView) {
                Toast.makeText(context, "Nessuna categoria selezionata", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Metodo per ottenere il filtro selezionato
    public String getSelectedFilter() {
        return selectedFilter;
    }
}