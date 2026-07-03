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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaController {

    private final ProductoService productoService;

    public CategoriaController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<Categoria>> listar() {
        return ResponseEntity.ok(productoService.listarCategorias());
    }

    @PostMapping
    public ResponseEntity<Categoria> crear(@Valid @RequestBody CreateCategoriaRequest request) {
        Categoria categoria = productoService.crearCategoria(
                new Categoria(0, request.nombre()));
        return ResponseEntity.status(HttpStatus.CREATED).body(categoria);
    }
}
