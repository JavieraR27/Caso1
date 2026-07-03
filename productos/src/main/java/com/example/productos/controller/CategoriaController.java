package com.example.productos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.productos.dto.CreateCategoriaRequest;
import com.example.productos.model.Categoria;
import com.example.productos.service.ProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Categorías",
        description = "Categorías del marketplace de mejoramiento del hogar; insumo del "
                + "reporte semanal de ventas por categoría")
@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaController {

    private final ProductoService productoService;

    public CategoriaController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Operation(summary = "Lista las categorías",
            description = "Categorías disponibles para publicar y filtrar el catálogo. Endpoint público.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de categorías")})
    @GetMapping
    public ResponseEntity<List<Categoria>> listar() {
        return ResponseEntity.ok(productoService.listarCategorias());
    }

    @Operation(summary = "Crea una categoría",
            description = "Alta de una categoría del marketplace. Requiere rol ADMINISTRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoría creada"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "409", description = "Ya existe una categoría con ese nombre")})
    @PostMapping
    public ResponseEntity<Categoria> crear(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nombre de la nueva categoría",
                    content = @Content(examples = @ExampleObject(value = "{\"nombre\": \"Herramientas\"}")))
            @Valid @RequestBody CreateCategoriaRequest request) {
        Categoria categoria = productoService.crearCategoria(
                new Categoria(0, request.nombre()));
        return ResponseEntity.status(HttpStatus.CREATED).body(categoria);
    }
}
