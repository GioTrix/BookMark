package it.uniroma2.pjdm.bookapp.ui.annotation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import it.uniroma2.pjdm.bookapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TakesNotes#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TakesNotes extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    private static final String TAG = "GT_TAG";
    private final Executor executor = Executors.newSingleThreadExecutor();

    private EditText et_take_notes;

    public TakesNotes() {

    }

    public static TakesNotes newInstance(String titolo, String autore, String url) {
        TakesNotes fragment = new TakesNotes();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, titolo);
        args.putString(ARG_PARAM2, autore);
        args.putString(ARG_PARAM3, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_takes_notes, container, false);

        et_take_notes = view.findViewById(R.id.et_take_notes);
        ImageView iv_save = view.findViewById(R.id.iv_save);
        Button bt_close_notes = view.findViewById(R.id.bt_close_notes);

        iv_save.setOnClickListener(v -> {
            String frase = et_take_notes.getText().toString();

            if (frase.isEmpty()) {
                Toast.makeText(requireContext(), "Inserisci una frase!", Toast.LENGTH_SHORT).show();
                return;
            }

            takesNote(frase);
        });

        bt_close_notes.setOnClickListener(v -> navigateToAnnotationFragment());

        return view;
    }

    private void navigateToAnnotationFragment() {
        Fragment fragment = new AnnotationFragment();
        FragmentTransaction fragmentTransaction = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment_home_main, fragment, "annotation_fragment");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void takesNote(String frase) {
        executor.execute(() -> {
            int idUser = getUserId();
            int idBook = getBookId();

            Log.d(TAG, "takesNote: idUser=" + idUser + ", idBook=" + idBook + ", frase=" + frase);

            String indirizzo = getString(R.string.url_servlet) + "/AddNoteServlet";
            Log.d(TAG, "takesNote: Indirizzo: " + indirizzo);

            HttpURLConnection http = null;
            try {
                URL url = new URL(indirizzo);
                http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("POST");

                Map < String, String > params = new LinkedHashMap < > ();
                params.put("idUser", String.valueOf(idUser));
                params.put("idBook", String.valueOf(idBook));
                params.put("testoAnnotazione", frase);

                StringBuilder postData = new StringBuilder();

                for (Map.Entry < String, String > param: params.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }

                byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);
                http.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                http.setDoOutput(true);

                try (OutputStream os = http.getOutputStream()) {
                    os.write(postDataBytes);
                }

                int code = http.getResponseCode();
                Log.d(TAG, "takes note: Codice di risposta: " + code);

                if (code == HttpURLConnection.HTTP_OK) {
                    // Successo
                    Log.d(TAG, "takes note: Successo");
                    navigateToAnnotationFragment();
                } else {
                    // Errore
                    Toast.makeText(getActivity(), "Errore di salvataggio", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "takes note: Errore, codice " + code);

                    // Aggiungiamo la lettura della risposta del server (se disponibile)
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(http.getErrorStream()))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        Log.e(TAG, "takes note: Messaggio di errore del server: " + sb);
                    } catch (IOException e) {
                        Log.e(TAG, "takes note: Errore nella lettura del messaggio di errore del server", e);
                    }
                }

            } catch (IOException e) {
                Log.e(TAG, "takes note: IOException", e);
            } finally {
                if (http != null) {
                    http.disconnect();
                }
            }
        });
    }

    private int getUserId() {
        SharedPreferences shared = requireActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        return shared.getInt("idUser", -1);
    }

    private int getBookId() {
        SharedPreferences shared = requireActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        return shared.getInt("bookId", -1);
    }
}