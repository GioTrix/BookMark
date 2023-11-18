package it.uniroma2.pjdm.bookapp.ui.starter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class StartFragment extends Fragment {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private static final String TAG = "GT_TAG";
    public StartFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        Button button = view.findViewById(R.id.bt_start);
        button.setOnClickListener(v -> {
            if (getToken() == null) {
                navController.navigate(R.id.action_startFragment_to_loginFragment);
            } else {
                checkToken(view);
            }

        });

    }

    private String getToken() {
        SharedPreferences shared = requireActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        return shared.getString("token", null);
    }

    private void clearToken() {
        SharedPreferences shared = requireActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor edt = shared.edit();
        edt.clear().apply();
    }

    private void checkToken(View view) {
        executor.execute(() -> {
            String token = getToken();
            String indirizzo = getString(R.string.url_servlet) + "/CheckToken";
            try {
                URL url = new URL(indirizzo);
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("POST");

                Map < String, String > params = new LinkedHashMap < > ();
                params.put("token", token);

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
                    Log.d(TAG, "Token ricevuto: " + jsonObject.getString("token"));

                    startActivity(new Intent(requireActivity(), HomeActivity.class));
                } else {
                    Log.e(TAG, "HTTP error code: " + code);
                }

            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException | JSONException e) {
                Log.e(TAG, "IOException or JSONException: " + e.getMessage());
                onCatchError(view);
                e.printStackTrace();
            }
        });
    }
    public void onCatchError(View view) {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            Toast.makeText(getActivity(), "Sessione scaduta", Toast.LENGTH_SHORT).show();
            clearToken();
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_startFragment_to_loginFragment);
        });
    }
}