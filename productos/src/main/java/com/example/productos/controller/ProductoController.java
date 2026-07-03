package com.example.productos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.productos.dto.CreateOfertaRequest;
import com.example.productos.dto.CreateProductoRequest;
import com.example.productos.dto.DescuentoStockRequest;
import com.example.productos.dto.OfertaResponse;
import com.example.productos.dto.ProductoResponse;
import com.example.productos.dto.UpdateProductoRequest;
import com.example.productos.mapper.ProductoMapper;
import com.example.productos.model.Oferta;
import com.example.productos.model.Producto;
import com.example.productos.service.ProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Productos",
        description = "Catálogo del marketplace Paris: publicación por proveedores APROBADOS, "
                + "precio vigente por oferta activa, stock y ofertas")
@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Publica un producto — valida vía WebClient que el proveedor esté APROBADO.
     */
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto publicado (ACTIVO)"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "La categoría o el proveedor no existen"),
            @ApiResponse(responseCode = "409", description = "El proveedor no está APROBADO"),
            @ApiResponse(responseCode = "503", description = "Servicio proveedores no disponible")})
    @Operation(summary = "Publica un producto en el catálogo",
            description = "Validación cruzada WebClient: solo un proveedor APROBADO puede "
                    + "publicar (se consulta al servicio proveedores). Requiere rol PROVEEDOR.")
    @PostMapping
    public ResponseEntity<ProductoResponse> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Producto a publicar",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"proveedorId\": 1, \"categoriaId\": 1, "
                                    + "\"nombre\": \"Taladro percutor 750W\", "
                                    + "\"descripcion\": \"Con maletín y set de brocas\", "
                                    + "\"precio\": 50000, \"stock\": 10}")))
            @Valid @RequestBody CreateProductoRequest request) {
        Producto producto = productoService.crear(
                ProductoMapper.toModel(request), request.categoriaId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductoMapper.toResponse(producto, productoService.precioVigente(producto)));
    }

    @Operation(summary = "Lista el catálogo",
            description = "Filtros opcionales por nombre de categoría y proveedor; cada producto "
                    + "incluye su precio vigente (con oferta aplicada). Endpoint público.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catálogo (puede ser vacío)")})
    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listar(
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "proveedorId", required = false) Integer proveedorId) {
        List<ProductoResponse> productos = productoService.listar(categoria, proveedorId).stream()
                .map(p -> ProductoMapper.toResponse(p, productoService.precioVigente(p)))
                .toList();
        return ResponseEntity.ok(productos);
    }

    /**
     * Detalle con precio vigente (lo consumen ventas y feedback).
     */
    @Operation(summary = "Busca un producto por id",
            description = "Detalle con precio vigente. Lo consumen ventas (precio/stock al "
                    + "crear la orden) y feedback. Endpoint público.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto con precio vigente"),
            @ApiResponse(responseCode = "404", description = "No existe producto con ese id")})
    @GetMapping("{id}")
    public ResponseEntity<ProductoResponse> buscarPorId(@PathVariable int id) {
        Producto producto = productoService.obtenerPorId(id);
        return ResponseEntity.ok(
                ProductoMapper.toResponse(producto, productoService.precioVigente(producto)));
    }

    @Operation(summary = "Edita un producto",
            description = "El vendedor actualiza nombre, precio, stock y estado. Requiere rol PROVEEDOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "No existe producto con ese id")})
    @PutMapping("{id}")
    public ResponseEntity<ProductoResponse> actualizar(@PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos actualizados del producto",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"nombre\": \"Taladro percutor 750W\", \"descripcion\": \"Con maletín\", "
                                    + "\"precio\": 48000, \"stock\": 12, \"estado\": \"ACTIVO\"}")))
            @Valid @RequestBody UpdateProductoRequest request) {
        Producto producto = productoService.actualizar(id, request.nombre(),
                request.descripcion(), request.precio(), request.stock(), request.estado());
        return ResponseEntity.ok(
                ProductoMapper.toResponse(producto, productoService.precioVigente(producto)));
    }

    /**
     * Descuenta stock al concretarse una venta (lo invoca el servicio ventas).
     */
    @Operation(summary = "Descuenta stock",
            description = "Lo invoca ventas al concretarse una compra; el stock nunca queda "
                    + "negativo. Requiere rol INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock descontado"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "No existe producto con ese id"),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente o producto no ACTIVO")})
    @PatchMapping("{id}/stock")
    public ResponseEntity<ProductoResponse> descontarStock(@PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Cantidad a descontar",
                    content = @Content(examples = @ExampleObject(value = "{\"cantidad\": 2}")))
            @Valid @RequestBody DescuentoStockRequest request) {
        Producto producto = productoService.descontarStock(id, request.cantidad());
        return ResponseEntity.ok(
                ProductoMapper.toResponse(producto, productoService.precioVigente(producto)));
    }

    @Operation(summary = "Lista las ofertas de un producto",
            description = "Historial de ofertas del producto. Endpoint público.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ofertas del producto (puede ser vacío)"),
            @ApiResponse(responseCode = "404", description = "No existe producto con ese id")})
    @GetMapping("{id}/ofertas")
    public ResponseEntity<List<OfertaResponse>> listarOfertas(@PathVariable int id) {
        List<OfertaResponse> ofertas = productoService.listarOfertas(id).stream()
                .map(ProductoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ofertas);
    }

    @Operation(summary = "Crea una oferta para un producto",
            description = "Una sola oferta activa por producto; PORCENTAJE descuenta ese % y "
                    + "MONTO_FIJO resta ese monto en CLP del precio. Requiere rol PROVEEDOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Oferta creada y activa"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "No existe producto con ese id"),
            @ApiResponse(responseCode = "409", description = "Ya hay una oferta activa o la vigencia es inválida")})
    @PostMapping("{id}/ofertas")
    public ResponseEntity<OfertaResponse> crearOferta(@PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Oferta con tipo, valor y vigencia",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"tipoOferta\": \"PORCENTAJE\", \"valor\": 20, "
                                    + "\"fechaInicio\": \"2026-07-03\", \"fechaFin\": \"2026-07-10\"}")))
            @Valid @RequestBody CreateOfertaRequest request) {
        Oferta oferta = new Oferta();
        oferta.setTipoOferta(request.tipoOferta());
        oferta.setValor(request.valor());
        oferta.setFechaInicio(request.fechaInicio());
        oferta.setFechaFin(request.fechaFin());

        Oferta guardada = productoService.crearOferta(id, oferta);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductoMapper.toResponse(guardada));
    }
}
