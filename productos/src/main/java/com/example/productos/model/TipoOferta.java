package com.example.productos.model;

/**
 * Tipo de descuento de una oferta:
 * PORCENTAJE — valor es el % de descuento sobre el precio (0-100).
 * MONTO_FIJO — valor es el descuento en CLP que se resta al precio.
 */
public enum TipoOferta {
    PORCENTAJE,
    MONTO_FIJO
}
