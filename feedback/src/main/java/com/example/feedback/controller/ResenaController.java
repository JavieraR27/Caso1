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

import jakarta.validation.Valid;

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
    @PostMapping
    public ResponseEntity<ResenaResponse> crear(@Valid @RequestBody CreateResenaRequest request) {
        Resena resena = resenaService.crear(ResenaMapper.toModel(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ResenaMapper.toResponse(resena));
    }

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
    @GetMapping("/producto/{productoId}/promedio")
    public ResponseEntity<PromedioResponse> promedio(@PathVariable int productoId) {
        return ResponseEntity.ok(resenaService.promedio(productoId));
    }

    /**
     * Moderación (acción exclusiva del administrador).
     */
    @DeleteMapping("{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        resenaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
