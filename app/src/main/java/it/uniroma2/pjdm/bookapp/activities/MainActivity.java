package it.uniroma2.pjdm.bookapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import it.uniroma2.pjdm.bookapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}