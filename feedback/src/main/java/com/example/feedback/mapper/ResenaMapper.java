package com.example.feedback.mapper;

import com.example.feedback.dto.CreateResenaRequest;
import com.example.feedback.dto.ResenaResponse;
import com.example.feedback.model.Resena;

/**
 * Convierte entre los DTOs y la entidad Resena. La fecha la define el
 * service; la compra verificada se valida antes de mapear.
 */
public class ResenaMapper {

    public static Resena toModel(CreateResenaRequest request) {
        Resena resena = new Resena();
        resena.setProductoId(request.productoId());
        resena.setClienteId(request.clienteId());
        resena.setVentaId(request.ventaId());
        resena.setCalificacion(request.calificacion());
        resena.setComentario(request.comentario());
        return resena;
    }

    public static ResenaResponse toResponse(Resena resena) {
        return new ResenaResponse(
                resena.getId(),
                resena.getProductoId(),
                resena.getClienteId(),
                resena.getVentaId(),
                resena.getCalificacion(),
                resena.getComentario(),
                resena.getFechaCreacion());
    }
}
