package com.example.despacho.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.despacho.model.Seguimiento;

@Repository
public interface SeguimientoRepository extends JpaRepository<Seguimiento, Integer> {

    Optional<Seguimiento> findByVentaId(int ventaId);

    boolean existsByVentaId(int ventaId);

    List<Seguimiento> findByClienteId(int clienteId);

    List<Seguimiento> findByProveedorId(int proveedorId);
}
