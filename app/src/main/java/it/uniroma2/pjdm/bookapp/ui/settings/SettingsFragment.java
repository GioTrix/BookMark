package it.uniroma2.pjdm.bookapp.ui.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import it.uniroma2.pjdm.bookapp.R;
import it.uniroma2.pjdm.bookapp.activities.MainActivity;
import it.uniroma2.pjdm.bookapp.ui.statistics.StatisticsFragment;

public class SettingsFragment extends Fragment {

    private static final String TAG = "GT_TAG";
    private final Executor executor = Executors.newSingleThreadExecutor();

    public SettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        TextView tvExit = view.findViewById(R.id.tvExit);

        TextView tvPrivacy = view.findViewById(R.id.tvPrivacy);

        tvPrivacy.setOnClickListener(v -> showPrivacyDialog());

        TextView tvVersion = view.findViewById(R.id.tvVersion);
        tvVersion.setOnClickListener(v -> showVersionDialog());

        TextView tvStats = view.findViewById(R.id.tvStats);
        tvStats.setOnClickListener(v -> {
            Fragment fragment = new StatisticsFragment();

            FragmentTransaction fragmentTransaction = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.nav_host_fragment_home_main, fragment, "statistics");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        tvExit.setOnClickListener(v -> logout());

        return view;
    }

    private void logout() {
        executor.execute(() -> {
            int userId = getUserId();
            String token = getToken();
            try {
                Log.d(TAG, "logout: UserId: " + userId);

                String indirizzo = getString(R.string.url_servlet) + "/LoginServlet?idUser=" + userId + "&token=" + URLEncoder.encode(token, "UTF-8");
                Log.d(TAG, "logout: URL: " + indirizzo);
                URL url = new URL(indirizzo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "logout: Codice di risposta: " + responseCode);

                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    Log.d(TAG, "logout: Risposta del server: " + sb);
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
                Log.e(TAG, "logout: IOException", e);
            }
        });
    }

    private void onSuccess() {
        Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                Toast.makeText(getActivity(), "Operazione avvenuta con successo...", Toast.LENGTH_LONG).show());
                startActivity(new Intent(requireActivity(), MainActivity.class));
    }

    private void onCatchError(int errorCode) {
        Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                Toast.makeText(getActivity(), "Errore durante l'operazione (Codice: " + errorCode + ")", Toast.LENGTH_LONG).show());
    }

    private void showVersionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        builder.setMessage("Versione: 1.0")
                .setPositiveButton("Chiudi", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void showPrivacyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        builder.setTitle("Informativa Privacy")
                .setMessage("Questa informativa sulla privacy descrive come raccogliamo, utilizziamo e proteggiamo le informazioni che ci fornisci quando utilizzi i nostri servizi.\n" +
                        "\n" +
                        "Raccolta delle Informazioni: Raccogliamo informazioni limitate e necessarie per fornirti i nostri servizi. Queste informazioni potrebbero includere dati personali come nome, email e informazioni di contatto.\n" +
                        "\n" +
                        "Utilizzo delle Informazioni: Utilizziamo le informazioni raccolte esclusivamente per migliorare e personalizzare la tua esperienza con i nostri servizi. Questi dati non verranno condivisi con terze parti senza il tuo consenso.\n" +
                        "\n" +
                        "Protezione dei Dati: Abbiamo implementato misure di sicurezza per proteggere le tue informazioni personali e garantirne la riservatezza.\n" +
                        "\n" +
                        "Aggiornamenti della Privacy: Possiamo apportare modifiche a questa politica sulla privacy e ti consigliamo di consultarla periodicamente per essere informato su eventuali modifiche.")
                .setPositiveButton("Chiudi", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private int getUserId() {
        SharedPreferences shared = requireActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        return shared.getInt("idUser", -1);
    }

    private String getToken(){
        SharedPreferences shared = requireActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        return shared.getString("token", "");
    }
}