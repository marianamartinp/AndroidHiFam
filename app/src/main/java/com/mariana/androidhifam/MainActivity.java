package com.mariana.androidhifam;

import com.google.android.material.navigation.NavigationView;
import com.google.api.client.extensions.android.http.AndroidHttp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import driveapi.DatosArchivo;
import driveapi.DriveServiceHelper;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import pojosalbumfamiliar.Grupo;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, DriveServiceHelper.DriveServiceInitialization, NavigationView.OnNavigationItemSelectedListener {

    // Constantes para las preferencias compartidas (SharedPreferences)
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_TOKEN = "userToken";

    // Variables para manejar la navegación y la configuración de la barra de la aplicación (AppBar)
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private @NonNull ActivityMainBinding binding;
    private SwipeToRefreshLayout refreshLayout;
    private DriveServiceHelper driveServiceHelper;
    private CCAlbumFamiliar cliente;
    private ArrayList<File> imagenes;
    private ArrayList<Grupo> grupos;
    // Variable para habilitar o deshabilitar la interacción del usuario
    private boolean habilitarInteraccion = true;

    // Interfaz para el layout de refresco al deslizar hacia abajo
    public interface SwipeToRefreshLayout {
        void onSwipeToRefresh(SwipeRefreshLayout refreshLayout);
    }

    // Método que se ejecuta al crear el fragmento
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Establecer el idioma de la aplicación a español
        setLocale(this,"es");
        super.onCreate(savedInstanceState);
        // Inflar el layout principal y establecerlo como la vista de contenido
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        binding.setLifecycleOwner(this);
        View root = binding.getRoot();
        setContentView(root);

        // Verificar si hay conexión a Internet
        if (internetDisponible()) {
            // Setup del NavHostFragment
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
            navController = navHostFragment.getNavController();

            // Inicializar el cliente y la lista de imágenes
            cliente = new CCAlbumFamiliar();
            imagenes = new ArrayList<>();

            // Configurar la barra de herramientas (Toolbar)
            View customView = LayoutInflater.from(this).inflate(R.layout.fragment_menu, binding.toolbar, false);
            binding.toolbar.addView(customView);
            binding.toolbar.setNavigationIcon(null);
            setSupportActionBar(binding.toolbar);
            binding.toolbar.setTitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);


            //Configuracion appBar
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.loginFragment,
                    R.id.registroFragment,
                    R.id.modificarPublicacion,
                    R.id.gruposFragment,
                    R.id.gruposRecuperablesFragment,
                    R.id.albumesRecuperablesFragment,
                    R.id.detallesGrupoFragment,
                    R.id.albumesFragment,
                    R.id.publicacionesFragment,
                    R.id.publicacionesListaFragment,
                    R.id.publicacionFragment,
                    R.id.nuevaPublicacionFragment,
                    R.id.nuevoGrupoFragment,
                    R.id.nuevoGrupoFragment,
                    R.id.detallesUsuarioFragment,
                    R.id.detallesAlbumFragment
            ).setOpenableLayout(binding.drawerLayout).build();

            //ToolBar
            // Configurar la barra de herramientas (Toolbar) con la navegación
            NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration);

            // Escuchar cambios en el destino de la navegación para mostrar/ocultar la barra de herramientas
            navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
                @Override
                public void onDestinationChanged(@NonNull NavController navController, @NonNull NavDestination navDestination, @Nullable Bundle bundle) {
                    if (navDestination.getId() == R.id.registroFragment) {
                        mostrarToolbar(false, false, false);
                    }
                }
            });

            // Drawer Layout
            // Configurar el NavigationView con el NavController
            NavigationUI.setupWithNavController(binding.navView, navController);
            binding.navView.setNavigationItemSelectedListener(this);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, binding.drawerLayout, binding.toolbar, R.string.drawerAbierto, R.string.drawerCerrado);
            binding.drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            // Botón de navegción atrás personalizado
            getOnBackPressedDispatcher().addCallback(binding.getLifecycleOwner(), callback);

            // Arrastrar para refrescar
            binding.refreshLayout.setOnRefreshListener(this);

            // Inicializar la api de Google Drive
            setDriveService();

            // Setup SharedPreferences para el login.
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean usuarioLoggeado = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);

            // Navegar a la pantalla adecuada según el estado de login del usuario
            if (usuarioLoggeado) {
                navController.navigate(R.id.gruposFragment);
            } else {
                navController.navigate(R.id.loginFragment);
            }
        }
        else {
            // Mostrar un mensaje si no hay conexión a Internet y cerrar la aplicación
            Toast.makeText(getApplicationContext(), "Conexión a internet no disponible.", Toast.LENGTH_SHORT).show();
            finish();
//            ModalFragment modal = new ModalFragment(position, (int) id, this, "¿Desea recuperar este grupo?", getString(R.string.btnRecuperar), getString(R.string.btnCancelar));
//            modal.show(getActivity().getSupportFragmentManager(), "modalRecuperarGrupo");
        }
    }

    // Manejar la navegación hacia arriba
    @Override
    public boolean onSupportNavigateUp() {
        if (habilitarInteraccion) {
            return navController.navigateUp() || super.onSupportNavigateUp();
        }
        return false;
    }

    // Manejar el refresco de la vista al deslizar hacia abajo
    @Override
    public void onRefresh() {
        if (null != refreshLayout) {
            refreshLayout.onSwipeToRefresh(binding.refreshLayout);
        }
    }

    // Inicializar el servicio de Google Drive en un hilo separado
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
    }

    // Preparar el menú de opciones (opcional)
    @Override
    public boolean onPrepareOptionsMenu (Menu menu){
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    // Manejar la selección de elementos del menú de navegación
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Cerrar menú y ejecutar una acción
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        if (habilitarInteraccion) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    int id = item.getItemId();
                    if (id == R.id.cerrarSesionItem) {
                        cerrarSesion();
                    } else if (id == R.id.usuarioItem) {
                        navController.navigate(R.id.detallesUsuarioFragment);
                    } else if (id == R.id.gruposItem) {
                        navController.navigate(R.id.gruposFragment);
                    }
                }
            }, 500);
        }
        return true;
    }

    // Mostrar u ocultar la barra de herramientas con o sin animación
    public void mostrarToolbar(boolean mostrar, boolean animar, boolean sentidoAvanzar) {
        if (mostrar) {
            if (animar) {
                Animation animacionEntrada = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
                animacionEntrada.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        // No precisado
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        findViewById(R.id.toolbarContenido).setVisibility(View.VISIBLE);
                        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // No precisado
                    }
                });
                binding.toolbar.startAnimation(animacionEntrada);
            }
            else {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                findViewById(R.id.toolbarContenido).setVisibility(View.VISIBLE);
            }
        }
        else {
            if (animar) {
                Animation animacionSalida;
                if (sentidoAvanzar) {
                    animacionSalida = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
                }
                else {
                    animacionSalida = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
                }
                animacionSalida.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        // No precisado
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        findViewById(R.id.toolbarContenido).setVisibility(View.INVISIBLE);
                        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        binding.toolbar.setNavigationIcon(null);
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // No precisado
                    }
                });
                binding.toolbar.startAnimation(animacionSalida);
            }
            else {
                findViewById(R.id.toolbarContenido).setVisibility(View.INVISIBLE);
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                binding.toolbar.setNavigationIcon(null);
            }
        }
    }

    // Manejar el botón de retroceso
    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (habilitarInteraccion) {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                } else if (navController.getCurrentDestination().getId() != R.id.gruposFragment) {
                    navController.popBackStack();
                } else {
                    this.setEnabled(false);
                }
                this.setEnabled(true);
            }
        }
    };

    // Obtener la instancia del cliente de comunicaciones desde otros fragments
    public CCAlbumFamiliar getCliente() {
        return cliente;
    }

    // Habilitar o deshabilitar la interacción del usuario
    public void setHabilitarInteraccion(boolean habilitarInteraccion) {
        this.habilitarInteraccion = habilitarInteraccion;
        if (habilitarInteraccion) {
            binding.drawerLayout.setClickable(true);
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
        else {
            binding.drawerLayout.setClickable(false);
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    // Método para obtener el estado de habilitación de la interacción desde otros fragments
    public boolean getHabilitarInteraccion() {
        return habilitarInteraccion;
    }

    // Método para obtener la lista de imágenes del directorio de la aplicación
    public ArrayList<File> getImagenes() {
        getImagenesDelAppDirectory();
        return imagenes;
    }

    // Método para obtener el servicio de conexión con Google Drive DriveServiceHelper
    public DriveServiceHelper getDriveServiceHelper() {
        return driveServiceHelper;
    }

    // Método para establecer el SwipeRefreshLayout
    public void setRefreshLayout(SwipeToRefreshLayout refreshLayout){
        this.refreshLayout = refreshLayout;
    }

    // Método para inicializar el servicio de Google Drive
    private void setDriveService() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                initializeDriveService();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // Métodos comunes
    public void cargarImagenesDrive(boolean cargarGrupos) {
        try {
            if (cargarGrupos) {
                LinkedHashMap<String, String> filtros = new LinkedHashMap<>();
                String queryGruposActivos = "(uig.COD_USUARIO = " + getToken() + " and g.FECHA_ELIMINACION is null)";
                String queryGruposEliminados = " or (g.COD_USUARIO_ADMIN_GRUPO = " + getToken() + " and g.FECHA_ELIMINACION is not null)";
                filtros.put(queryGruposActivos, queryGruposEliminados);
                LinkedHashMap<String, String> ordenacion = new LinkedHashMap<>();
                ordenacion.put("g.titulo", "asc");
                grupos = cliente.leerGrupos(filtros, ordenacion);
            }
            File directorio = getExternalFilesDir(null);
            if (null != directorio) {
                eliminarContenido(directorio);
            }
            for (Grupo grupo : grupos) {
                driveServiceHelper.listFiles(grupo.getCodGrupo());
            }
        } catch (ExcepcionAlbumFamiliar e) {
            Toast.makeText(this, e.getMensajeUsuario(), Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Toast.makeText(this, "Error al conectar con Google Drive.", Toast.LENGTH_SHORT).show();
        }
    }

    private static void eliminarContenido(File directorio) {
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

    // Método para obtener todas las imágenes del directorio de archivos de la aplicación
    private void getImagenesDelAppDirectory() {
        imagenes = new ArrayList<>();
        // Obtiene el directorio donde la aplicación almacena los archivos
        File directory = getExternalFilesDir(null);
        // Obtiene todos los archivos en el directorio
        File[] files = directory.listFiles();
        // Verifica si el directorio no está vacío
        if (files != null) {
            for (File file : files) {
                // Verifica si el archivo es una imagen comprobando su extensión y la agrega a la lista de imágenes
                if (file.isFile() && esImagen(file)) {
                    imagenes.add(file);
                }
            }
        }
    }

    // Método para verificar si un archivo es una imagen basándose en su extensión de archivo
    private boolean esImagen(File file) {
        // Extensiones de archivo de imágenes admitidas
        String[] imageExtensions = {".jpg", ".jpeg", ".png"};
        // Obtiene el nombre del archivo y lo convierte a minúsculas para una comparación sin distinción entre mayúsculas y minúsculas
        String fileName = file.getName().toLowerCase();
        // Si el nombre del archivo coincide con alguna de las extensiones de imagen, devuelve true
        for (String extension : imageExtensions) {
            if (fileName.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    // Verificar si hay conexión a Internet
    private boolean internetDisponible() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    // Cambiar el idioma de la aplicación
    private static void setLocale(Activity activity, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    // Método para obtener el token (id) del usuario desde las preferencias compartidas
    public String getToken() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Devuelve el valor almacenado en SharedPreferences con la clave KEY_USER_TOKEN, o null si no existe
        return sharedPreferences.getString(KEY_USER_TOKEN, null);
    }

    // Cerrar sesión del usuario y navegar a la pantalla de login
    public void cerrarSesion() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        List<Fragment> listaFragments = getSupportFragmentManager().findFragmentById((R.id.navHostFragment)).getChildFragmentManager().getFragments();
        Fragment fragmentActual = listaFragments.get(listaFragments.size() - 1);
        if (fragmentActual instanceof GruposFragment) {
            navController.navigate(GruposFragmentDirections.actionGruposFragmentToLoginFragment());
        }
        else if (fragmentActual instanceof DetallesUsuarioFragment) {
            navController.navigate(DetallesUsuarioFragmentDirections.actionDetallesUsuarioFragmentToLoginFragment());
        }
        else {
            navController.navigate(R.id.loginFragment);
        }
    }
}