package com.example.productos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.productos.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findByCategoriaNombre(String nombre);

    List<Producto> findByProveedorId(int proveedorId);

    List<Producto> findByCategoriaNombreAndProveedorId(String nombre, int proveedorId);
}
