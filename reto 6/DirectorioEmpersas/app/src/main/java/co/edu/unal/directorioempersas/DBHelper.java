package co.edu.unal.directorioempersas;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

//Clase la cual maneja la creacion y actualizacion de la db - mdeiante el uso de sqlite
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "DirectorioEmpresas.db";
    public static final int DATABASE_VERSION = 1;

    // Nombre de la tabla
    public static final String TABLE_EMPRESAS = "empresas";

    // Columnas
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_TELEFONO = "telefono";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PRODUCTOS = "productos_servicios";
    public static final String COLUMN_CLASIFICACION = "clasificacion";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //se llama una vez a crear la db
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_EMPRESAS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NOMBRE + " TEXT NOT NULL, " +
                COLUMN_URL + " TEXT, " +
                COLUMN_TELEFONO + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_PRODUCTOS + " TEXT, " +
                COLUMN_CLASIFICACION + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    //update de la db 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPRESAS);
        onCreate(db);
    }

    // CREATE - Insertar una nueva empresa
    public boolean insertarEmpresa(String nombre, String url, String telefono,
                                   String email, String productos, String clasificacion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_URL, url);
        values.put(COLUMN_TELEFONO, telefono);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PRODUCTOS, productos);
        values.put(COLUMN_CLASIFICACION, clasificacion);

        long result = db.insert(TABLE_EMPRESAS, null, values);
        return result != -1;
    }

    // READ - Obtener todas las empresas
    public ArrayList<Empresa> obtenerTodasEmpresas() {
        ArrayList<Empresa> listaEmpresas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EMPRESAS, null);

        if (cursor.moveToFirst()) {
            do {
                Empresa empresa = new Empresa();
                empresa.setId(cursor.getInt(0));
                empresa.setNombre(cursor.getString(1));
                empresa.setUrl(cursor.getString(2));
                empresa.setTelefono(cursor.getString(3));
                empresa.setEmail(cursor.getString(4));
                empresa.setProductos(cursor.getString(5));
                empresa.setClasificacion(cursor.getString(6));
                listaEmpresas.add(empresa);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return listaEmpresas;
    }

    // READ - Obtener una empresa por ID
    public Empresa obtenerEmpresa(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EMPRESAS, null,
                COLUMN_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Empresa empresa = new Empresa();
            empresa.setId(cursor.getInt(0));
            empresa.setNombre(cursor.getString(1));
            empresa.setUrl(cursor.getString(2));
            empresa.setTelefono(cursor.getString(3));
            empresa.setEmail(cursor.getString(4));
            empresa.setProductos(cursor.getString(5));
            empresa.setClasificacion(cursor.getString(6));
            cursor.close();
            return empresa;
        }
        return null;
    }

    // UPDATE - Actualizar una empresa
    public boolean actualizarEmpresa(int id, String nombre, String url, String telefono,
                                     String email, String productos, String clasificacion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_URL, url);
        values.put(COLUMN_TELEFONO, telefono);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PRODUCTOS, productos);
        values.put(COLUMN_CLASIFICACION, clasificacion);

        int rowsAffected = db.update(TABLE_EMPRESAS, values,
                COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        return rowsAffected > 0;
    }

    // DELETE - Eliminar una empresa
    public boolean eliminarEmpresa(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_EMPRESAS,
                COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        return rowsDeleted > 0;
    }

    // Filtrar por nombre
    public ArrayList<Empresa> filtrarPorNombre(String nombre) {
        ArrayList<Empresa> listaEmpresas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EMPRESAS, null,
                COLUMN_NOMBRE + " LIKE ?", new String[]{"%" + nombre + "%"},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Empresa empresa = new Empresa();
                empresa.setId(cursor.getInt(0));
                empresa.setNombre(cursor.getString(1));
                empresa.setUrl(cursor.getString(2));
                empresa.setTelefono(cursor.getString(3));
                empresa.setEmail(cursor.getString(4));
                empresa.setProductos(cursor.getString(5));
                empresa.setClasificacion(cursor.getString(6));
                listaEmpresas.add(empresa);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return listaEmpresas;
    }

    // Filtrar por clasificación
    public ArrayList<Empresa> filtrarPorClasificacion(String clasificacion) {
        ArrayList<Empresa> listaEmpresas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EMPRESAS, null,
                COLUMN_CLASIFICACION + "=?", new String[]{clasificacion},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Empresa empresa = new Empresa();
                empresa.setId(cursor.getInt(0));
                empresa.setNombre(cursor.getString(1));
                empresa.setUrl(cursor.getString(2));
                empresa.setTelefono(cursor.getString(3));
                empresa.setEmail(cursor.getString(4));
                empresa.setProductos(cursor.getString(5));
                empresa.setClasificacion(cursor.getString(6));
                listaEmpresas.add(empresa);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return listaEmpresas;
    }

    // Filtrar por nombre Y clasificación
    public ArrayList<Empresa> filtrarPorNombreYClasificacion(String nombre, String clasificacion) {
        ArrayList<Empresa> listaEmpresas = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EMPRESAS, null,
                COLUMN_NOMBRE + " LIKE ? AND " + COLUMN_CLASIFICACION + "=?",
                new String[]{"%" + nombre + "%", clasificacion},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Empresa empresa = new Empresa();
                empresa.setId(cursor.getInt(0));
                empresa.setNombre(cursor.getString(1));
                empresa.setUrl(cursor.getString(2));
                empresa.setTelefono(cursor.getString(3));
                empresa.setEmail(cursor.getString(4));
                empresa.setProductos(cursor.getString(5));
                empresa.setClasificacion(cursor.getString(6));
                listaEmpresas.add(empresa);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return listaEmpresas;
    }



}
