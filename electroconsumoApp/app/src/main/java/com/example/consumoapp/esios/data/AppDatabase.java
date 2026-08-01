package com.example.consumoapp.esios.data;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.consumoapp.factura.ElectrodomesticoDao;
import com.example.consumoapp.factura.ElectrodomesticoEntity;
import com.example.consumoapp.factura.SimulacionFacturaDao;
import com.example.consumoapp.factura.SimulacionFacturaEntity;
import com.example.consumoapp.tarifas.data.ComercializadoraEntity;
import com.example.consumoapp.tarifas.data.TarifaComercialEntity;
import com.example.consumoapp.tarifas.data.TarifaDao;
import com.example.consumoapp.tarifas.data.TarifaSeed;
import com.example.consumoapp.regulacion.data.ConstanteReguladaDao;
import com.example.consumoapp.regulacion.data.ConstanteReguladaEntity;
import com.example.consumoapp.usuarios.data.UsuarioDao;
import com.example.consumoapp.usuarios.data.UsuarioEntity;

/**
 * Base de datos local de la aplicación.
 *
 * Room crea por debajo una base de datos SQLite. La versión 3 añade las tablas
 * de comercializadoras y tarifas sin borrar los precios ESIOS ni los
 * electrodomésticos existentes.
 */
@Database(
        entities = {
                PrecioLuzEntity.class,
                ElectrodomesticoEntity.class,
                ComercializadoraEntity.class,
                TarifaComercialEntity.class,
                SimulacionFacturaEntity.class,
                ConstanteReguladaEntity.class,
                UsuarioEntity.class
        },
        version = 10,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    // Instancia única de la base de datos.
    private static volatile AppDatabase instance;

    // DAO disponible para consultar precios ESIOS.
    public abstract PrecioLuzDao precioLuzDao();

    // DAO para consultar electrodomésticos usados por el simulador.
    public abstract ElectrodomesticoDao electrodomesticoDao();

    // DAO para consultar comercializadoras y tarifas del mercado libre.
    public abstract TarifaDao tarifaDao();

    // DAO para guardar y consultar el historial de facturas simuladas.
    public abstract SimulacionFacturaDao simulacionFacturaDao();

    // DAO de constantes reguladas con fecha de vigencia.
    public abstract ConstanteReguladaDao constanteReguladaDao();

    // DAO de cuentas locales.
    public abstract UsuarioDao usuarioDao();

    /**
     * Migración segura de la versión 2 a la 3.
     *
     * Antes se utilizaba fallbackToDestructiveMigration(), que podía borrar la
     * base de datos. Esta migración conserva los datos ya guardados.
     */
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `comercializadora` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`nombre` TEXT NOT NULL)"
            );

            database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_comercializadora_nombre` " +
                            "ON `comercializadora` (`nombre`)"
            );

            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `tarifa_comercial` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`comercializadoraId` INTEGER NOT NULL, " +
                            "`nombre` TEXT NOT NULL, " +
                            "`fechaActualizacion` TEXT, " +
                            "`fuente` TEXT, " +
                            "`precioP1` REAL, " +
                            "`precioP2` REAL, " +
                            "`precioP3` REAL, " +
                            "`potenciaP1` REAL, " +
                            "`potenciaP2` REAL, " +
                            "`alquiler` REAL, " +
                            "`permanencia` TEXT, " +
                            "`descuento` TEXT, " +
                            "`servicios` TEXT, " +
                            "`observaciones` TEXT, " +
                            "`activa` INTEGER NOT NULL, " +
                            "FOREIGN KEY(`comercializadoraId`) REFERENCES `comercializadora`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE)"
            );

            database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_tarifa_comercial_comercializadoraId` " +
                            "ON `tarifa_comercial` (`comercializadoraId`)"
            );

            database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_tarifa_comercial_comercializadoraId_nombre` " +
                            "ON `tarifa_comercial` (`comercializadoraId`, `nombre`)"
            );
        }
    };


    /** Migración de la versión 3 a la 4: añade el historial de simulaciones. */
    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `simulacion_factura` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`fechaCreacion` INTEGER NOT NULL, " +
                            "`nombre` TEXT, `fuentePrecio` TEXT, `comercializadora` TEXT, `tarifa` TEXT, " +
                            "`consumoTotalKwh` REAL NOT NULL, `consumoP1Kwh` REAL NOT NULL, " +
                            "`consumoP2Kwh` REAL NOT NULL, `consumoP3Kwh` REAL NOT NULL, " +
                            "`costeP1` REAL NOT NULL, `costeP2` REAL NOT NULL, `costeP3` REAL NOT NULL, " +
                            "`costeEnergia` REAL NOT NULL, `costePotencia` REAL NOT NULL, " +
                            "`alquilerContador` REAL NOT NULL, `impuestoElectricidad` REAL NOT NULL, " +
                            "`iva` REAL NOT NULL, `totalFactura` REAL NOT NULL, " +
                            "`potenciaPuntaKw` REAL NOT NULL, `potenciaValleKw` REAL NOT NULL, " +
                            "`diasFactura` INTEGER NOT NULL, `numeroElectrodomesticos` INTEGER NOT NULL)"
            );
        }
    };



    /** Migración 4 a 5: convierte precios_luz en un histórico actualizable. */
    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `precios_luz` ADD COLUMN `indicadorId` INTEGER NOT NULL DEFAULT 1001");
            database.execSQL("ALTER TABLE `precios_luz` ADD COLUMN `precioKwh` REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `precios_luz` ADD COLUMN `fechaDescarga` INTEGER NOT NULL DEFAULT 0");
            database.execSQL("UPDATE `precios_luz` SET `precioKwh` = `precio` / 1000.0 WHERE `precioKwh` = 0");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_precios_luz_fechaHora` ON `precios_luz` (`fechaHora`)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_precios_luz_indicadorId_fechaHora_zona` " +
                    "ON `precios_luz` (`indicadorId`, `fechaHora`, `zona`)");
        }
    };


    /** Migración 5 a 6: añade los electrodomésticos configurados del hogar. */
    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `electrodomestico_hogar` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`catalogoId` INTEGER, `nombre` TEXT, `categoria` TEXT, `estancia` TEXT, " +
                            "`potenciaW` REAL NOT NULL, `porcentajeUso` REAL NOT NULL, " +
                            "`horasDia` REAL NOT NULL, `diasMes` INTEGER NOT NULL, " +
                            "`periodoUso` TEXT, `horaInicio` REAL NOT NULL, " +
                            "`diaValleCompleto` INTEGER NOT NULL, `activo` INTEGER NOT NULL)"
            );
        }
    };



    /** Migración 6 a 7: añade las constantes reguladas versionadas por fecha. */
    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `constante_regulada` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`codigo` TEXT NOT NULL, `periodo` TEXT NOT NULL, " +
                            "`valor` REAL NOT NULL, `unidad` TEXT NOT NULL, " +
                            "`fechaDesde` TEXT NOT NULL, `fechaHasta` TEXT, " +
                            "`fuente` TEXT NOT NULL, `activa` INTEGER NOT NULL)"
            );
            database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_constante_regulada_codigo_periodo_fechaDesde` " +
                            "ON `constante_regulada` (`codigo`, `periodo`, `fechaDesde`)"
            );
            database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_constante_regulada_fechaDesde_fechaHasta` " +
                            "ON `constante_regulada` (`fechaDesde`, `fechaHasta`)"
            );
        }
    };

    /** Migración 7 a 8: usuarios locales y separación de datos por cuenta. */
    private static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `usuario` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`usuario` TEXT NOT NULL, `usuarioNormalizado` TEXT NOT NULL, " +
                            "`email` TEXT NOT NULL, `emailNormalizado` TEXT NOT NULL, " +
                            "`passwordHash` TEXT NOT NULL, `passwordSalt` TEXT NOT NULL, " +
                            "`fechaRegistro` INTEGER NOT NULL, `activo` INTEGER NOT NULL)"
            );
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_usuario_usuarioNormalizado` ON `usuario` (`usuarioNormalizado`)");
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_usuario_emailNormalizado` ON `usuario` (`emailNormalizado`)");

            database.execSQL("ALTER TABLE `electrodomestico_hogar` ADD COLUMN `usuarioId` INTEGER NOT NULL DEFAULT 0");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_electrodomestico_hogar_usuarioId` ON `electrodomestico_hogar` (`usuarioId`)");

            database.execSQL("ALTER TABLE `simulacion_factura` ADD COLUMN `usuarioId` INTEGER NOT NULL DEFAULT 0");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_simulacion_factura_usuarioId` ON `simulacion_factura` (`usuarioId`)");
        }
    };


    /** Migración 8 a 9: conserva el desglose profesional completo de cada simulación. */
    private static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `simulacion_factura` ADD COLUMN `costeEnergiaMercado` REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `simulacion_factura` ADD COLUMN `costePotenciaBase` REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `simulacion_factura` ADD COLUMN `peajesEnergia` REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `simulacion_factura` ADD COLUMN `peajesPotencia` REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `simulacion_factura` ADD COLUMN `cargosEnergia` REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `simulacion_factura` ADD COLUMN `cargosPotencia` REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `simulacion_factura` ADD COLUMN `ajustesSistema` REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `simulacion_factura` ADD COLUMN `otrosConceptos` REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `simulacion_factura` ADD COLUMN `baseImpuestoElectricidad` REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `simulacion_factura` ADD COLUMN `baseIva` REAL NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `simulacion_factura` ADD COLUMN `reguladosSeparados` INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `simulacion_factura` ADD COLUMN `fuenteConstantes` TEXT");
        }
    };

    /**
     * Migración 9 a 10: elimina la tabla de aparatos genéricos guardados.
     * La función fue retirada de la aplicación, por lo que esta tabla ya no forma
     * parte del esquema de Room. El resto de datos se conserva.
     */
    private static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE IF EXISTS `electrodomestico_hogar`");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "consumo_app.db"
                            )
                            .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    // Diagnóstico seguro: confirma que Room abrió las tablas.
                                    try (Cursor cursor = db.query(
                                            "SELECT name FROM sqlite_master WHERE type='table' "
                                                    + "AND name IN ('usuario','precios_luz') ORDER BY name")) {
                                        StringBuilder tablas = new StringBuilder();
                                        while (cursor.moveToNext()) {
                                            if (tablas.length() > 0) tablas.append(", ");
                                            tablas.append(cursor.getString(0));
                                        }
                                        Log.d("ROOM_DB", "Tablas verificadas: " + tablas);
                                    } catch (Exception e) {
                                        Log.e("ROOM_DB", "No se pudieron verificar las tablas", e);
                                    }
                                }
                            })
                            .build();

                    // Importa las 34 tarifas del CSV solo la primera vez.
                    TarifaSeed.cargarSiEstaVacio(
                            context.getApplicationContext(),
                            instance
                    );
                }
            }
        }

        return instance;
    }
}
