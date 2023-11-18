package it.uniroma2.pjdm.bookapp.ui.favorites;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import it.uniroma2.pjdm.bookapp.R;
import it.uniroma2.pjdm.bookapp.adapter.BookFavAdapter;
import it.uniroma2.pjdm.bookapp.model.BookFavModel;
import it.uniroma2.pjdm.bookapp.ui.annotation.AnnotationFragment;

public class FavoritesFragment extends Fragment {
    private final ArrayList < BookFavModel > bookFavList = new ArrayList < > (); // Elenco dei libri preferiti
    private BookFavAdapter adapter;
    private static final String TAG = "GT_TAG";
    private final Executor executor = Executors.newSingleThreadExecutor();
    private RecyclerView recyclerView;

    private TextView emptyFav;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Permette operazioni di rete nel thread principale (usato solo a fini didattici)
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emptyFav = view.findViewById(R.id.tvEmptyFav);

        recyclerView = view.findViewById(R.id.rv_book_fav);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        //Inizializzazione dell'adapter
        adapter = new BookFavAdapter(requireContext(), bookFavList, new BookFavAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BookFavModel model, int position) {

                saveBookDataToSharedPreferences(model.getTitle(), model.getAuthor(), model.getImageUrl(), model.getBookId());

                //Crea una nuova istanza di AnnotationFragment
                AnnotationFragment fragment = new AnnotationFragment();

                //Passa i dati al nuovo fragment
                Bundle bundle = new Bundle();
                bundle.putString("titolo", model.getTitle());
                bundle.putString("autore", model.getAuthor());
                bundle.putString("url", model.getImageUrl());
                bundle.putInt("bookId", model.getBookId());
                fragment.setArguments(bundle);

                //Sostituzione del fragment corrente con il nuovo
                FragmentTransaction fragmentTransaction = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment_home_main, fragment, "annotation_fragment");
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }

            private void saveBookDataToSharedPreferences(String title, String author, String imageUrl, int bookId) {
                //Salva i dati del libro nelle SharedPreferences
                SharedPreferences preferences = requireActivity().getSharedPreferences("MySharedPref", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("titolo", title);
                editor.putString("autore", author);
                editor.putString("url", imageUrl);
                editor.putInt("bookId", bookId);
                editor.apply();
            }

            @Override
            public void onItemSwipe(BookFavModel book, int position) {
                Log.d(TAG, "onItemSwipe");
                removeBookFromFavorites(book.getBookId());
            }
        });
        recyclerView.setAdapter(adapter);

        loadBookFav();

        //Supporto per l'azione di "swipe"
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeItem(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadBookFav() {
        executor.execute(() -> {
            try {
                // Ottieni l'ID dell'utente corrente
                int userId = getUserId();
                String indirizzo = getString(R.string.url_servlet) + "/BookFav?idUser=" + userId;
                Log.d(TAG, "loadBookFav: URL: " + indirizzo);

                URL url = new URL(indirizzo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                //Verifica se la risposta contiene dati JSON prima di creare il JSONArray
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder responseStringBuilder = new StringBuilder();
                    String line;

                    //Lettura della risposta
                    while ((line = br.readLine()) != null) {
                        responseStringBuilder.append(line);
                    }

                    String jsonString = responseStringBuilder.toString();
                    Log.d(TAG, "loadBookFav: Response: " + jsonString);

                    //Creazione del JSONArray dalla stringa JSON
                    JSONArray booksArray = new JSONArray(jsonString);

                    //Elaborazione del JSONArray
                    for (int i = 0; i < booksArray.length(); i++) {
                        JSONObject book = booksArray.getJSONObject(i);
                        String titolo = book.getString("titolo");
                        String autore = book.getString("autore");
                        String urlCopertina = book.getString("url");
                        int bookId = book.getInt("idLibro");

                        //Aggiorna l'adapter per riflettere i dati ottenuti
                        adapter.add(new BookFavModel(titolo, autore, urlCopertina, bookId));
                        Log.d(TAG, "loadBookFav: Book added - Title: " + titolo + ", Author: " + autore + ", URL: " + urlCopertina + ", ID: " + bookId);

                        //Aggiorna l'UI sul thread principale
                        requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                    }
                    checkVisibility(booksArray);
                } else {
                    //Gestione del caso in cui la risposta HTTP non sia OK
                    requireActivity().runOnUiThread(() -> {
                        try {
                            Log.e(TAG, "loadBookFav: HTTP Error - " + conn.getResponseCode());
                            Toast.makeText(getContext(), "Errore nella risposta del server: " + conn.getResponseCode(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

                //Chiusura della connessione
                conn.disconnect();
            } catch (MalformedURLException e) {
                Log.e(TAG, "loadBookFav: MalformedURLException - " + e.getMessage());
                e.printStackTrace();
            } catch (JSONException | IOException e) {
                Log.e(TAG, "loadBookFav: JSONException or IOException - " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    //Metodo per aggiornare la visibility del TextView in base all'elenco dei libri
    public void checkVisibility(JSONArray booksArray){
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            if (booksArray.length()==0) {
                recyclerView.setVisibility(View.GONE);
                emptyFav.setVisibility(View.VISIBLE);
            }
            else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyFav.setVisibility(View.GONE);
            }
        });

    }

    private void removeBookFromFavorites(int bookId) {
        executor.execute(() -> {
            try {
                //Ottieni l'ID dell'utente corrente
                int userId = getUserId();
                Log.d(TAG, "removeBookToFavorites: UserId: " + userId + ", BookId: " + bookId);

                String indirizzo = getString(R.string.url_servlet) + "/BookFav?idUser=" + userId + "&idBook=" + bookId;
                Log.d(TAG, "removeBookToFavorites: URL: " + indirizzo);
                URL url = new URL(indirizzo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "removeBookToFavorites: Codice di risposta: " + responseCode);

                //Lettura della risposta anche in caso di errore 500
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    Log.d(TAG, "removeBookToFavorites: Risposta del server: " + sb);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                Log.e(TAG, "removeBookToFavorites: IOException", e);
            }
        });
    }

    private int getUserId() {
        //Ottieni l'ID dell'utente dalle SharedPreferences
        SharedPreferences shared = requireActivity().getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return shared.getInt("idUser", -1);
    }
}