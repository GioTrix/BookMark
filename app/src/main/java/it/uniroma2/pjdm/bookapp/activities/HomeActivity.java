package it.uniroma2.pjdm.bookapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.os.Bundle;

import it.uniroma2.pjdm.bookapp.R;
import it.uniroma2.pjdm.bookapp.databinding.ActivityHomeBinding;
import it.uniroma2.pjdm.bookapp.ui.favorites.FavoritesFragment;
import it.uniroma2.pjdm.bookapp.ui.home.HomeFragment;
import it.uniroma2.pjdm.bookapp.ui.settings.SettingsFragment;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.navView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    replaceFragment(new HomeFragment());
                    break;
                case R.id.nav_favorites:
                    replaceFragment(new FavoritesFragment());
                    break;
                case R.id.nav_settings:
                    replaceFragment(new SettingsFragment());
                    break;
                default:
                    return false;
            }
            return true;
        });
        replaceFragment(new HomeFragment());
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment_home_main, fragment);
        fragmentTransaction.commit();
    }
}