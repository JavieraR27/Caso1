package com.example.clientes.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import com.example.clientes.dto.ClienteLegacyResponse;
import com.example.clientes.exception.BusinessConflictException;
import com.example.clientes.exception.ResourceNotFoundException;
import com.example.clientes.exception.ServiceUnavailableException;
import com.example.clientes.model.Cliente;
import com.example.clientes.model.Direccion;
import com.example.clientes.model.TipoCliente;
import com.example.clientes.repository.ClienteRepository;
import com.example.clientes.repository.DireccionRepository;

import reactor.core.publisher.Mono;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final DireccionRepository direccionRepository;
    private final WebClient legacyWebClient;

    public ClienteService(ClienteRepository clienteRepository,
            DireccionRepository direccionRepository,
            @Qualifier("legacyWebClient") WebClient legacyWebClient) {
        this.clienteRepository = clienteRepository;
        this.direccionRepository = direccionRepository;
        this.legacyWebClient = legacyWebClient;
    }

    /**
     * Registro directo de un cliente nuevo (Pedro).
     */
    public Cliente registrar(Cliente cliente) {
        if (clienteRepository.existsByEmail(cliente.getEmail())) {
            throw new BusinessConflictException(
                    "Ya existe un cliente con el email: " + cliente.getEmail());
        }
        cliente.setTipo(TipoCliente.NUEVO);
        cliente.setFechaCreacion(LocalDateTime.now());
        return clienteRepository.save(cliente);
    }

    /**
     * Login del marketplace. Si el email no existe localmente, se validan las
     * credenciales contra el sistema legacy (validación cruzada) y, de ser
     * correctas, el cliente histórico se MIGRA conservando sus credenciales
     * (María entra sin re-registrarse).
     */
    public Cliente login(String email, String password) {
        Cliente local = clienteRepository.findByEmail(email).orElse(null);

        if (local != null) {
            if (!local.getPassword().equals(password)) {
                throw new BusinessConflictException(
                        "Credenciales inválidas para el email: " + email);
            }
            return local;
        }
        return migrarDesdeLegacy(email, password);
    }

    private Cliente migrarDesdeLegacy(String email, String password) {
        ClienteLegacyResponse legacy = validarEnLegacy(email, password);

        Cliente migrado = new Cliente();
        migrado.setEmail(legacy.email());
        migrado.setPassword(password);
        migrado.setNombre(legacy.nombre());
        migrado.setTipo(TipoCliente.MIGRADO);
        migrado.setLegacyId(legacy.id());
        migrado.setFechaCreacion(LocalDateTime.now());
        return clienteRepository.save(migrado);
    }

    private ClienteLegacyResponse validarEnLegacy(String email, String password) {
        try {
            return legacyWebClient.post()
                    .uri("/api/v1/legacy/validaciones")
                    .bodyValue(new CredencialesLegacy(email, password))
                    .retrieve()
                    .onStatus(s -> s.value() == 404,
                            r -> Mono.error(new ResourceNotFoundException(
                                    "Cliente no encontrado (ni local ni legacy) para el email: "
                                            + email)))
                    .onStatus(s -> s.value() == 409,
                            r -> Mono.error(new BusinessConflictException(
                                    "Credenciales inválidas para el email: " + email)))
                    .bodyToMono(ClienteLegacyResponse.class)
                    .block();
        } catch (WebClientRequestException e) {
            throw new ServiceUnavailableException("Servicio legacy no disponible", e);
        }
    }

    /** Cuerpo del POST al servicio legacy. */
    private record CredencialesLegacy(String email, String password) {}

    public Cliente obtenerPorId(int id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado para id: " + id));
    }

    public Cliente actualizar(int id, String nombre, String telefono) {
        Cliente cliente = obtenerPorId(id);
        cliente.setNombre(nombre);
        cliente.setTelefono(telefono);
        return clienteRepository.save(cliente);
    }

    public Direccion agregarDireccion(int clienteId, Direccion direccion) {
        Cliente cliente = obtenerPorId(clienteId);
        direccion.setCliente(cliente);
        return direccionRepository.save(direccion);
    }

    public List<Direccion> listarDirecciones(int clienteId) {
        obtenerPorId(clienteId);
        return direccionRepository.findByClienteId(clienteId);
    }
}
