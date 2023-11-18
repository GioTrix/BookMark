package it.uniroma2.pjdm.bookapp.ui.home;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import it.uniroma2.pjdm.bookapp.R;
public class BookDetail extends Fragment {
    private static final String ARG_PARAM1 = "titolo";
    private static final String ARG_PARAM2 = "autore";
    private static final String ARG_PARAM3 = "url";

    private static final String ARG_PARAM4 = "idLibro";

    private TextView tvPubDetTitle;
    private TextView tvPlotDetTitle;

    private static final String TAG = "GT_TAG";
    private final Executor executor = Executors.newSingleThreadExecutor();

    private String mParam1;
    private String mParam2;
    private String mParam3;
    private int idBook;

    public BookDetail() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mParam3 = getArguments().getString(ARG_PARAM3);
            idBook = getArguments().getInt(ARG_PARAM4);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_book_detail, container, false);

        TextView titleDET = view.findViewById(R.id.tv_det_titolo);
        titleDET.setText(mParam1);
        TextView authorDET = view.findViewById(R.id.tv_det_autore);
        authorDET.setText(mParam2);
        ImageView urlImageANN = view.findViewById(R.id.iv_urlImage_det);
        Glide.with(requireContext()).load(mParam3).into(urlImageANN);

        tvPubDetTitle = view.findViewById(R.id.tv_pub_det);
        tvPlotDetTitle = view.findViewById(R.id.tv_plot_det);

        getBookDetail();

        AppCompatButton btDetailHome = view.findViewById(R.id.bt_back_to_home);
        btDetailHome.setOnClickListener(v -> {
            Fragment fragment = new HomeFragment();
            FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.nav_host_fragment_home_main, fragment, "home");
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        return view;
    }

    private void getBookDetail() {
        executor.execute(() -> {
            try {
                String indirizzo = getString(R.string.url_servlet) + "/DetailBookServlet?idBook=" + idBook;
                Log.d(TAG, "getBookDetail: URL: " + indirizzo);

                URL url = new URL(indirizzo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder responseStringBuilder = new StringBuilder();
                    String line;

                    while ((line = br.readLine()) != null) {
                        responseStringBuilder.append(line);
                    }

                    String jsonString = responseStringBuilder.toString();
                    Log.d(TAG, "getBookDetail: Response: " + jsonString);

                    if (!jsonString.isEmpty()) {
                        JSONArray jsonArray = new JSONArray(jsonString);

                        if (jsonArray.length() > 0) {
                            JSONObject jsonObject = jsonArray.getJSONObject(0);

                            int anno = jsonObject.getInt("annoPubblicazione");
                            String descrizione = jsonObject.getString("descrizione");

                            requireActivity().runOnUiThread(() -> {
                                tvPubDetTitle.setText(String.valueOf(anno));
                                tvPlotDetTitle.setText(descrizione);
                            });
                        } else {
                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Nessun dettaglio del libro trovato", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "La risposta del server è vuota o non valida", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    requireActivity().runOnUiThread(() -> {
                        try {
                            Log.e(TAG, "getBookDetail: HTTP Error - " + conn.getResponseCode());
                            Toast.makeText(getContext(), "Errore nella risposta del server: " + conn.getResponseCode(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

                conn.disconnect();
            } catch (IOException | JSONException e) {
                Log.e(TAG, "getBookDetail: Exception - " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}