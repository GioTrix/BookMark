package it.uniroma2.pjdm.bookapp.ui.register;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

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
public class RegisterFragment extends Fragment {
    private static final String TAG = "GT_TAG";
    private final Executor executor = Executors.newSingleThreadExecutor();
    private TextInputLayout etUsername;
    private TextInputLayout etEmail;
    private TextInputLayout etPassword;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        etUsername = view.findViewById(R.id.etr_username);
        etEmail = view.findViewById(R.id.etr_email);
        etPassword = view.findViewById(R.id.etr_password);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        Button btGoLogin = view.findViewById(R.id.bt_login);
        btGoLogin.setOnClickListener(v -> navController.navigate(R.id.action_registerFragment_to_loginFragment));

        Button btSignup = view.findViewById(R.id.bt_signup);
        btSignup.setOnClickListener(v -> {
            String username = Objects.requireNonNull(etUsername.getEditText()).getText().toString();
            String email = Objects.requireNonNull(etEmail.getEditText()).getText().toString();
            String password = Objects.requireNonNull(etPassword.getEditText()).getText().toString();

            signup(username, email, password);
            Toast.makeText(getActivity(), "Registrato", Toast.LENGTH_LONG).show();
        });
    }

    public void signup(String username, String email, String password) {
        executor.execute(() -> {
            String indirizzo = getString(R.string.url_servlet) + "/SignupServlet";
            Log.d(TAG, "signup: Indirizzo: " + indirizzo);

            HttpURLConnection http = null;
            try {
                URL url = new URL(indirizzo);
                http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("POST");

                Map < String, String > params = new LinkedHashMap < > ();
                params.put("username", username);
                params.put("email", email);
                params.put("password", password);

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
                Log.d(TAG, "signup: Codice di risposta: " + code);

                if (code == HttpURLConnection.HTTP_OK) {
                    // Successo
                    Log.d(TAG, "signup: Successo");
                } else {
                    // Errore
                    Log.e(TAG, "signup: Errore, codice " + code);

                    // Aggiungiamo la lettura della risposta del server (se disponibile)
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(http.getErrorStream()))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        Log.e(TAG, "signup: Messaggio di errore del server: " + sb);
                    } catch (IOException e) {
                        Log.e(TAG, "signup: Errore nella lettura del messaggio di errore del server", e);
                    }
                }

            } catch (IOException e) {
                Log.e(TAG, "signup: IOException", e);
            } finally {
                if (http != null) {
                    http.disconnect();
                }
            }
        });
    }

}