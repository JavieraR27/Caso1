package com.example.productos.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.example.productos.dto.ProveedorResponse;
import com.example.productos.exception.BusinessConflictException;
import com.example.productos.exception.ResourceNotFoundException;
import com.example.productos.exception.ServiceUnavailableException;
import com.example.productos.model.Categoria;
import com.example.productos.model.EstadoProducto;
import com.example.productos.model.Oferta;
import com.example.productos.model.Producto;
import com.example.productos.repository.CategoriaRepository;
import com.example.productos.repository.OfertaRepository;
import com.example.productos.repository.ProductoRepository;

import reactor.core.publisher.Mono;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final OfertaRepository ofertaRepository;
    private final WebClient proveedoresWebClient;

    public ProductoService(ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            OfertaRepository ofertaRepository,
            @Qualifier("proveedoresWebClient") WebClient proveedoresWebClient) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.ofertaRepository = ofertaRepository;
        this.proveedoresWebClient = proveedoresWebClient;
    }

    /**
     * Publica un producto. Validación cruzada: solo un proveedor APROBADO
     * puede publicar (se consulta al servicio proveedores por WebClient).
     */
    public Producto crear(Producto producto, int categoriaId) {
        Categoria categoria = obtenerCategoria(categoriaId);
        validarProveedorAprobado(producto.getProveedorId());

        producto.setCategoria(categoria);
        producto.setEstado(EstadoProducto.ACTIVO);
        producto.setFechaCreacion(LocalDateTime.now());
        return productoRepository.save(producto);
    }

    private void validarProveedorAprobado(int proveedorId) {
        ProveedorResponse proveedor;
        try {
            proveedor = proveedoresWebClient.get()
                    .uri("/api/v1/proveedores/{id}", proveedorId)
                    .retrieve()
                    .onStatus(s -> s.value() == 404,
                            r -> Mono.error(new ResourceNotFoundException(
                                    "Proveedor no encontrado para id: " + proveedorId)))
                    .bodyToMono(ProveedorResponse.class)
                    .block();
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Servicio proveedores no disponible", e);
        }

        if (proveedor == null || !"APROBADO".equals(proveedor.estado())) {
            throw new BusinessConflictException(
                    "Solo un proveedor APROBADO puede publicar productos (proveedor "
                            + proveedorId + " en estado "
                            + (proveedor != null ? proveedor.estado() : "desconocido") + ")");
        }
    }

    public List<Producto> listar(String categoria, Integer proveedorId) {
        if (categoria != null && proveedorId != null) {
            return productoRepository.findByCategoriaNombreAndProveedorId(categoria, proveedorId);
        }
        if (categoria != null) {
            return productoRepository.findByCategoriaNombre(categoria);
        }
        if (proveedorId != null) {
            return productoRepository.findByProveedorId(proveedorId);
        }
        return productoRepository.findAll();
    }

    public Producto obtenerPorId(int id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado para id: " + id));
    }

    public Producto actualizar(int id, String nombre, String descripcion, int precio,
            int stock, EstadoProducto estado) {
        Producto producto = obtenerPorId(id);
        producto.setNombre(nombre);
        producto.setDescripcion(descripcion);
        producto.setPrecio(precio);
        producto.setStock(stock);
        producto.setEstado(estado);
        return productoRepository.save(producto);
    }

    /**
     * Descuenta stock al concretarse una venta (lo invoca el servicio ventas).
     * El stock nunca queda negativo.
     */
    public Producto descontarStock(int id, int cantidad) {
        Producto producto = obtenerPorId(id);

        if (producto.getEstado() != EstadoProducto.ACTIVO) {
            throw new BusinessConflictException(
                    "El producto " + id + " no está ACTIVO en el catálogo");
        }
        if (producto.getStock() < cantidad) {
            throw new BusinessConflictException(
                    "Stock insuficiente para el producto " + id + ": disponible "
                            + producto.getStock() + ", solicitado " + cantidad);
        }
        producto.setStock(producto.getStock() - cantidad);
        return productoRepository.save(producto);
    }

    /**
     * Precio con la oferta activa y vigente aplicada (si la hay):
     * PORCENTAJE descuenta ese % del precio; MONTO_FIJO resta ese monto en CLP.
     */
    public int precioVigente(Producto producto) {
        LocalDate hoy = LocalDate.now();
        return ofertaRepository.findByProductoIdAndActivaTrue(producto.getId()).stream()
                .filter(o -> !hoy.isBefore(o.getFechaInicio()) && !hoy.isAfter(o.getFechaFin()))
                .findFirst()
                .map(o -> switch (o.getTipoOferta()) {
                    case PORCENTAJE -> producto.getPrecio() - (producto.getPrecio() * o.getValor() / 100);
                    case MONTO_FIJO -> Math.max(0, producto.getPrecio() - o.getValor());
                })
                .orElse(producto.getPrecio());
    }

    /**
     * Crea una oferta; una sola activa por producto y vigencia coherente.
     */
    public Oferta crearOferta(int productoId, Oferta oferta) {
        Producto producto = obtenerPorId(productoId);

        if (!ofertaRepository.findByProductoIdAndActivaTrue(productoId).isEmpty()) {
            throw new BusinessConflictException(
                    "El producto " + productoId + " ya tiene una oferta activa");
        }
        if (oferta.getFechaFin().isBefore(oferta.getFechaInicio())) {
            throw new BusinessConflictException(
                    "La fecha de fin de la oferta no puede ser anterior a la de inicio");
        }
        oferta.setProducto(producto);
        oferta.setActiva(true);
        return ofertaRepository.save(oferta);
    }

    public List<Oferta> listarOfertas(int productoId) {
        obtenerPorId(productoId);
        return ofertaRepository.findByProductoId(productoId);
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    public Categoria crearCategoria(Categoria categoria) {
        if (categoriaRepository.existsByNombre(categoria.getNombre())) {
            throw new BusinessConflictException(
                    "Ya existe la categoría: " + categoria.getNombre());
        }
        return categoriaRepository.save(categoria);
    }

    public Categoria obtenerCategoria(int id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoría no encontrada para id: " + id));
    }
}
