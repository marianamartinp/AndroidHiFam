package com.mariana.androidhifam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.mariana.androidhifam.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private Integer idUsuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        // Configuración del binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        binding.setLifecycleOwner(this);
        View root = binding.getRoot();
        setContentView(root);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        navController = navHostFragment.getNavController();
        setSupportActionBar(binding.toolbar);
        View customView = LayoutInflater.from(this).inflate(R.layout.fragment_menu, binding.toolbar, false);
        binding.toolbar.addView(customView);
        binding.toolbar.setTitle(null);

        //Configuracion appBar
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.loginFragment,
                R.id.gruposFragment
        ).setOpenableLayout(binding.drawerLayout).build();

        //ToolBar
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
              @Override
              public void onDestinationChanged(@NonNull NavController navController, @NonNull NavDestination navDestination, @Nullable Bundle bundle) {
                  if(navDestination.getId() == R.id.loginFragment || navDestination.getId() == R.id.registroFragment) {
                      mostrarToolbar(false);
                  }
              }
        });

        // Drawer Layout
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Botón de navegción atrás personalizado
        getOnBackPressedDispatcher().addCallback(binding.getLifecycleOwner(), callback);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return  navController.navigateUp() || super.onSupportNavigateUp();

    }

    public void mostrarToolbar(Boolean mostrar) {
        if (mostrar) {
            Animation animacionEntrada = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
            binding.toolbar.startAnimation(animacionEntrada);
            binding.toolbar.setVisibility(View.VISIBLE);
        }
        else {
            Animation animacionSalida = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
            animacionSalida.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    binding.toolbar.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            binding.toolbar.startAnimation(animacionSalida);
        }
    }

    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if(binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            }
            else if (navController.getCurrentDestination().getId() != R.id.gruposFragment) {
                navController.popBackStack();
            }
            else {
                this.setEnabled(false);
            }
            this.setEnabled(true);

        }

    };

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }
}