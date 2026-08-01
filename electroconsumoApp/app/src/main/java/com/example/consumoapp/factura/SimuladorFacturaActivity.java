package com.example.consumoapp.factura;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.consumoapp.R;
import com.example.consumoapp.SessionManager;
import com.example.consumoapp.databinding.ActivitySimuladorFacturaBinding;
import com.example.consumoapp.esios.provider.FuentePrecioEsiosLocal;
import com.example.consumoapp.factura.viewmodel.SimuladorFacturaViewModel;
import com.example.consumoapp.factura.model.SimuladorFacturaEvent;
import com.example.consumoapp.factura.model.SimuladorFacturaUiState;
import com.example.consumoapp.factura.data.SimuladorFacturaRepository;
import com.example.consumoapp.tarifas.data.ComercializadoraEntity;
import com.example.consumoapp.tarifas.data.TarifaComercialEntity;
import com.example.consumoapp.factura.ui.ResultadoFacturaRenderer;
import com.example.consumoapp.factura.ui.FacturaCalculationController;
import com.example.consumoapp.factura.ui.ComparadorTarifasNavigator;
import com.example.consumoapp.factura.ui.SimulacionSnapshotFactory;
import com.example.consumoapp.factura.ui.ElectrodomesticosFormController;
import com.example.consumoapp.factura.ui.SimuladorTabsController;
import com.example.consumoapp.factura.ui.TarifaFormController;
import com.example.consumoapp.factura.ui.ContratoFormController;
import com.example.consumoapp.factura.ui.SimuladorToolbarController;
import com.example.consumoapp.regulacion.domain.ConstantesFactura;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Simulador de consumo y factura aproximada.
 *
 * Funcionamiento:
 * 1. Carga los electrodomésticos verificados en SQLite.
 * 2. Llena el Spinner de categorías y el Spinner de electrodomésticos.
 * 3. Muestra las potencias publicadas.
 * 4. Permite añadir líneas de consumo.
 * 5. Suma kWh, coste de energía, potencia, alquiler, impuesto eléctrico e IVA.
 */
public class SimuladorFacturaActivity extends AppCompatActivity {

    /**
     * Extra usado por la navegación general para abrir una pestaña concreta.
     * 0 = Hogar, 1 = Uso/electrodomésticos, 2 = Tarifa y factura.
     */
    public static final String EXTRA_TAB_INICIAL = "extra_tab_inicial";

    private ActivitySimuladorFacturaBinding binding;
    private SimuladorFacturaViewModel viewModel;
    private SimuladorTabsController tabsController;
    private ResultadoFacturaRenderer resultadoRenderer;
    private ElectrodomesticosFormController electrodomesticosController;
    private TarifaFormController tarifaController;
    private FacturaCalculationController calculoController;
    private ContratoFormController contratoController;
    private SimuladorToolbarController toolbarController;
    private ConstantesFactura constantesVigentes;

    // La instancia se obtiene del ViewModel para conservar todas las líneas añadidas.
    private List<LineaSimuladorFactura> lineas;
    private LineaSimuladorAdapter lineasAdapter;


    // Referencias del último estado renderizado. Evitan repetir efectos al cambiar otra sección.
    private List<ComercializadoraEntity> comercializadorasRenderizadas;
    private List<TarifaComercialEntity> tarifasRenderizadas;
    private List<String> categoriasRenderizadas;
    private List<ElectrodomesticoEntity> electrodomesticosRenderizados;
    private SimuladorFacturaUiState.ConstantesCargadas constantesRenderizadas;
    private SimuladorFacturaRepository.PreciosEsios preciosEsiosRenderizados;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.estaLogueado(this)) {
            startActivity(new Intent(this, com.example.consumoapp.LoginActivity.class));
            finish();
            return;
        }
        binding = ActivitySimuladorFacturaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(SimuladorFacturaViewModel.class);

        // Recupera la misma lista aunque Android recree la Activity.
        // Así, añadir una segunda o tercera línea no elimina las anteriores.
        lineas = viewModel.getLineasFactura();

        tabsController = new SimuladorTabsController(binding, this::recalcularTotales);
        resultadoRenderer = new ResultadoFacturaRenderer(binding);
        toolbarController = new SimuladorToolbarController(this, binding);
        toolbarController.configurar();
        configurarTabs();
        aplicarTabInicial(getIntent());
        configurarRecyclerView();

        tarifaController = new TarifaFormController(
                this,
                binding,
                lineas,
                lineasAdapter,
                viewModel::cargarTarifas,
                this::recalcularTotales,
                () -> constantesVigentes != null
        );
        electrodomesticosController = new ElectrodomesticosFormController(
                this,
                binding,
                viewModel::cargarElectrodomesticos,
                tarifaController::actualizarPrecioCampoSegunPeriodo
        );
        calculoController = new FacturaCalculationController(
                this,
                binding,
                lineas,
                lineasAdapter,
                resultadoRenderer,
                tarifaController::getFuenteSeleccionada
        );
        contratoController = new ContratoFormController(
                binding,
                this::recalcularTotales
        );
        observarViewModel();

        electrodomesticosController.configurarPeriodos();
        electrodomesticosController.configurarTiposDia();
        configurarEventos();
        contratoController.cargarValoresPorDefecto();
        cargarConstantesReguladasVigentes(false);
        cargarDatosIniciales();
        configurarSelectorFuentePrecio();
        cargarComercializadoras();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        aplicarTabInicial(intent);
    }

    /**
     * Abre la sección solicitada desde el menú general sin perder la lógica
     * del simulador ni crear una pantalla duplicada de electrodomésticos.
     */
    private void aplicarTabInicial(Intent intent) {
        if (intent == null || !intent.hasExtra(EXTRA_TAB_INICIAL)) {
            return;
        }

        int posicion = intent.getIntExtra(EXTRA_TAB_INICIAL, 0);
        posicion = Math.max(0, Math.min(2, posicion));
        seleccionarTab(posicion);
    }


    /**
     * Observa datos ligados al ciclo de vida. LiveData no actualiza una Activity destruida.
     */
    private void observarViewModel() {
        viewModel.getUiState().observe(this, this::renderizarEstado);

        viewModel.getEventos().observe(this, contenedor -> {
            SimuladorFacturaEvent evento = contenedor == null ? null : contenedor.consumir();
            if (evento != null) {
                procesarEvento(evento);
            }
        });
    }

    /** Procesa cada efecto puntual una sola vez, incluso tras recrear la Activity. */
    private void procesarEvento(SimuladorFacturaEvent evento) {
        if (evento instanceof SimuladorFacturaEvent.MostrarError) {
            contratoController.mostrarCargando(false);
            String mensaje = ((SimuladorFacturaEvent.MostrarError) evento).mensaje;
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            return;
        }

        if (evento instanceof SimuladorFacturaEvent.SimulacionGuardada) {
            String mensaje = ((SimuladorFacturaEvent.SimulacionGuardada) evento).mensaje;
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
            return;
        }

        if (evento instanceof SimuladorFacturaEvent.MostrarMensaje) {
            String mensaje = ((SimuladorFacturaEvent.MostrarMensaje) evento).mensaje;
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        }
    }

    /** Actualiza únicamente las secciones cuyo dato cambió dentro del estado inmutable. */
    private void renderizarEstado(SimuladorFacturaUiState estado) {
        if (estado == null) {
            return;
        }

        if (comercializadorasRenderizadas != estado.comercializadoras) {
            comercializadorasRenderizadas = estado.comercializadoras;
            tarifaController.mostrarComercializadoras(estado.comercializadoras);
        }
        if (tarifasRenderizadas != estado.tarifas) {
            tarifasRenderizadas = estado.tarifas;
            tarifaController.mostrarTarifas(estado.tarifas);
        }
        if (categoriasRenderizadas != estado.categorias) {
            categoriasRenderizadas = estado.categorias;
            electrodomesticosController.mostrarCategorias(estado.categorias);
        }
        if (electrodomesticosRenderizados != estado.electrodomesticos) {
            electrodomesticosRenderizados = estado.electrodomesticos;
            electrodomesticosController.mostrarElectrodomesticos(estado.electrodomesticos);
        }
        if (constantesRenderizadas != estado.constantes && estado.constantes != null) {
            constantesRenderizadas = estado.constantes;
            mostrarConstantes(estado.constantes);
        }
        if (preciosEsiosRenderizados != estado.preciosEsios && estado.preciosEsios != null) {
            preciosEsiosRenderizados = estado.preciosEsios;
            mostrarPreciosEsios(estado.preciosEsios);
        }

        // Impide lanzar otra carga manual de constantes mientras hay trabajo pendiente.
        contratoController.mostrarCargando(estado.cargando);
    }

    private void mostrarConstantes(SimuladorFacturaUiState.ConstantesCargadas resultado) {
        ConstantesFactura constantes = resultado.constantes;
        constantesVigentes = constantes;
        calculoController.setConstantes(constantes);

        contratoController.mostrarConstantes(constantes);
        tarifaController.actualizarDisponibilidadConstantes();

        if (resultado.mostrarMensaje) {
            Toast.makeText(this,
                    "Constantes cargadas desde SQLite para " + resultado.fecha,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarPreciosEsios(SimuladorFacturaRepository.PreciosEsios resultado) {
        if (resultado.precios.isEmpty()) {
            Toast.makeText(this,
                    "No hay precios ESIOS guardados. Actualiza ESIOS o introduce el precio manualmente.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        FuentePrecioEsiosLocal fuente = new FuentePrecioEsiosLocal(resultado.precios, resultado.fecha);
        double p1 = fuente.getPrecioEnergiaP1();
        double p2 = fuente.getPrecioEnergiaP2();
        double p3 = fuente.getPrecioEnergiaP3();
        double media = (p1 + p2 + p3) / 3.0;

        binding.radioEsios.setChecked(true);
        binding.edtPrecioKwh.setText(String.format(Locale.US, "%.6f", media));
        calculoController.actualizarPrecios(p1, p2, p3);
        tarifaController.establecerFuenteEsios(fuente.getDescripcionFuente());
        Toast.makeText(this,
                String.format(Locale.getDefault(),
                        "ESIOS %s cargado · P1 %.4f · P2 %.4f · P3 %.4f €/kWh",
                        resultado.fecha, p1, p2, p3),
                Toast.LENGTH_LONG).show();
    }

    /** Configura la navegación visual delegándola en su controlador. */
    private void configurarTabs() {
        tabsController.configurar();
    }

    /** Muestra una pestaña concreta sin mezclar esta lógica con la Activity. */
    private void mostrarTab(int posicion) {
        tabsController.mostrar(posicion);
    }

    /** Selecciona una pestaña validando el índice solicitado. */
    private void seleccionarTab(int posicion) {
        tabsController.seleccionar(posicion);
    }

    private void configurarRecyclerView() {
        lineasAdapter = new LineaSimuladorAdapter(lineas, posicion -> {
            lineas.remove(posicion);
            lineasAdapter.notifyItemRemoved(posicion);
            lineasAdapter.notifyItemRangeChanged(posicion, lineas.size() - posicion);
            recalcularTotales();
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(true);

        binding.recyclerLineasFactura.setLayoutManager(layoutManager);
        binding.recyclerLineasFactura.setAdapter(lineasAdapter);

        // La animación de cambios puede conservar temporalmente la altura anterior
        // cuando RecyclerView está dentro de un ScrollView.
        binding.recyclerLineasFactura.setItemAnimator(null);
        binding.recyclerLineasFactura.setNestedScrollingEnabled(false);
    }

    private void configurarEventos() {
        binding.btnUsarPrecioEsios.setOnClickListener(v -> cargarPrecioMedioEsios());
        binding.btnAnadirLinea.setOnClickListener(v -> anadirLinea());
        binding.btnCargarConstantes.setOnClickListener(v ->
                cargarConstantesReguladasVigentes(true));
        binding.checkPrecioSoloMercado.setOnCheckedChangeListener((button, checked) ->
                recalcularTotales());

        // Botones de navegación entre pestañas.
        binding.btnSiguienteHogar.setOnClickListener(v -> seleccionarTab(1));
        binding.btnVolverUso.setOnClickListener(v -> seleccionarTab(0));
        // La pestaña Contrato fue eliminada: desde Uso se pasa directamente a Factura.
        binding.btnSiguienteUso.setOnClickListener(v -> seleccionarTab(2));
        binding.btnCalcularFactura.setOnClickListener(v -> calcularFacturaConFuenteSeleccionada());
        // Desde Factura se vuelve directamente a la sección Uso.
        binding.btnVolverFactura.setOnClickListener(v -> seleccionarTab(1));
        binding.btnCompararTarifas.setOnClickListener(v -> abrirComparadorTarifas());
        binding.btnGuardarSimulacion.setOnClickListener(v -> pedirNombreYGuardarSimulacion());
        binding.btnVerHistorial.setOnClickListener(v ->
                startActivity(new Intent(this, HistorialSimulacionesActivity.class)));
    }

    /** Configura la fuente de precios delegándola en su controlador. */
    private void configurarSelectorFuentePrecio() {
        tarifaController.configurarSelectorFuentePrecio();
    }

    /** Solicita al ViewModel las comercializadoras sin bloquear la interfaz. */
    private void cargarComercializadoras() {
        viewModel.cargarComercializadoras();
    }

    /** Actualiza el precio visible al cambiar de periodo. */
    private void actualizarPrecioCampoSegunPeriodo() {
        tarifaController.actualizarPrecioCampoSegunPeriodo();
    }

    /**
     * Solicita al ViewModel las constantes vigentes. Room se consulta fuera del hilo principal.
     */
    private void cargarConstantesReguladasVigentes(boolean mostrarMensaje) {
        final String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(new Date());
        contratoController.mostrarCargando(true);
        viewModel.cargarConstantes(fecha, mostrarMensaje);
    }

    private void cargarDatosIniciales() {
        viewModel.cargarCategorias();
    }

    /**
     * Aplica la fuente elegida a todas las líneas de consumo y genera el resultado.
     * La selección de electrodomésticos nunca necesita conocer precios.
     */
    private void calcularFacturaConFuenteSeleccionada() {
        if (lineas.isEmpty()) {
            Toast.makeText(this, "Añade al menos un electrodoméstico antes de calcular", Toast.LENGTH_LONG).show();
            seleccionarTab(1);
            return;
        }

        if (tarifaController.esComercializadora()) {
            if (!tarifaController.tieneTarifaSeleccionada()) {
                Toast.makeText(this, "Selecciona una comercializadora y una tarifa", Toast.LENGTH_LONG).show();
                return;
            }
            tarifaController.aplicarTarifaSeleccionada();
            return;
        }

        if (tarifaController.esManual()) {
            Double precio = contratoController.leerPrecioManual();
            if (precio == null) {
                return;
            }
            calculoController.actualizarPrecioUnico(precio);
            tarifaController.establecerFuenteManual();
            recalcularTotales();
            return;
        }

        // Para PVPC/ESIOS se cargan P1, P2 y P3 desde Room y, al terminar,
        // cargarPrecioMedioEsios recalcula la factura con esos valores.
        cargarPrecioMedioEsios();
    }

    private void cargarPrecioMedioEsios() {
        viewModel.cargarUltimosPreciosEsios();
    }

    private void anadirLinea() {
        LineaSimuladorFactura linea = electrodomesticosController.crearLinea();
        if (linea == null) {
            return;
        }

        // Guardamos la nueva línea en la misma lista compartida por el adaptador.
        lineas.add(linea);

        // Actualizamos el contenido completo para forzar una nueva medición de altura.
        // Es importante porque esta lista está dentro del ScrollView de la pestaña Uso.
        lineasAdapter.notifyDataSetChanged();
        binding.recyclerLineasFactura.requestLayout();

        binding.recyclerLineasFactura.post(() -> {
            binding.recyclerLineasFactura.requestLayout();
            binding.recyclerLineasFactura.scrollToPosition(lineas.size() - 1);
        });

        recalcularTotales();
        electrodomesticosController.limpiarUso();

        Toast.makeText(
                this,
                "Electrodoméstico añadido. Total en la lista: " + lineas.size(),
                Toast.LENGTH_SHORT
        ).show();
    }

    /**
     * Abre el comparador con exactamente el mismo consumo calculado a partir
     * de los electrodomésticos, incluidos su porcentaje de uso y periodos.
     */
    private void abrirComparadorTarifas() {
        recalcularTotales();
        if (lineas.isEmpty()) {
            Toast.makeText(this, "Añade al menos un electrodoméstico a la factura", Toast.LENGTH_SHORT).show();
            seleccionarTab(1);
            return;
        }
        if (calculoController.getUltimoResultado() == null) {
            return;
        }

        ComparadorTarifasNavigator.abrir(
                this,
                lineas,
                calculoController,
                tarifaController.getFuenteSeleccionada()
        );
    }

    private void recalcularTotales() {
        calculoController.recalcular();
    }


    /** Solicita un nombre para identificar la simulación en el historial. */
    private void pedirNombreYGuardarSimulacion() {
        if (lineas.isEmpty()) {
            Toast.makeText(this, "Añade al menos un electrodoméstico antes de guardar", Toast.LENGTH_LONG).show();
            seleccionarTab(1);
            return;
        }

        EditText input = new EditText(this);
        input.setHint("Ej.: Factura julio 2026");
        input.setSingleLine(true);

        new AlertDialog.Builder(this)
                .setTitle("Guardar simulación")
                .setMessage("Escribe un nombre para reconocerla en el historial.")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nombre = input.getText().toString().trim();
                    if (nombre.isEmpty()) {
                        nombre = "Simulación " + (System.currentTimeMillis() / 1000L);
                    }
                    guardarSimulacion(nombre);
                })
                .show();
    }

    /** Crea una fotografía del resultado actual y la guarda en Room. */
    private void guardarSimulacion(String nombre) {
        if (!calculoController.recalcular() || calculoController.getUltimoResultado() == null) {
            return;
        }

        SimulacionFacturaEntity simulacion = SimulacionSnapshotFactory.crear(
                SessionManager.obtenerUsuarioId(this),
                nombre,
                tarifaController.getFuenteSeleccionada(),
                tarifaController.getNombreComercializadoraSeleccionada(),
                tarifaController.getNombreTarifaSeleccionada(),
                calculoController,
                constantesVigentes,
                lineas.size()
        );
        viewModel.guardarSimulacion(simulacion);
    }


}
