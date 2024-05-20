package com.mariana.androidhifam;

import com.google.api.client.extensions.android.http.AndroidHttp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.mariana.androidhifam.databinding.ActivityMainBinding;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import driveapi.DatosArchivo;
import driveapi.DriveServiceHelper;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, DriveServiceHelper.DriveServiceInitialization {

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private Integer idUsuario;
    private SwipeToRefreshLayout refreshLayout;
    private DriveServiceHelper driveServiceHelper;
    private CCAlbumFamiliar cliente;
    private ArrayList<File> imagenes;


    public interface SwipeToRefreshLayout {
        void onSwipeToRefresh();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        // Configuración del binding
        cliente = new CCAlbumFamiliar();
        imagenes = new ArrayList<>();
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

        // Arrastrar para refrescar
        binding.refreshLayout.setOnRefreshListener(this);

        // Inicializar la api de Google Drive
        setDriveService();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return  navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public void onRefresh() {
        if (null != refreshLayout) {
            refreshLayout.onSwipeToRefresh();
        }
        binding.refreshLayout.setRefreshing(false);
    }

    @Override
    public void initializeDriveService() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            InputStream is = getResources().openRawResource(R.raw.credentials);
            GoogleCredential credential;
            try {
                credential = GoogleCredential.fromStream(is)
                        .createScoped(Collections.singleton(DriveScopes.DRIVE));

                driveServiceHelper = new DriveServiceHelper(new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                        .setApplicationName("HiFam!")
                        .build(), this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();
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
    public CCAlbumFamiliar getCliente() {
        return cliente;
    }
    public ArrayList<File> getImagenes() {
        return imagenes;
    }

    public DriveServiceHelper getDriveServiceHelper() {
        return driveServiceHelper;
    }

    public void setRefreshLayout(SwipeToRefreshLayout refreshLayout){
        this.refreshLayout = refreshLayout;
    }

    private void setDriveService() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                initializeDriveService();
            } catch (Exception e) {
                e.printStackTrace(); // Handle exceptions appropriately
            }
        });
        executor.shutdown();
    }

    // Métodos comunes
    public void cargarImagenesDrive() {
        try {
            LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
            String queryGruposActivos = "(uig.COD_USUARIO = " + idUsuario + " and g.FECHA_ELIMINACION is null)";
            String queryGruposEliminados = " or (g.COD_USUARIO_ADMIN_GRUPO = " + idUsuario + " and g.FECHA_ELIMINACION is not null)";
            filtros.put(queryGruposActivos, queryGruposEliminados);
            LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
            ordenacion.put("g.titulo", "asc");
            ArrayList<Grupo> grupos = cliente.leerGrupos(filtros,ordenacion);
            File directorio = getExternalFilesDir(null);
            if (null != directorio) {
                eliminarContenido(directorio);
            }
            for (Grupo grupo : grupos) {
                driveServiceHelper.listFiles(grupo.getCodGrupo());
            }
        } catch (ExcepcionAlbumFamiliar e) {
            throw new RuntimeException(e);
        }
        getAllImagesFromAppDirectory();
    }

    public static void eliminarContenido(File directorio) {
        if (directorio.isDirectory()) {
            File[] files = directorio.listFiles();
            if (files != null) {
                for (File child : files) {
                    eliminarContenido(child);
                }
            }
        }
        directorio.delete();
    }

    private void getAllImagesFromAppDirectory() {
        imagenes = new ArrayList<>();
        // Get the directory where your app stores files
        File directory = getExternalFilesDir(null); // or use getExternalFilesDir(null) for external storage
        // Get all files in the directory
        File[] files = directory.listFiles();
        // Check if the directory is not empty
        if (files != null) {
            for (File file : files) {
                // Check if the file is an image by checking its extension
                if (file.isFile() && isImageFile(file)) {
                    imagenes.add(file);
                }
            }
        }
    }

    private boolean isImageFile(File file) {
        // Check the file extension to determine if it's an image
        String[] imageExtensions = {".jpg", ".jpeg", ".png"};
        String fileName = file.getName().toLowerCase();

        for (String extension : imageExtensions) {
            if (fileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
}