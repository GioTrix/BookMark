package it.uniroma2.pjdm.bookapp.ui.login;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import it.uniroma2.pjdm.bookapp.R;
import it.uniroma2.pjdm.bookapp.activities.HomeActivity;

public class LoginFragment extends Fragment {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private static final String TAG = "GT_TAG";
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button btReg = view.findViewById(R.id.bt_login_signup);
        Button btAccedi = view.findViewById(R.id.bt_login_home);
        etEmail = view.findViewById(R.id.etl_email);
        etPassword = view.findViewById(R.id.etl_password);

        btReg.setOnClickListener(v -> NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_loginFragment_to_registerFragment));

        btAccedi.setOnClickListener(v -> {
            login(Objects.requireNonNull(etEmail.getText()).toString(), Objects.requireNonNull(etPassword.getText()).toString());
            Toast.makeText(getActivity(), "Accesso in corso...", Toast.LENGTH_SHORT).show();
        });
    }

    public void login(String email, String password) {
        executor.execute(() -> {
            String indirizzo = getString(R.string.url_servlet) + "/LoginServlet";
            try {
                URL url = new URL(indirizzo);
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("POST");

                Map < String, String > params = new LinkedHashMap < > ();
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
                http.getOutputStream().write(postDataBytes);

                int code = http.getResponseCode();

                if (code == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader((http.getInputStream())));
                    StringBuilder sb = new StringBuilder();
                    String output;

                    while ((output = br.readLine()) != null) {
                        sb.append(output);
                    }

                    JSONObject jsonObject = new JSONObject(sb.toString());
                    Log.d(TAG, "Email ricevuta: " + jsonObject.getString("email"));
                    Log.d(TAG, "ID utente ricevuto: " + jsonObject.getInt("idUser"));
                    SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MySharedPref", MODE_PRIVATE);
                    SharedPreferences.Editor edt = sharedPreferences.edit();
                    Log.d(TAG, "Salvataggio nelle SharedPreferences...");
                    edt.putString("email", jsonObject.getString("email"));
                    edt.putInt("idUser", jsonObject.getInt("idUser"));
                    edt.putString("token", jsonObject.getString("token"));
                    edt.apply();

                    startActivity(new Intent(requireActivity(), HomeActivity.class));
                } else {
                    Log.e(TAG, "HTTP error code: " + code);
                }

            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException | JSONException e) {
                Log.e(TAG, "IOException or JSONException: " + e.getMessage());
                onCatchError();
                e.printStackTrace();
            }
        });
    }

    public void onCatchError() {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> Toast.makeText(getActivity(), "Credenziali errate", Toast.LENGTH_SHORT).show());
    }
}