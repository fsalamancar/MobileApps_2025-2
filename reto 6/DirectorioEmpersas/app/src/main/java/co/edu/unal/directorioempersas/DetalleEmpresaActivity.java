package co.edu.unal.directorioempersas;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DetalleEmpresaActivity extends AppCompatActivity {

    private EditText etNombre, etUrl, etTelefono, etEmail, etProductos;
    private RadioGroup radioGroupClasificacion;
    private RadioButton rbConsultoria, rbDesarrollo, rbFabrica;
    private Button btnGuardar;
    private DBHelper dbHelper;
    private int empresaId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_empresa);

        // Inicializar vistas
        etNombre = findViewById(R.id.etNombre);
        etUrl = findViewById(R.id.etUrl);
        etTelefono = findViewById(R.id.etTelefono);
        etEmail = findViewById(R.id.etEmail);
        etProductos = findViewById(R.id.etProductos);
        radioGroupClasificacion = findViewById(R.id.radioGroupClasificacion);
        rbConsultoria = findViewById(R.id.rbConsultoria);
        rbDesarrollo = findViewById(R.id.rbDesarrollo);
        rbFabrica = findViewById(R.id.rbFabrica);
        btnGuardar = findViewById(R.id.btnGuardar);

        dbHelper = new DBHelper(this);

        // Verificar si es edición
        if (getIntent().hasExtra("empresa_id")) {
            empresaId = getIntent().getIntExtra("empresa_id", -1);
            cargarDatosEmpresa();
        }

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarEmpresa();
            }
        });
    }

    private void cargarDatosEmpresa() {
        Empresa empresa = dbHelper.obtenerEmpresa(empresaId);
        if (empresa != null) {
            etNombre.setText(empresa.getNombre());
            etUrl.setText(empresa.getUrl());
            etTelefono.setText(empresa.getTelefono());
            etEmail.setText(empresa.getEmail());
            etProductos.setText(empresa.getProductos());

            switch (empresa.getClasificacion()) {
                case "Consultoría":
                    rbConsultoria.setChecked(true);
                    break;
                case "Desarrollo a la medida":
                    rbDesarrollo.setChecked(true);
                    break;
                case "Fábrica de software":
                    rbFabrica.setChecked(true);
                    break;
            }
        }
    }

    private void guardarEmpresa() {
        String nombre = etNombre.getText().toString().trim();
        String url = etUrl.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String productos = etProductos.getText().toString().trim();

        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = radioGroupClasificacion.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Seleccione una clasificación", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadio = findViewById(selectedId);
        String clasificacion = selectedRadio.getText().toString();

        boolean resultado;
        if (empresaId == -1) {
            // Insertar nueva empresa
            resultado = dbHelper.insertarEmpresa(nombre, url, telefono, email, productos, clasificacion);
            if (resultado) {
                Toast.makeText(this, "Empresa agregada exitosamente", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Actualizar empresa existente
            resultado = dbHelper.actualizarEmpresa(empresaId, nombre, url, telefono, email, productos, clasificacion);
            if (resultado) {
                Toast.makeText(this, "Empresa actualizada exitosamente", Toast.LENGTH_SHORT).show();
            }
        }

        if (resultado) {
            finish();
        } else {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
        }
    }
}
