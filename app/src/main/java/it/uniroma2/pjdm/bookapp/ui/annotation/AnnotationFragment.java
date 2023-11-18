package it.uniroma2.pjdm.bookapp.ui.annotation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import it.uniroma2.pjdm.bookapp.R;
import it.uniroma2.pjdm.bookapp.adapter.AnnotationAdapter;
import it.uniroma2.pjdm.bookapp.model.AnnotationModel;
import it.uniroma2.pjdm.bookapp.ui.favorites.FavoritesFragment;

public class AnnotationFragment extends Fragment {

    private final ArrayList < AnnotationModel > notesList = new ArrayList < > ();
    private AnnotationAdapter adapter;
    private static final String TAG = "GT_TAG";
    private final Executor executor = Executors.newSingleThreadExecutor();
    RecyclerView recyclerView;
    TextView emptyFav;

    public AnnotationFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String titolo = getTitle();
        String autore = getAuthor();
        String url = getUrl();

        View view = inflater.inflate(R.layout.fragment_annotation, container, false);
        TextView titleANN = view.findViewById(R.id.tv_ann_titolo);
        titleANN.setText(titolo);
        TextView authorANN = view.findViewById(R.id.tv_ann_autore);
        authorANN.setText(autore);
        ImageView urlImageANN = view.findViewById(R.id.iv_urlImage_ann);
        Glide.with(requireContext()).load(url).into(urlImageANN);

        AppCompatButton back = view.findViewById(R.id.bt_back_to_fav);

        FloatingActionButton fab = view.findViewById(R.id.bt_add_ann);
        fab.setOnClickListener(v -> {
            TakesNotes fragment = TakesNotes.newInstance(titolo, autore, url);
            FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.nav_host_fragment_home_main, fragment, "takes_notes");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        back.setOnClickListener(v -> {
            Fragment fragment = new FavoritesFragment();
            FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.nav_host_fragment_home_main, fragment, "preferiti");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emptyFav = view.findViewById(R.id.tvEmptyAnn);

        recyclerView = view.findViewById(R.id.rv_book_ann);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new AnnotationAdapter(notesList, new AnnotationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AnnotationModel model, int position) {

            }

            @Override
            public void onItemSwipe(AnnotationModel annotation, int position) {
                removeNotes(annotation.getAnnotationId());
            }
        });

        recyclerView.setAdapter(adapter);

        loadNotes();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeItemAnn(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void loadNotes() {
        executor.execute(() -> {
            try {
                int userId = getUserId();
                int bookId = getBookId();
                String indirizzo = getString(R.string.url_servlet) + "/AddNoteServlet?idUser=" + userId + "&idBook=" + bookId;
                Log.d(TAG, "loadNotes: URL: " + indirizzo);

                URL url = new URL(indirizzo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder responseStringBuilder = new StringBuilder();
                    String line;

                    while ((line = br.readLine()) != null) {
                        responseStringBuilder.append(line);
                    }

                    String jsonString = responseStringBuilder.toString();
                    Log.d(TAG, "loadNotes: Response: " + jsonString);

                    if (!jsonString.isEmpty()) {
                        JSONArray notesArray = new JSONArray(jsonString);

                        for (int i = 0; i < notesArray.length(); i++) {
                            JSONObject note = notesArray.getJSONObject(i);
                            int annotationId = note.getInt("idAnnotazione");
                            String testoAnnotazione = note.getString("testoAnnotazione");

                            requireActivity().runOnUiThread(() -> adapter.add(new AnnotationModel(annotationId, testoAnnotazione)));
                            Log.d(TAG, "loadNotes: Notes - Frase: " + testoAnnotazione);
                        }
                        checkVisibility(notesArray);
                    } else {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "La risposta del server è vuota o non valida", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    requireActivity().runOnUiThread(() -> {
                        try {
                            Log.e(TAG, "loadNotes: HTTP Error - " + conn.getResponseCode());
                            Toast.makeText(requireContext(), "Errore nella risposta del server: " + conn.getResponseCode(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

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

    //Metodo per aggiornare la visibility del TextView in base all'elenco delle note
    public void checkVisibility(JSONArray notesArray){
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            if (notesArray.length() ==0) {
                recyclerView.setVisibility(View.GONE);
                emptyFav.setVisibility(View.VISIBLE);
            }
            else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyFav.setVisibility(View.GONE);
            }
        });

    }

    private void removeNotes(int annotationID) {
        executor.execute(() -> {
            try {
                Log.d(TAG, "removeNotes: NoteId: " + annotationID);

                String indirizzo = getString(R.string.url_servlet) + "/AddNoteServlet?idNote=" + annotationID;
                Log.d(TAG, "removeNotes: URL: " + indirizzo);
                URL url = new URL(indirizzo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "removeNotes: Codice di risposta: " + responseCode);

                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    Log.d(TAG, "removeNotes: Risposta del server: " + sb);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    onSuccess();
                } else {
                    onCatchError(responseCode);
                }

            } catch (IOException e) {
                onCatchError(-1);
                Log.e(TAG, "removeNotes: IOException", e);
            }
        });
    }

    private void onSuccess() {
        Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                Toast.makeText(getActivity(), "Note rimosse con successo...", Toast.LENGTH_LONG).show());
    }

    private void onCatchError(int errorCode) {
        Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                Toast.makeText(getActivity(), "Errore durante l'operazione (Codice: " + errorCode + ")", Toast.LENGTH_LONG).show());
    }

    private int getUserId() {
        SharedPreferences shared = requireActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        return shared.getInt("idUser", -1);
    }

    private int getBookId() {
        SharedPreferences shared = requireActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        return shared.getInt("bookId", -1);
    }

    private String getTitle() {
        SharedPreferences shared = requireActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        return shared.getString("titolo", "");
    }

    private String getAuthor() {
        SharedPreferences shared = requireActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        return shared.getString("autore", "");
    }

    private String getUrl() {
        SharedPreferences shared = requireActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        return shared.getString("url", "");
    }
}