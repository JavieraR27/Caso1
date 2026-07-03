package com.example.feedback.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.feedback.dto.CreateResenaRequest;
import com.example.feedback.dto.PromedioResponse;
import com.example.feedback.dto.ResenaResponse;
import com.example.feedback.mapper.ResenaMapper;
import com.example.feedback.model.Resena;
import com.example.feedback.service.ResenaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Reseñas",
        description = "Feedback del marketplace Paris: reseñas con compra verificada contra "
                + "ventas y productos, reputación por producto (promedio de calificaciones) "
                + "y moderación del administrador")
@RestController
@RequestMapping("/api/v1/resenas")
public class ResenaController {

    private final ResenaService resenaService;

    public ResenaController(ResenaService resenaService) {
        this.resenaService = resenaService;
    }

    /**
     * Publica una reseña con compra verificada (validada contra ventas y
     * productos vía WebClient).
     */
    @Operation(summary = "Publica una reseña con compra verificada",
            description = "Validaciones cruzadas WebClient: la venta existe en ventas, "
                    + "pertenece al cliente y contiene el producto reseñado; el producto "
                    + "existe en el catálogo de productos. Una sola reseña por (cliente, "
                    + "producto, venta). Solo puede llamarlo el rol CLIENTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reseña publicada"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "La venta o el producto no existen"),
            @ApiResponse(responseCode = "409", description = "Reseña duplicada, la venta no pertenece "
                    + "al cliente o el producto no está en la venta (compra no verificada)"),
            @ApiResponse(responseCode = "503", description = "Servicio ventas o productos no disponible")})
    @PostMapping
    public ResponseEntity<ResenaResponse> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Reseña de un producto comprado en una venta del cliente",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"productoId\": 1, \"clienteId\": 1, \"ventaId\": 1, "
                                    + "\"calificacion\": 4, "
                                    + "\"comentario\": \"El taladro Bauker 650W llegó a tiempo "
                                    + "y funciona muy bien; el maletín venía algo rayado.\"}")))
            @Valid @RequestBody CreateResenaRequest request) {
        Resena resena = resenaService.crear(ResenaMapper.toModel(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ResenaMapper.toResponse(resena));
    }

    @Operation(summary = "Lista las reseñas de un producto",
            description = "Reseñas publicadas del producto para la ficha del catálogo. "
                    + "Endpoint público (no requiere token).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de reseñas (puede ser vacío)")})
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<ResenaResponse>> listarPorProducto(@PathVariable int productoId) {
        List<ResenaResponse> resenas = resenaService.listarPorProducto(productoId).stream()
                .map(ResenaMapper::toResponse)
                .toList();
        return ResponseEntity.ok(resenas);
    }

    /**
     * Reputación del producto (promedio + total), para elegir entre ofertas.
     */
    @Operation(summary = "Calcula la reputación de un producto",
            description = "Promedio de calificaciones (0 si no hay reseñas) y total de "
                    + "reseñas, para comparar ofertas de distintos proveedores. Endpoint "
                    + "público (no requiere token).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Promedio y total de reseñas del producto")})
    @GetMapping("/producto/{productoId}/promedio")
    public ResponseEntity<PromedioResponse> promedio(@PathVariable int productoId) {
        return ResponseEntity.ok(resenaService.promedio(productoId));
    }

    /**
     * Moderación (acción exclusiva del administrador).
     */
    @Operation(summary = "Elimina una reseña inapropiada",
            description = "Moderación del marketplace: borra definitivamente la reseña. "
                    + "Solo puede llamarlo el rol ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reseña eliminada, sin cuerpo"),
            @ApiResponse(responseCode = "404", description = "No existe una reseña con ese id")})
    @DeleteMapping("{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        resenaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
