package com.example.productos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.productos.exception.BusinessConflictException;
import com.example.productos.model.Categoria;
import com.example.productos.model.EstadoProducto;
import com.example.productos.model.Oferta;
import com.example.productos.model.Producto;
import com.example.productos.model.TipoOferta;
import com.example.productos.repository.CategoriaRepository;
import com.example.productos.repository.OfertaRepository;
import com.example.productos.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private OfertaRepository ofertaRepository;

    @Mock
    private WebClient proveedoresWebClient;

    @InjectMocks
    private ProductoService productoService;

    private Producto taladro(int stock, EstadoProducto estado) {
        Producto p = new Producto();
        p.setId(1);
        p.setProveedorId(1);
        p.setCategoria(new Categoria(1, "Herramientas"));
        p.setNombre("Taladro percutor");
        p.setPrecio(50000);
        p.setStock(stock);
        p.setEstado(estado);
        return p;
    }

    private Oferta oferta(TipoOferta tipo, int valor, LocalDate inicio, LocalDate fin) {
        Oferta o = new Oferta();
        o.setTipoOferta(tipo);
        o.setValor(valor);
        o.setFechaInicio(inicio);
        o.setFechaFin(fin);
        o.setActiva(true);
        return o;
    }

    @Test
    void descontarStock_insuficiente_lanza409() {
        when(productoRepository.findById(1))
                .thenReturn(Optional.of(taladro(2, EstadoProducto.ACTIVO)));

        assertThatThrownBy(() -> productoService.descontarStock(1, 5))
                .isInstanceOf(BusinessConflictException.class);
        verify(productoRepository, never()).save(any());
    }

    @Test
    void descontarStock_productoInactivo_lanza409() {
        when(productoRepository.findById(1))
                .thenReturn(Optional.of(taladro(10, EstadoProducto.INACTIVO)));

        assertThatThrownBy(() -> productoService.descontarStock(1, 1))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void descontarStock_ok_restaLaCantidad() {
        when(productoRepository.findById(1))
                .thenReturn(Optional.of(taladro(10, EstadoProducto.ACTIVO)));
        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Producto actualizado = productoService.descontarStock(1, 3);

        assertThat(actualizado.getStock()).isEqualTo(7);
    }

    @Test
    void precioVigente_sinOferta_devuelveElPrecioBase() {
        when(ofertaRepository.findByProductoIdAndActivaTrue(1)).thenReturn(List.of());

        assertThat(productoService.precioVigente(taladro(10, EstadoProducto.ACTIVO)))
                .isEqualTo(50000);
    }

    @Test
    void precioVigente_ofertaPorcentajeVigente_descuentaElPorcentaje() {
        when(ofertaRepository.findByProductoIdAndActivaTrue(1)).thenReturn(List.of(
                oferta(TipoOferta.PORCENTAJE, 20,
                        LocalDate.now().minusDays(1), LocalDate.now().plusDays(1))));

        assertThat(productoService.precioVigente(taladro(10, EstadoProducto.ACTIVO)))
                .isEqualTo(40000);
    }

    @Test
    void precioVigente_ofertaMontoFijoVigente_restaElMonto() {
        when(ofertaRepository.findByProductoIdAndActivaTrue(1)).thenReturn(List.of(
                oferta(TipoOferta.MONTO_FIJO, 15000,
                        LocalDate.now().minusDays(1), LocalDate.now().plusDays(1))));

        assertThat(productoService.precioVigente(taladro(10, EstadoProducto.ACTIVO)))
                .isEqualTo(35000);
    }

    @Test
    void precioVigente_ofertaVencida_noAplica() {
        when(ofertaRepository.findByProductoIdAndActivaTrue(1)).thenReturn(List.of(
                oferta(TipoOferta.PORCENTAJE, 20,
                        LocalDate.now().minusDays(10), LocalDate.now().minusDays(5))));

        assertThat(productoService.precioVigente(taladro(10, EstadoProducto.ACTIVO)))
                .isEqualTo(50000);
    }

    @Test
    void crearOferta_yaHayUnaActiva_lanza409() {
        when(productoRepository.findById(1))
                .thenReturn(Optional.of(taladro(10, EstadoProducto.ACTIVO)));
        when(ofertaRepository.findByProductoIdAndActivaTrue(1)).thenReturn(List.of(
                oferta(TipoOferta.PORCENTAJE, 10, LocalDate.now(), LocalDate.now().plusDays(5))));

        assertThatThrownBy(() -> productoService.crearOferta(1,
                oferta(TipoOferta.MONTO_FIJO, 5000, LocalDate.now(), LocalDate.now().plusDays(3))))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void crearOferta_fechasInvertidas_lanza409() {
        when(productoRepository.findById(1))
                .thenReturn(Optional.of(taladro(10, EstadoProducto.ACTIVO)));
        when(ofertaRepository.findByProductoIdAndActivaTrue(1)).thenReturn(List.of());

        assertThatThrownBy(() -> productoService.crearOferta(1,
                oferta(TipoOferta.PORCENTAJE, 10,
                        LocalDate.now().plusDays(5), LocalDate.now())))
                .isInstanceOf(BusinessConflictException.class);
    }
}
