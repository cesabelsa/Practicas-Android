package com.example.consumoapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.consumoapp.databinding.ActivityMainBinding;
import com.example.consumoapp.factura.SimuladorFacturaActivity;
import com.example.consumoapp.feature.comparador.ComparadorTarifasActivity;
import com.example.consumoapp.esios.ui.EsiosPagerAdapter;
import com.example.consumoapp.esios.network.EsiosRepository;
import com.example.consumoapp.esios.settings.EsiosPreferences;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pantalla principal de Electroconsumo.
 *
 * Mantiene el flujo original de la app y mejora la navegación:
 * - Menú lateral general para secciones grandes.
 * - Menú horizontal ESIOS para apartados internos.
 * - Toolbar con usuario conectado y acciones rápidas.
 */
public class MainActivity extends AppCompatActivity {

    // ViewBinding evita usar findViewById en la mayoría de vistas.
    private ActivityMainBinding binding;

    // Receiver dinámico para detectar cambios de conexión mientras la pantalla está abierta.
    private BroadcastReceiver networkReceiver;

    // Referencia a la acción de actualización para mostrarla solo en pantallas ESIOS útiles.
    private MenuItem itemActualizarEsios;

    // Ejecuta consultas Room fuera del hilo principal.
    private final ExecutorService executorEstado = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.estaLogueado(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Inflamos el layout con ViewBinding.
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Convertimos MaterialToolbar en ActionBar.
        setSupportActionBar(binding.toolbarMain);

        // El menú lateral general sustituye al popup simple del icono hamburguesa.
        configurarMenuLateralGeneral();
        configurarBotonAtras();

        // Configuramos el ViewPager2 con sus fragmentos ESIOS.
        configurarViewPager();

        // Estado inicial de título, subtítulo y selección visual.
        actualizarEstadoMenus(0);

        // Registramos eventos del sistema.
        registrarNetworkReceiver();
        actualizarEstadoEsiosVisible();

        // Si otra pantalla vuelve a MainActivity indicando una sección, la abrimos.
        aplicarDestinoInicial(getIntent());

        if (EsiosPreferences.actualizarAlIniciar(this) && hayConexionInternet()) {
            actualizarEsiosEnSegundoPlano();
        }
    }

    private void configurarMenuLateralGeneral() {

        // Icono hamburguesa en la Toolbar principal.
        binding.toolbarMain.setNavigationIcon(R.drawable.ic_menu_24);
        binding.toolbarMain.setNavigationContentDescription(R.string.descripcion_abrir_menu_principal);

        // Al pulsar el icono se abre el menú lateral.
        binding.toolbarMain.setNavigationOnClickListener(v -> binding.drawerLayoutMain.openDrawer(binding.navigationViewMain));

        // Cargamos en la cabecera del menú el usuario guardado en sesión.
        View headerView = binding.navigationViewMain.getHeaderView(0);
        TextView txtAvatar = headerView.findViewById(R.id.txtDrawerAvatar);
        TextView txtNombre = headerView.findViewById(R.id.txtDrawerNombre);
        TextView txtEmail = headerView.findViewById(R.id.txtDrawerEmail);

        if (SessionManager.estaLogueado(this)) {
            txtAvatar.setText(SessionManager.obtenerInicial(this));
            txtNombre.setText(SessionManager.obtenerNombre(this));
            txtEmail.setText(SessionManager.obtenerEmail(this));
        } else {
            txtAvatar.setText("U");
            txtNombre.setText("Usuario invitado");
            txtEmail.setText("Inicia sesión para guardar tus datos");
        }

        // MainActivity requiere sesión, por lo que ocultamos Login y Registro
        // y dejamos visible únicamente Cerrar sesión.
        NavigationUtils.configurarOpcionesSesion(this, binding.navigationViewMain.getMenu());

        // El menú lateral se usa para bloques grandes de la app.
        // Las pestañas horizontales quedan reservadas para secciones internas de ESIOS.
        binding.navigationViewMain.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            binding.drawerLayoutMain.closeDrawer(binding.navigationViewMain);

            if (itemId == R.id.action_nav_main) {
                binding.viewPagerEsios.setCurrentItem(0, true);
                actualizarEstadoMenus(0);
                return true;
            }

            if (itemId == R.id.action_nav_esios) {
                binding.viewPagerEsios.setCurrentItem(1, true);
                actualizarEstadoMenus(1);
                return true;
            }

            // El resto de destinos se resuelve desde un único punto.
            // Así el menú lateral y los menús de las toolbars secundarias
            // abren exactamente las mismas pantallas y con las mismas flags.
            return NavigationUtils.handleNavigation(this, itemId);
        });
    }

    /**
     * El botón Atrás cierra primero el menú lateral. Solo cuando el menú ya
     * está cerrado se aplica el comportamiento normal de Android.
     */
    private void configurarBotonAtras() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.drawerLayoutMain.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayoutMain.closeDrawer(GravityCompat.START);
                    return;
                }

                // Desactivamos temporalmente este callback para delegar el
                // siguiente paso al sistema y evitar una llamada recursiva.
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private void abrirSimuladorEnTab(int tab) {
        Intent intent = new Intent(this, SimuladorFacturaActivity.class);
        intent.putExtra(SimuladorFacturaActivity.EXTRA_TAB_INICIAL, tab);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        aplicarDestinoInicial(intent);
    }

    /** Aplica la pestaña ESIOS solicitada por NavigationUtils. */
    private void aplicarDestinoInicial(Intent intent) {
        if (intent == null || !intent.hasExtra(NavigationUtils.EXTRA_MAIN_TAB)) {
            return;
        }
        int tab = intent.getIntExtra(NavigationUtils.EXTRA_MAIN_TAB, 0);
        tab = Math.max(0, Math.min(2, tab));
        binding.viewPagerEsios.setCurrentItem(tab, false);
        actualizarEstadoMenus(tab);
    }

    private void configurarViewPager() {

        // Adapter que crea ResumenFragment, PreciosFragment y GraficosFragment.
        binding.viewPagerEsios.setAdapter(new EsiosPagerAdapter(this));

        // Une las pestañas con el ViewPager2.
        new TabLayoutMediator(binding.tabLayoutEsios, binding.viewPagerEsios,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull com.google.android.material.tabs.TabLayout.Tab tab, int position) {

                        // Menú horizontal ESIOS compacto:
                        // todas las pestañas conservan su icono y solo la pestaña activa
                        // mostrará también su nombre. Así se mantiene el ancho disponible
                        // y el usuario identifica con claridad la sección seleccionada.
                        String etiquetaVisible = obtenerEtiquetaVisiblePestana(position);
                        String etiquetaAccesible = obtenerEtiquetaPestana(position);
                        tab.setText(position == 0 ? etiquetaVisible : null);
                        tab.setContentDescription(etiquetaAccesible);

                        if (position == 0) {
                            tab.setIcon(R.drawable.ic_home);
                        } else if (position == 1) {
                            tab.setIcon(R.drawable.ic_euro);
                        } else {
                            tab.setIcon(R.drawable.ic_chart);
                        }
                    }
                }).attach();

        configurarTooltipsMenuEsios();
        actualizarEtiquetasTabs(binding.viewPagerEsios.getCurrentItem());

        // Al cambiar de pestaña actualizamos el subtítulo y la selección lateral.
        binding.viewPagerEsios.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                actualizarEtiquetasTabs(position);
                actualizarEstadoMenus(position);
            }
        });
    }



    /**
     * Muestra el texto únicamente en la pestaña seleccionada.
     * Las demás conservan solo el icono para que las cinco opciones sigan
     * entrando en el ancho de la pantalla sin recargar el menú.
     */
    private void actualizarEtiquetasTabs(int posicionSeleccionada) {
        for (int i = 0; i < binding.tabLayoutEsios.getTabCount(); i++) {
            com.google.android.material.tabs.TabLayout.Tab tab =
                    binding.tabLayoutEsios.getTabAt(i);

            if (tab != null) {
                String etiquetaVisible = obtenerEtiquetaVisiblePestana(i);
                String etiquetaAccesible = obtenerEtiquetaPestana(i);
                tab.setText(i == posicionSeleccionada ? etiquetaVisible : null);
                tab.setContentDescription(etiquetaAccesible);
            }
        }
    }

    private void configurarTooltipsMenuEsios() {

        // TabLayout muestra el nombre solo en la pestaña activa.
        // Este método mantiene además el tooltip y la descripción accesible
        // en las cinco opciones, incluidas las que muestran solo el icono.
        ViewGroup contenedorTabs = (ViewGroup) binding.tabLayoutEsios.getChildAt(0);

        if (contenedorTabs == null) {
            return;
        }

        for (int i = 0; i < contenedorTabs.getChildCount(); i++) {
            View tabView = contenedorTabs.getChildAt(i);
            String etiqueta = obtenerEtiquetaPestana(i);

            tabView.setContentDescription(etiqueta);
            ponerTooltip(tabView, etiqueta);
        }
    }

    /**
     * Pone texto accesible y tooltip sin depender de TooltipCompat.
     *
     * setTooltipText existe desde Android 8.0, por eso comprobamos la versión.
     * En Android 6 y 7 la app seguirá funcionando, pero sin tooltip visual.
     */
    private void ponerTooltip(View vista, String texto) {
        vista.setContentDescription(texto);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vista.setTooltipText(texto);
        }
    }

    /**
     * Devuelve una etiqueta corta para la pestaña activa.
     * Mantener textos breves evita cortes en pantallas pequeñas.
     */
    private String obtenerEtiquetaVisiblePestana(int posicionEsios) {
        if (posicionEsios == 0) {
            return getString(R.string.tab_esios_visible_resumen);
        }
        if (posicionEsios == 1) {
            return getString(R.string.tab_esios_visible_hoy);
        }
        return getString(R.string.tab_esios_visible_graficos);
    }

    private String obtenerEtiquetaPestana(int posicionEsios) {

        if (posicionEsios == 0) {
            return getString(R.string.tab_esios_resumen);
        }

        if (posicionEsios == 1) {
            return getString(R.string.tab_esios_precio_hoy);
        }

        return getString(R.string.tab_esios_graficos);
    }

    private void actualizarEstadoMenus(int posicionEsios) {

        // El subtítulo indica dónde está el usuario dentro de la Home ESIOS.
        binding.toolbarMain.setSubtitle(obtenerTituloPestana(posicionEsios));

        // El botón de actualización se muestra en Resumen, Precios y Gráficos.
        // En Ajustes se oculta porque esa pantalla no consulta precios ni estadísticas.
        actualizarVisibilidadAccionEsios(posicionEsios);

        // El Drawer marca solo el bloque grande activo.
        if (posicionEsios == 0) {
            binding.navigationViewMain.setCheckedItem(R.id.action_nav_main);
        } else {
            binding.navigationViewMain.setCheckedItem(R.id.action_nav_esios);
        }
    }


    /**
     * Controla si la acción de actualización tiene sentido en la pestaña abierta.
     * Las posiciones 0 a 2 trabajan con información ESIOS; la posición 3 son ajustes.
     */
    private void actualizarVisibilidadAccionEsios(int posicionEsios) {
        if (itemActualizarEsios == null) {
            return;
        }

        boolean mostrarActualizar = posicionEsios >= 0 && posicionEsios <= 2;
        itemActualizarEsios.setVisible(mostrarActualizar);
        itemActualizarEsios.setEnabled(mostrarActualizar);
    }

    private String obtenerTituloPestana(int posicionEsios) {

        // Los textos se guardan en strings.xml para mantenerlos centralizados
        // y facilitar una futura traducción de la aplicación.
        if (posicionEsios == 0) {
            return getString(R.string.toolbar_subtitulo_resumen);
        }

        if (posicionEsios == 1) {
            return getString(R.string.toolbar_subtitulo_precio_horas);
        }

        return getString(R.string.toolbar_subtitulo_graficos);
    }

    private void actualizarEsiosEnSegundoPlano() {
        new EsiosRepository(getApplicationContext()).descargarPreciosHoy(
                new EsiosRepository.EsiosRepositoryCallback() {
                    @Override public void onSuccess(java.util.List<com.example.consumoapp.esios.data.PrecioLuzEntity> precios) {
                        mainHandler.post(MainActivity.this::actualizarEstadoEsiosVisible);
                    }
                    @Override public void onError(String mensaje) {
                        mainHandler.post(MainActivity.this::actualizarEstadoEsiosVisible);
                    }
                });
    }

    private void registrarNetworkReceiver() {

        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // Actualizamos el indicador persistente en vez de depender solo de un Toast.
                actualizarEstadoEsiosVisible();
            }
        };

        // Registro dinámico recomendado para CONNECTIVITY_ACTION.
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private boolean hayConexionInternet() {

        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (manager == null) {
            return false;
        }

        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    /**
     * Consulta la caché Room en segundo plano y muestra un estado claro:
     * conexión disponible, modo sin conexión y fecha de la última descarga real.
     */
    private void actualizarEstadoEsiosVisible() {
        final boolean conectado = hayConexionInternet();

        executorEstado.execute(() -> {
            Long ultimaDescarga = com.example.consumoapp.esios.data.AppDatabase
                    .getInstance(getApplicationContext())
                    .precioLuzDao()
                    .obtenerUltimaFechaDescarga();

            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (ultimaDescarga == null || ultimaDescarga <= 0L) {
                    binding.txtEstadoEsios.setText(conectado
                            ? R.string.estado_esios_online_sin_datos
                            : R.string.estado_esios_offline_sin_datos);
                    return;
                }

                String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(new Date(ultimaDescarga));

                binding.txtEstadoEsios.setText(getString(
                        conectado
                                ? R.string.estado_esios_online_con_fecha
                                : R.string.estado_esios_offline_con_fecha,
                        fecha));
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarEstadoEsiosVisible();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // La Toolbar conserva acciones rápidas y una ayuda contextual de la sección abierta.
        getMenuInflater().inflate(R.menu.menu_esios, menu);

        // Guardamos la referencia para ocultar Actualizar dentro de Ajustes ESIOS.
        itemActualizarEsios = menu.findItem(R.id.action_actualizar_esios);
        actualizarVisibilidadAccionEsios(binding.viewPagerEsios.getCurrentItem());

        // Muestra en la Toolbar el usuario conectado, si existe sesión activa.
        NavigationUtils.prepararMenuUsuario(this, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_actualizar_esios) {

            // Sin Internet no se solicita una descarga nueva: se mantienen los datos guardados.
            if (!hayConexionInternet()) {
                Toast.makeText(this, R.string.estado_esios_sin_conexion, Toast.LENGTH_SHORT).show();
                return true;
            }

            // Enviamos un broadcast interno para que los fragments ESIOS actualicen sus datos.
            Intent intent = new Intent("com.example.consumoapp.ACCION_ACTUALIZAR_ESIOS");
            intent.setPackage(getPackageName());
            sendBroadcast(intent);

            Toast.makeText(this, R.string.estado_esios_actualizando, Toast.LENGTH_SHORT).show();

            // Los fragments guardan los datos de forma asíncrona. Volvemos a consultar
            // el estado unos segundos después para reflejar la nueva fecha si ya terminó.
            mainHandler.postDelayed(this::actualizarEstadoEsiosVisible, 2500L);
            return true;
        }

        if (item.getItemId() == R.id.action_info_seccion_esios) {
            mostrarInformacionSeccionEsios(binding.viewPagerEsios.getCurrentItem());
            return true;
        }

        // El avatar de usuario mantiene su comportamiento centralizado.
        if (NavigationUtils.handleNavigation(this, item.getItemId())) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Muestra una explicación breve y contextual de la pestaña ESIOS abierta.
     * El menú de tres puntos deja así de contener acciones duplicadas como cerrar sesión.
     */
    private void mostrarInformacionSeccionEsios(int posicion) {
        int tituloId;
        int mensajeId;

        switch (posicion) {
            case 1:
                tituloId = R.string.info_esios_precio_titulo;
                mensajeId = R.string.info_esios_precio_mensaje;
                break;
            case 2:
                tituloId = R.string.info_esios_graficos_titulo;
                mensajeId = R.string.info_esios_graficos_mensaje;
                break;
            case 0:
            default:
                tituloId = R.string.info_esios_resumen_titulo;
                mensajeId = R.string.info_esios_resumen_mensaje;
                break;
        }

        new AlertDialog.Builder(this)
                .setTitle(tituloId)
                .setMessage(mensajeId)
                .setPositiveButton(R.string.accion_cerrar, null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Liberamos el receiver para evitar fugas de memoria.
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
        executorEstado.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
    }
}
