package com.example.notificaciones.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.notificaciones.dto.CreateNotificacionRequest;
import com.example.notificaciones.dto.NotificacionResponse;
import com.example.notificaciones.mapper.NotificacionMapper;
import com.example.notificaciones.model.Notificacion;
import com.example.notificaciones.model.TipoNotificacion;
import com.example.notificaciones.service.NotificacionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    /**
     * Registra un aviso (lo invocan ventas, despacho, tickets y administrador
     * vía WebClient).
     */
    @PostMapping
    public ResponseEntity<NotificacionResponse> crear(
            @Valid @RequestBody CreateNotificacionRequest request) {
        Notificacion notificacion = notificacionService.crear(NotificacionMapper.toModel(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(NotificacionMapper.toResponse(notificacion));
    }

    /**
     * Bandeja del cliente/proveedor, con filtros opcionales combinables.
     */
    @GetMapping
    public ResponseEntity<List<NotificacionResponse>> listar(
            @RequestParam(name = "destinatarioId", required = false) Integer destinatarioId,
            @RequestParam(name = "tipo", required = false) TipoNotificacion tipo) {
        List<NotificacionResponse> notificaciones = notificacionService
                .listar(destinatarioId, tipo).stream()
                .map(NotificacionMapper::toResponse)
                .toList();
        return ResponseEntity.ok(notificaciones);
    }

    @GetMapping("{id}")
    public ResponseEntity<NotificacionResponse> buscarPorId(@PathVariable int id) {
        return ResponseEntity.ok(
                NotificacionMapper.toResponse(notificacionService.obtenerPorId(id)));
    }
}
