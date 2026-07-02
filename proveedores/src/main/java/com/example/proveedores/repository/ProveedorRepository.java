package com.example.proveedores.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.proveedores.model.EstadoProveedor;
import com.example.proveedores.model.Proveedor;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

    List<Proveedor> findByEstado(EstadoProveedor estado);

    boolean existsByRut(String rut);

    boolean existsByEmail(String email);
}
