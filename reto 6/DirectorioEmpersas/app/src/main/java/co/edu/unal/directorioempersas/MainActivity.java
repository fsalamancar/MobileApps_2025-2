package co.edu.unal.directorioempersas;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private ListView listView;
    private ArrayAdapter<Empresa> adapter;
    private ArrayList<Empresa> listaEmpresas;
    private EditText etFiltroNombre;
    private Spinner spinnerClasificacion;
    private Button btnFiltrar, btnLimpiarFiltros;
    private FloatingActionButton fabAgregar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        listView = findViewById(R.id.listViewEmpresas);
        etFiltroNombre = findViewById(R.id.etFiltroNombre);
        spinnerClasificacion = findViewById(R.id.spinnerClasificacion);
        btnFiltrar = findViewById(R.id.btnFiltrar);
        btnLimpiarFiltros = findViewById(R.id.btnLimpiarFiltros);
        fabAgregar = findViewById(R.id.fabAgregar);

        // Inicializar base de datos
        dbHelper = new DBHelper(this);

        // Configurar spinner de clasificaciones
        ArrayList<String> clasificaciones = new ArrayList<>();
        clasificaciones.add("Todas");
        clasificaciones.add("Consultoría");
        clasificaciones.add("Desarrollo a la medida");
        clasificaciones.add("Fábrica de software");
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, clasificaciones);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClasificacion.setAdapter(spinnerAdapter);

        // Cargar empresas
        cargarEmpresas();

        // Configurar listeners
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Empresa empresa = listaEmpresas.get(position);
                mostrarDetalleEmpresa(empresa);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Empresa empresa = listaEmpresas.get(position);
                mostrarDialogoEliminar(empresa);
                return true;
            }
        });

        btnFiltrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aplicarFiltros();
            }
        });

        btnLimpiarFiltros.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etFiltroNombre.setText("");
                spinnerClasificacion.setSelection(0);
                cargarEmpresas();
            }
        });

        fabAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DetalleEmpresaActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarEmpresas();
    }

    private void cargarEmpresas() {
        listaEmpresas = dbHelper.obtenerTodasEmpresas();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaEmpresas);
        listView.setAdapter(adapter);
    }

    private void aplicarFiltros() {
        String nombre = etFiltroNombre.getText().toString().trim();
        String clasificacion = spinnerClasificacion.getSelectedItem().toString();

        if (!nombre.isEmpty() && !clasificacion.equals("Todas")) {
            listaEmpresas = dbHelper.filtrarPorNombreYClasificacion(nombre, clasificacion);
        } else if (!nombre.isEmpty()) {
            listaEmpresas = dbHelper.filtrarPorNombre(nombre);
        } else if (!clasificacion.equals("Todas")) {
            listaEmpresas = dbHelper.filtrarPorClasificacion(clasificacion);
        } else {
            listaEmpresas = dbHelper.obtenerTodasEmpresas();
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaEmpresas);
        listView.setAdapter(adapter);
    }

    private void mostrarDetalleEmpresa(Empresa empresa) {
        Intent intent = new Intent(MainActivity.this, DetalleEmpresaActivity.class);
        intent.putExtra("empresa_id", empresa.getId());
        startActivity(intent);
    }

    private void mostrarDialogoEliminar(final Empresa empresa) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar eliminación");
        builder.setMessage("¿Está seguro que desea eliminar a " + empresa.getNombre() + "?");

        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dbHelper.eliminarEmpresa(empresa.getId())) {
                    Toast.makeText(MainActivity.this, "Empresa eliminada", Toast.LENGTH_SHORT).show();
                    cargarEmpresas();
                } else {
                    Toast.makeText(MainActivity.this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("No", null);
        builder.show();
    }
}
