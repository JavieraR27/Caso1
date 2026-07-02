package com.example.proveedores.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.proveedores.exception.BusinessConflictException;
import com.example.proveedores.exception.ResourceNotFoundException;
import com.example.proveedores.model.DocumentoProveedor;
import com.example.proveedores.model.EstadoProveedor;
import com.example.proveedores.model.Proveedor;
import com.example.proveedores.repository.DocumentoProveedorRepository;
import com.example.proveedores.repository.ProveedorRepository;

@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final DocumentoProveedorRepository documentoProveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository,
            DocumentoProveedorRepository documentoProveedorRepository) {
        this.proveedorRepository = proveedorRepository;
        this.documentoProveedorRepository = documentoProveedorRepository;
    }

    /**
     * Registra la postulación de un vendedor. Queda en estado POSTULADO hasta
     * que el administrador la resuelva.
     */
    public Proveedor postular(Proveedor proveedor) {
        if (proveedorRepository.existsByRut(proveedor.getRut())) {
            throw new BusinessConflictException(
                    "Ya existe un proveedor con el rut: " + proveedor.getRut());
        }
        if (proveedorRepository.existsByEmail(proveedor.getEmail())) {
            throw new BusinessConflictException(
                    "Ya existe un proveedor con el email: " + proveedor.getEmail());
        }
        proveedor.setEstado(EstadoProveedor.POSTULADO);
        proveedor.setFechaPostulacion(LocalDateTime.now());
        return proveedorRepository.save(proveedor);
    }

    public List<Proveedor> listar(EstadoProveedor estado) {
        if (estado == null) {
            return proveedorRepository.findAll();
        }
        return proveedorRepository.findByEstado(estado);
    }

    public Proveedor obtenerPorId(int id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proveedor no encontrado para id: " + id));
    }

    /**
     * Resuelve la postulación (lo invoca el servicio administrador).
     * Solo se resuelve un proveedor POSTULADO; el rechazo exige observaciones.
     */
    public Proveedor cambiarEstado(int id, EstadoProveedor nuevoEstado, String observaciones) {
        Proveedor proveedor = obtenerPorId(id);

        if (proveedor.getEstado() != EstadoProveedor.POSTULADO) {
            throw new BusinessConflictException(
                    "La postulación ya fue resuelta: el proveedor " + id
                            + " está en estado " + proveedor.getEstado());
        }
        if (nuevoEstado != EstadoProveedor.APROBADO && nuevoEstado != EstadoProveedor.RECHAZADO) {
            throw new BusinessConflictException(
                    "El nuevo estado debe ser APROBADO o RECHAZADO");
        }
        if (nuevoEstado == EstadoProveedor.RECHAZADO
                && (observaciones == null || observaciones.isBlank())) {
            throw new BusinessConflictException(
                    "El rechazo de una postulación exige observaciones");
        }
        proveedor.setEstado(nuevoEstado);
        proveedor.setObservaciones(observaciones);
        proveedor.setFechaResolucion(LocalDateTime.now());
        return proveedorRepository.save(proveedor);
    }

    /**
     * Adjunta un documento a la postulación; solo mientras está POSTULADO.
     */
    public DocumentoProveedor agregarDocumento(int proveedorId, DocumentoProveedor documento) {
        Proveedor proveedor = obtenerPorId(proveedorId);

        if (proveedor.getEstado() != EstadoProveedor.POSTULADO) {
            throw new BusinessConflictException(
                    "No se pueden adjuntar documentos: la postulación del proveedor "
                            + proveedorId + " ya fue resuelta");
        }
        documento.setProveedor(proveedor);
        documento.setFechaCarga(LocalDateTime.now());
        return documentoProveedorRepository.save(documento);
    }

    public List<DocumentoProveedor> listarDocumentos(int proveedorId) {
        obtenerPorId(proveedorId);
        return documentoProveedorRepository.findByProveedorId(proveedorId);
    }
}
