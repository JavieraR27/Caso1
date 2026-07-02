package com.example.proveedores.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.proveedores.model.DocumentoProveedor;

@Repository
public interface DocumentoProveedorRepository extends JpaRepository<DocumentoProveedor, Integer> {

    List<DocumentoProveedor> findByProveedorId(int proveedorId);
}
