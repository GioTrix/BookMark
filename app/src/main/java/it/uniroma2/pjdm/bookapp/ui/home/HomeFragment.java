package it.uniroma2.pjdm.bookapp.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import it.uniroma2.pjdm.bookapp.R;
import it.uniroma2.pjdm.bookapp.adapter.BookCatalogoAdapter;
import it.uniroma2.pjdm.bookapp.adapter.SpinnerAdapter;
import it.uniroma2.pjdm.bookapp.model.BookCatalogoModel;

public class HomeFragment extends Fragment {
    private static final String TAG = "GT_TAG";
    private final ArrayList < BookCatalogoModel > bookList = new ArrayList < > ();
    private SpinnerAdapter sAdapter;
    private BookCatalogoAdapter adapter;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public HomeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Imposta il LayoutManager per mostrare la lista dei libri preferiti
        RecyclerView recyclerView = view.findViewById(R.id.rv_catalogo);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        SearchView searchView = view.findViewById(R.id.sv_catalogue);
        recyclerView.setHasFixedSize(true);

        adapter = new BookCatalogoAdapter(requireContext(), bookList);

        Spinner spinner = view.findViewById(R.id.filterSpinner);
        sAdapter = new SpinnerAdapter(requireContext(), spinner);
        recyclerView.setAdapter(adapter);

        //Richiede e carica la lista dei libri
        getAllBooks();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String value) {
                //Recupera il filtro selezionato dallo Spinner
                String filter = sAdapter.getSelectedFilter();

                //Controllo se la coppia filtro-valore è corretta
                Log.d(TAG, "Filtro selezionato: " + filter);
                Log.d(TAG, "Valore di ricerca: " + value);

                //Chiama il metodo searchBooks con il filtro e il valore
                searchBooks(filter, value);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        adapter.setFavButtonClickListener((bookId, isInFav) -> {
            if (isInFav) {
                addBookToFavorites(bookId);
            } else {
                Toast.makeText(getContext(), "Libro già aggiunto ai preferiti", Toast.LENGTH_LONG).show();
            }
        });

        adapter.setOnBookClickListener((titolo, autore, url, idBook) -> {
            Fragment fragment = new BookDetail();

            //Passa i dati al fragment dei dettagli (modifica come necessario)
            Bundle bundle = new Bundle();
            bundle.putString("titolo", titolo);
            bundle.putString("autore", autore);
            bundle.putString("url", url);
            bundle.putInt("idLibro", idBook);
            fragment.setArguments(bundle);

            FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.nav_host_fragment_home_main, fragment, "bookDetail");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }

    private void addBookToFavorites(int bookId) {
        executor.execute(() -> {
            try {
                int userId = getUserId();
                Log.d(TAG, "addBookToFavorites: UserId: " + userId + ", BookId: " + bookId);

                String indirizzo = getString(R.string.url_servlet) + "/BookFav";
                URL url = new URL(indirizzo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                Map < String, String > params = new LinkedHashMap < > ();
                params.put("idUser", String.valueOf(userId));
                params.put("idBook", String.valueOf(bookId));

                StringBuilder postData = new StringBuilder();

                for (Map.Entry < String, String > param: params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }

                byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);
                conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postDataBytes);
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "addBookToFavorites: Codice di risposta: " + responseCode);

                //Leggi la risposta anche in caso di errore 500
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    Log.d(TAG, "addBookToFavorites: Risposta del server: " + sb);
                } catch (IOException e) {
                    Log.e(TAG, "addBookToFavorites: Risposta del server: " + e.getMessage());
                }

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    onSuccess(bookId); // Successo
                } else {

                    onCatchError(responseCode); //Errore
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void onCatchError(int errorCode) {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> Toast.makeText(getActivity(), "Errore durante l'operazione (Codice: " + errorCode + ")", Toast.LENGTH_LONG).show());
    }
    private void onSuccess(int bookId) {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            Toast.makeText(getActivity(), "Libro aggiunto ai preferiti con successo...", Toast.LENGTH_LONG).show();
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MySharedPref", MODE_PRIVATE);
            SharedPreferences.Editor edt = sharedPreferences.edit();
            Log.d(TAG, "Salvataggio nelle SharedPreferences...");
            //Salva i valori nelle SharedPreferences
            edt.putInt("bookId", bookId);
            edt.apply();
        });
    }

    private int getUserId() {
        SharedPreferences shared = requireActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        return shared.getInt("idUser", -1);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void searchBooks(String filter, String value) {
        executor.execute(() -> {
            try {
                String indirizzo = getString(R.string.url_servlet) + "/SearchBooks?filter=" + URLEncoder.encode(filter, "UTF-8") + "&value=" + URLEncoder.encode(value, "UTF-8");
                Log.d(TAG, "URL della richiesta: " + indirizzo);
                URL url = new URL(indirizzo);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);

                int responseCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "Codice di risposta HTTP: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    String jsonString = br.readLine();
                    Log.d(TAG, "Risposta JSON: " + jsonString);

                    if (jsonString != null) {
                        //Prima di aggiungere nuovi libri, svuota l'elenco esistente
                        bookList.clear();

                        //La risposta non è null, quindi puoi analizzarla come JSON
                        JSONArray booksArray = new JSONArray(jsonString);

                        //Aggiungi i nuovi libri all'elenco svuotato
                        for (int i = 0; i < booksArray.length(); i++) {
                            JSONObject book = booksArray.getJSONObject(i);
                            int idLibro = book.getInt("idLibro");
                            String titolo = book.getString("titolo");
                            String autore = book.getString("autore");
                            String genere = book.getString("genere");
                            String copertina = book.getString("url");

                            //Aggiungi i nuovi libri all'elenco
                            bookList.add(new BookCatalogoModel(idLibro, titolo, autore, genere, copertina));
                        }

                        //Aggiorna l'adapter con i nuovi libri
                        requireActivity().runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                            Toast.makeText(getContext(), "Ricerca eseguita", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "run: book calling");
                        });
                    } else {
                        //La risposta è null o vuota, gestisci l'errore
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "La risposta del server è vuota o non valida", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Log.e(TAG, "Error 404");
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getAllBooks() {
        executor.execute(() -> {
            try {
                String indirizzo = getString(R.string.url_servlet) + "/AllBooks";
                URL url = new URL(indirizzo);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8));

                //Verifica se la risposta contiene dati JSON prima di creare il JSONArray
                String jsonString = br.readLine();
                if (jsonString != null) {
                    JSONArray booksArray = new JSONArray(jsonString);

                    //Elaborazione del JSONArray
                    for (int i = 0; i < booksArray.length(); i++) {
                        JSONObject book = booksArray.getJSONObject(i);
                        int idLibro = book.getInt("idLibro");
                        String titolo = book.getString("titolo");
                        String autore = book.getString("autore");
                        String genere = book.getString("genere");
                        String copertina = book.getString("url");

                        //Aggiornamento dell'adapter per riflettere i dati ottenuti
                        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                            adapter.add(new BookCatalogoModel(idLibro, titolo, autore, genere, copertina));
                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "run: book calling");
                        });
                    }
                } else {
                    //Gestione del caso in cui jsonString sia null o non contenga dati JSON
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Errore nella risposta del server", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        });
    }
}