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

import jakarta.validation.Valid;

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
    @PostMapping
    public ResponseEntity<ProductoResponse> crear(
            @Valid @RequestBody CreateProductoRequest request) {
        Producto producto = productoService.crear(
                ProductoMapper.toModel(request), request.categoriaId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductoMapper.toResponse(producto, productoService.precioVigente(producto)));
    }

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
    @GetMapping("{id}")
    public ResponseEntity<ProductoResponse> buscarPorId(@PathVariable int id) {
        Producto producto = productoService.obtenerPorId(id);
        return ResponseEntity.ok(
                ProductoMapper.toResponse(producto, productoService.precioVigente(producto)));
    }

    @PutMapping("{id}")
    public ResponseEntity<ProductoResponse> actualizar(@PathVariable int id,
            @Valid @RequestBody UpdateProductoRequest request) {
        Producto producto = productoService.actualizar(id, request.nombre(),
                request.descripcion(), request.precio(), request.stock(), request.estado());
        return ResponseEntity.ok(
                ProductoMapper.toResponse(producto, productoService.precioVigente(producto)));
    }

    /**
     * Descuenta stock al concretarse una venta (lo invoca el servicio ventas).
     */
    @PatchMapping("{id}/stock")
    public ResponseEntity<ProductoResponse> descontarStock(@PathVariable int id,
            @Valid @RequestBody DescuentoStockRequest request) {
        Producto producto = productoService.descontarStock(id, request.cantidad());
        return ResponseEntity.ok(
                ProductoMapper.toResponse(producto, productoService.precioVigente(producto)));
    }

    @GetMapping("{id}/ofertas")
    public ResponseEntity<List<OfertaResponse>> listarOfertas(@PathVariable int id) {
        List<OfertaResponse> ofertas = productoService.listarOfertas(id).stream()
                .map(ProductoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ofertas);
    }

    @PostMapping("{id}/ofertas")
    public ResponseEntity<OfertaResponse> crearOferta(@PathVariable int id,
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
