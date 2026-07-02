package com.example.proveedores.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.proveedores.dto.CambioEstadoRequest;
import com.example.proveedores.dto.CreateDocumentoRequest;
import com.example.proveedores.dto.CreateProveedorRequest;
import com.example.proveedores.dto.DocumentoResponse;
import com.example.proveedores.dto.ProveedorResponse;
import com.example.proveedores.mapper.ProveedorMapper;
import com.example.proveedores.model.DocumentoProveedor;
import com.example.proveedores.model.EstadoProveedor;
import com.example.proveedores.model.Proveedor;
import com.example.proveedores.service.ProveedorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @PostMapping
    public ResponseEntity<ProveedorResponse> postular(
            @Valid @RequestBody CreateProveedorRequest request) {
        Proveedor proveedor = proveedorService.postular(ProveedorMapper.toModel(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProveedorMapper.toResponse(proveedor));
    }

    @GetMapping
    public ResponseEntity<List<ProveedorResponse>> listar(
            @RequestParam(name = "estado", required = false) EstadoProveedor estado) {
        List<ProveedorResponse> proveedores = proveedorService.listar(estado).stream()
                .map(ProveedorMapper::toResponse)
                .toList();
        return ResponseEntity.ok(proveedores);
    }

    @GetMapping("{id}")
    public ResponseEntity<ProveedorResponse> buscarPorId(@PathVariable int id) {
        return ResponseEntity.ok(ProveedorMapper.toResponse(proveedorService.obtenerPorId(id)));
    }

    /**
     * Resuelve la postulación (lo invoca el servicio administrador vía WebClient).
     */
    @PatchMapping("{id}/estado")
    public ResponseEntity<ProveedorResponse> cambiarEstado(@PathVariable int id,
            @Valid @RequestBody CambioEstadoRequest request) {
        Proveedor proveedor = proveedorService.cambiarEstado(
                id, request.estado(), request.observaciones());
        return ResponseEntity.ok(ProveedorMapper.toResponse(proveedor));
    }

    @GetMapping("{id}/documentos")
    public ResponseEntity<List<DocumentoResponse>> listarDocumentos(@PathVariable int id) {
        List<DocumentoResponse> documentos = proveedorService.listarDocumentos(id).stream()
                .map(ProveedorMapper::toResponse)
                .toList();
        return ResponseEntity.ok(documentos);
    }

    @PostMapping("{id}/documentos")
    public ResponseEntity<DocumentoResponse> agregarDocumento(@PathVariable int id,
            @Valid @RequestBody CreateDocumentoRequest request) {
        DocumentoProveedor documento = new DocumentoProveedor();
        documento.setTipo(request.tipo());
        documento.setNombreArchivo(request.nombreArchivo());
        documento.setUrl(request.url());

        DocumentoProveedor guardado = proveedorService.agregarDocumento(id, documento);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProveedorMapper.toResponse(guardado));
    }
}
