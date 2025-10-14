package co.edu.unal.directorioempersas;

//Clase modelo, representa los datos


public class Empresa {
    private int id;
    private String nombre;
    private String url;
    private String telefono;
    private String email;
    private String productos;
    private String clasificacion;

    // Constructor vacío
    public Empresa() {}

    // Constructor con parámetros
    public Empresa(int id, String nombre, String url, String telefono,
                   String email, String productos, String clasificacion) {
        this.id = id;
        this.nombre = nombre;
        this.url = url;
        this.telefono = telefono;
        this.email = email;
        this.productos = productos;
        this.clasificacion = clasificacion;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProductos() { return productos; }
    public void setProductos(String productos) { this.productos = productos; }

    public String getClasificacion() { return clasificacion; }
    public void setClasificacion(String clasificacion) { this.clasificacion = clasificacion; }

    @Override
    public String toString() {
        return nombre + " - " + clasificacion;
    }
}
