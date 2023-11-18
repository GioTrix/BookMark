package it.uniroma2.pjdm.bookapp.ui.statistics;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

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
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import it.uniroma2.pjdm.bookapp.R;
import it.uniroma2.pjdm.bookapp.ui.settings.SettingsFragment;

public class StatisticsFragment extends Fragment {

    private BarChart barChart;
    private TextView tvBookReads;
    private TextView tvQuotesAnnotated;
    private TextView tvGenreStats;
    private static final String TAG = "GT_TAG";
    private final Executor executor = Executors.newSingleThreadExecutor();

    public StatisticsFragment() {
        barChart = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        barChart = view.findViewById(R.id.chartBooksRead);
        tvBookReads = view.findViewById(R.id.tvBooksRead);
        tvQuotesAnnotated = view.findViewById(R.id.tvQuotesAnnotated);
        tvGenreStats = view.findViewById(R.id.tvStatsGenere);

        getClassifica();
        getBooksRead();
        getNotes();
        getGenreStats();

        AppCompatButton back = view.findViewById(R.id.bt_back_to_settings);
        back.setOnClickListener(v -> {
            Fragment fragment = new SettingsFragment();

            FragmentTransaction fragmentTransaction = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.nav_host_fragment_home_main, fragment, "settings");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        return view;
    }

    private void getNotes() {
        executor.execute(() -> {
            try {
                int idUser = getUserId();
                String indirizzo = getString(R.string.url_servlet) + "/NoteStatsServlet?idUser=" + idUser;
                Log.d(TAG, "getNotes: URL: " + indirizzo);

                URL url = new URL(indirizzo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                //Verifica se la risposta contiene dati JSON prima di creare il JSONArray
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder responseStringBuilder = new StringBuilder();
                    String line;

                    //Lettura della risposta
                    while ((line = br.readLine()) != null) {
                        responseStringBuilder.append(line);
                    }

                    String jsonString = responseStringBuilder.toString();
                    Log.d(TAG, "getNotes: Response: " + jsonString); // Log della risposta JSON

                    if (!jsonString.isEmpty()) {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        int noteStats = jsonObject.getInt("noteStats");

                        requireActivity().runOnUiThread(() -> tvQuotesAnnotated.setText(String.format(Locale.getDefault(), "Frasi celebri annotate: %d", noteStats)));

                    } else {
                        //Gestione del caso in cui jsonString sia null o vuoto
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "La risposta del server è vuota o non valida", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    //Gestione del caso in cui la risposta HTTP non sia OK
                    requireActivity().runOnUiThread(() -> {
                        try {
                            Log.e(TAG, "getNotes: HTTP Error - " + conn.getResponseCode()); // Log dell'errore HTTP
                            Toast.makeText(getContext(), "Errore nella risposta del server: " + conn.getResponseCode(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

                //Chiusura della connessione
                conn.disconnect();
            } catch (IOException | JSONException e) {
                Log.e(TAG, "getNotes: Exception - " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void getGenreStats() {
        executor.execute(() -> {
            try {
                int idUser = getUserId();
                String indirizzo = getString(R.string.url_servlet) + "/GetGenreStats?idUser=" + idUser;
                Log.d(TAG, "getGenreStats: URL: " + indirizzo);

                URL url = new URL(indirizzo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                //Verifica se la risposta contiene dati JSON prima di creare il JSONArray
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder responseStringBuilder = new StringBuilder();
                    String line;

                    //Lettura della risposta
                    while ((line = br.readLine()) != null) {
                        responseStringBuilder.append(line);
                    }

                    String jsonString = responseStringBuilder.toString();
                    Log.d(TAG, "getGenreStats: Response: " + jsonString); // Log della risposta JSON

                    if (!jsonString.isEmpty()) {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        String genere = jsonObject.getString("genere");

                        requireActivity().runOnUiThread(() -> tvGenreStats.setText(String.format(Locale.getDefault(), "Genere preferito: %s", genere)));

                    } else {
                        //Gestione del caso in cui jsonString sia null o vuoto
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "La risposta del server è vuota o non valida", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    //Gestione del caso in cui la risposta HTTP non sia OK
                    requireActivity().runOnUiThread(() -> {
                        try {
                            Log.e(TAG, "getGenreStats: HTTP Error - " + conn.getResponseCode()); // Log dell'errore HTTP
                            Toast.makeText(getContext(), "Errore nella risposta del server: " + conn.getResponseCode(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

                //Chiusura della connessione
                conn.disconnect();
            } catch (IOException | JSONException e) {
                Log.e(TAG, "getNotes: Exception - " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void getBooksRead() {
        executor.execute(() -> {
            try {
                int idUser = getUserId();
                String indirizzo = getString(R.string.url_servlet) + "/BookStatsServlet?idUser=" + idUser;
                Log.d(TAG, "getBooksRead: URL: " + indirizzo);

                URL url = new URL(indirizzo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                //Verifica se la risposta contiene dati JSON prima di creare il JSONArray
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder responseStringBuilder = new StringBuilder();
                    String line;

                    //Lettura della risposta
                    while ((line = br.readLine()) != null) {
                        responseStringBuilder.append(line);
                    }

                    String jsonString = responseStringBuilder.toString();
                    Log.d(TAG, "getBooksRead: Response: " + jsonString);

                    if (!jsonString.isEmpty()) {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        int bookStats = jsonObject.getInt("bookStats");

                        requireActivity().runOnUiThread(() -> tvBookReads.setText(String.format(Locale.getDefault(), "Numero libri letti: %d", bookStats)));
                    } else {
                        //Gestione del caso in cui jsonString sia null o vuoto
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "La risposta del server è vuota o non valida", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    //Gestione del caso in cui la risposta HTTP non sia OK
                    requireActivity().runOnUiThread(() -> {
                        try {
                            Log.e(TAG, "getBooksRead: HTTP Error - " + conn.getResponseCode()); // Log dell'errore HTTP
                            Toast.makeText(getContext(), "Errore nella risposta del server: " + conn.getResponseCode(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

                //Chiusura della connessione
                conn.disconnect();
            } catch (IOException | JSONException e) {
                Log.e(TAG, "getBooksRead: Exception - " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void getClassifica() {
        executor.execute(() -> {
            try {
                String indirizzo = getString(R.string.url_servlet) + "/GetClassificaLettori";
                Log.d(TAG, "getClassifica: URL: " + indirizzo);

                URL url = new URL(indirizzo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                //Verifica se la risposta contiene dati JSON prima di creare il JSONArray
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder responseStringBuilder = new StringBuilder();
                    String line;

                    //Lettura della risposta
                    while ((line = br.readLine()) != null) {
                        responseStringBuilder.append(line);
                    }

                    String jsonString = responseStringBuilder.toString();
                    Log.d(TAG, "getClassifica: Response: " + jsonString); // Log della risposta JSON

                    if (!jsonString.isEmpty()) {
                        JSONArray usersArray = new JSONArray(jsonString);

                        ArrayList < BarEntry > barEntries = new ArrayList < > ();
                        ArrayList < String > usernames = new ArrayList < > (); // Lista per mantenere i nomi utente

                        //Elaborazione del JSONArray
                        for (int i = 0; i < usersArray.length(); i++) {
                            JSONObject user = usersArray.getJSONObject(i);
                            String username = user.getString("username");
                            int countBook = user.getInt("countBook");

                            // Aggiungi i dati al grafico a barre
                            BarEntry barEntry = new BarEntry(i, countBook);
                            barEntries.add(barEntry);
                            usernames.add(username); // Aggiungi il nome utente alla lista
                        }

                        //Aggiornamento del grafico con i dati ottenuti
                        requireActivity().runOnUiThread(() -> {
                            BarDataSet barDataSet = new BarDataSet(barEntries, "Libri letti");
                            barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                            barDataSet.setDrawValues(true);

                            BarData barData = new BarData(barDataSet);

                            //Imposta i dati sull'asse X con i nomi utente
                            XAxis xAxis = barChart.getXAxis();
                            xAxis.setValueFormatter(new IndexAxisValueFormatter(usernames));
                            xAxis.setTextSize(10);
                            xAxis.setLabelCount(usernames.size());

                            barChart.setData(barData);
                            barChart.animateY(5000);
                            barChart.getDescription().setEnabled(false);
                        });
                    } else {
                        //Gestione del caso in cui jsonString sia null o vuoto
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "La risposta del server è vuota o non valida", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    //Gestione del caso in cui la risposta HTTP non sia OK
                    requireActivity().runOnUiThread(() -> {
                        try {
                            Log.e(TAG, "getClassifica: HTTP Error - " + conn.getResponseCode()); // Log dell'errore HTTP
                            Toast.makeText(getContext(), "Errore nella risposta del server: " + conn.getResponseCode(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

                //Chiusura della connessione
                conn.disconnect();
            } catch (MalformedURLException e) {
                Log.e(TAG, "getClassifica: MalformedURLException - " + e.getMessage()); //Log dell'errore
                e.printStackTrace();
            } catch (JSONException | IOException e) {
                Log.e(TAG, "getClassifica: JSONException or IOException - " + e.getMessage()); //Log dell'errore
                e.printStackTrace();
            }
        });
    }

    private int getUserId() {
        SharedPreferences shared = requireActivity().getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return shared.getInt("idUser", -1);
    }

}