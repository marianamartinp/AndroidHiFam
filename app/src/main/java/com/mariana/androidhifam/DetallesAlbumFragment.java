package com.mariana.androidhifam;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.mariana.androidhifam.databinding.FragmentDetallesAlbumBinding;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ccalbumfamiliar.CCAlbumFamiliar;
import pojosalbumfamiliar.Album;
import pojosalbumfamiliar.ExcepcionAlbumFamiliar;
import utils.Utils;

public class DetallesAlbumFragment extends Fragment implements View.OnClickListener {
    private @NonNull DetallesAlbumFragmentArgs detallesAlbumFragmentArgs;
    private @NonNull FragmentDetallesAlbumBinding binding;
    private CCAlbumFamiliar cliente;
    private MainActivity activity;
    private ExecutorService executorService;
    private Handler mainHandler;
    private NavController navController;
    private Integer idAlbum, tokenUsuario;
    private Album album;

    // Método llamado al crear la instancia del Fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtiene los argumentos pasados al Fragment
        if (getArguments() != null) {
            detallesAlbumFragmentArgs = DetallesAlbumFragmentArgs.fromBundle(getArguments());
            idAlbum = detallesAlbumFragmentArgs.getIdAlbum();
        }
        activity = (MainActivity) getActivity();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // Método llamado para crear la vista del Fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity.findViewById(R.id.refreshLayout).setEnabled(false);
        // Infla el layout del Fragment y obtiene una instancia del binding
        binding = FragmentDetallesAlbumBinding.inflate(inflater, container, false);
        navController = NavHostFragment.findNavController(this);
        cliente = activity.getCliente();
        tokenUsuario = Integer.parseInt(activity.getToken());
        return binding.getRoot();
    }

    // Método llamado cuando la vista ha sido creada
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Carga los detalles del álbum
        cargarAlbum(idAlbum);
        // Configura el listener para el botón de atrás
        binding.botonAtras.setOnClickListener(this);
    }

    // Método llamado al destruir la vista del Fragment
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Método para manejar los eventos de clic en los botones
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.botonModificarAlbum) {
            modificarAlbum();
        }
        else if (id == R.id.botonAtras) {
            findNavController(v).popBackStack();
        }
    }

    // Método para cargar los detalles del álbum
    public void cargarAlbum(Integer idAlbum) {
        executorService.execute(() -> {
            try {
                album = cliente.leerAlbum(idAlbum);
                mainHandler.post(() -> {
                    if (null != album) {
                        revisarPermisos(tokenUsuario, album);
                        actualizarInterfaz();
                    }
                });
            } catch (ExcepcionAlbumFamiliar e) {
                mainHandler.post(() -> Toast.makeText(requireContext(), "Error al cargar el título del grupo.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Método para actualizar la interfaz con los detalles del álbum
    public void actualizarInterfaz() {
        binding.tituloAlbum.setText(album.getTitulo());
        binding.descripcionAlbum.setText(album.getDescripcion());
        binding.editextFechaCreacion.setText(Utils.parsearDateAString(album.getFechaCreacion()));
        binding.editextAdminGrupo.setText("@" + album.getUsuarioAdminAlbum().getUsuario());
        binding.editextGrupo.setText(album.getGrupoCreaAlbum().getTitulo());
        if (album.getTipo().equals("I")) {
            binding.radioButtonIndividual.setChecked(true);
        }
        else {
            binding.radioButtonColectivo.setChecked(true);
        }
    }

    // Método para revisar los permisos del usuario sobre el álbum y habilitar o deshabilitar la edición
    public void revisarPermisos(Integer tokenUsuario, Album album) {
        // Si el usuario es el administrador del álbum o del grupo, se habilita la edición
        if (Objects.equals(tokenUsuario, album.getUsuarioAdminAlbum().getCodUsuario())) {
            binding.botonModificarAlbum.setVisibility(View.VISIBLE);
            binding.botonModificarAlbum.setOnClickListener(this);
            binding.tituloAlbum.setEnabled(true);
            binding.tituloAlbum.setInputType(InputType.TYPE_CLASS_TEXT);
            binding.tituloAlbum.setTextColor(getResources().getColor(R.color.darkGrey, activity.getTheme()));
            binding.descripcionAlbum.setEnabled(true);
            binding.descripcionAlbum.setInputType(InputType.TYPE_CLASS_TEXT);
            binding.descripcionAlbum.setTextColor(getResources().getColor(R.color.darkGrey, activity.getTheme()));
            binding.radiogroup.setEnabled(false);
            binding.radioButtonColectivo.setEnabled(true);
            binding.radioButtonIndividual.setEnabled(true);
        }
        else {
            // Si el usuario no tiene permisos de edición, se deshabilita la edición
            binding.botonModificarAlbum.setVisibility(View.INVISIBLE);
            binding.tituloAlbum.setEnabled(false);
            binding.tituloAlbum.setInputType(InputType.TYPE_NULL);
            binding.tituloAlbum.setTextColor(getResources().getColor(R.color.mediumGrey, activity.getTheme()));
            binding.descripcionAlbum.setEnabled(false);
            binding.descripcionAlbum.setInputType(InputType.TYPE_NULL);
            binding.descripcionAlbum.setTextColor(getResources().getColor(R.color.mediumGrey, activity.getTheme()));
            binding.radiogroup.setEnabled(false);
            binding.radioButtonColectivo.setEnabled(false);
            binding.radioButtonIndividual.setEnabled(false);
        }
    }

    // Método para modificar el álbum
    public void modificarAlbum() {
        String tituloAlbum = binding.tituloAlbum.getText().toString().trim();
        String descripcionAlbum = binding.descripcionAlbum.getText().toString().trim();
        if (!tituloAlbum.isEmpty()) {
            // Crea un objeto Album con los nuevos datos
            Album albumModificado = new Album();
            albumModificado.setTitulo(tituloAlbum);
            albumModificado.setDescripcion(descripcionAlbum);
            albumModificado.setUsuarioAdminAlbum(album.getUsuarioAdminAlbum());
            albumModificado.setGrupoCreaAlbum(album.getGrupoCreaAlbum());
            if (binding.radioButtonColectivo.isChecked()){
                albumModificado.setTipo("C");
            }
            else {
                albumModificado.setTipo("I");
            }

            // Ejecuta la operación de modificar álbum en un hilo aparte
            executorService.execute(() -> {
                try {
                    cliente.modificarAlbum(album.getCodAlbum(), albumModificado);
                    mainHandler.post(() -> {
                        Toast.makeText(requireContext(), "El álbum se ha modificado correctamente.", Toast.LENGTH_SHORT).show();
                        navController.popBackStack();
                    });
                } catch (ExcepcionAlbumFamiliar e) {
                    String mensaje;
                    mensaje = e.getMensajeUsuario();
                    mainHandler.post(() -> Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show());
                }
            });
        }
        else {
            Toast.makeText(requireContext(), "El título del álbum es obligatorio.", Toast.LENGTH_SHORT).show();
        }
    }

}