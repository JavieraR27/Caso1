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
import com.example.proveedores.dto.LoginProveedorRequest;
import com.example.proveedores.dto.LoginResponse;
import com.example.proveedores.dto.ProveedorResponse;
import com.example.proveedores.mapper.ProveedorMapper;
import com.example.proveedores.model.DocumentoProveedor;
import com.example.proveedores.model.EstadoProveedor;
import com.example.proveedores.model.Proveedor;
import com.example.proveedores.security.JwtUtil;
import com.example.proveedores.service.ProveedorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Proveedores",
        description = "Postulación, aprobación, login y documentos de los vendedores del "
                + "marketplace Paris (ferreterías y otros comercios)")
@RestController
@RequestMapping("/api/v1/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;
    private final JwtUtil jwtUtil;

    public ProveedorController(ProveedorService proveedorService, JwtUtil jwtUtil) {
        this.proveedorService = proveedorService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Login del vendedor (emite JWT con rol PROVEEDOR; exige estado APROBADO).
     */
    @Operation(summary = "Autentica un proveedor y entrega su JWT",
            description = "Emite un token con rol PROVEEDOR; solo un proveedor en estado APROBADO "
                    + "puede iniciar sesión. Endpoint público: no requiere token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto; token JWT y datos del proveedor"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "El email no corresponde a ningún proveedor"),
            @ApiResponse(responseCode = "409", description = "Password incorrecta o proveedor aún no APROBADO")})
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciales del vendedor",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"email\": \"ventas@ferreteriaelmartillo.cl\", "
                                    + "\"password\": \"Fierro2026\"}")))
            @Valid @RequestBody LoginProveedorRequest request) {
        Proveedor proveedor = proveedorService.login(request.email(), request.password());
        String token = jwtUtil.generar(String.valueOf(proveedor.getId()), "PROVEEDOR");
        return ResponseEntity.ok(new LoginResponse(token, ProveedorMapper.toResponse(proveedor)));
    }

    @Operation(summary = "Registra la postulación de un vendedor",
            description = "El vendedor queda en estado POSTULADO hasta que el administrador "
                    + "resuelva. Endpoint público: no requiere token.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Postulación registrada en estado POSTULADO"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "409", description = "Ya existe un proveedor con ese rut o email")})
    @PostMapping
    public ResponseEntity<ProveedorResponse> postular(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la postulación del vendedor",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"rut\": \"76543210-K\", \"razonSocial\": \"Ferretería Cóndor\", "
                                    + "\"email\": \"condor@ferreteria.cl\", \"password\": \"Condor2026\", "
                                    + "\"telefono\": \"+56911111111\"}")))
            @Valid @RequestBody CreateProveedorRequest request) {
        Proveedor proveedor = proveedorService.postular(ProveedorMapper.toModel(request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProveedorMapper.toResponse(proveedor));
    }

    @Operation(summary = "Lista los proveedores",
            description = "Filtro opcional por estado (?estado=POSTULADO es la bandeja del "
                    + "administrador). Requiere rol PROVEEDOR, ADMINISTRADOR o INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado (puede ser vacío)")})
    @GetMapping
    public ResponseEntity<List<ProveedorResponse>> listar(
            @RequestParam(name = "estado", required = false) EstadoProveedor estado) {
        List<ProveedorResponse> proveedores = proveedorService.listar(estado).stream()
                .map(ProveedorMapper::toResponse)
                .toList();
        return ResponseEntity.ok(proveedores);
    }

    @Operation(summary = "Busca un proveedor por id",
            description = "Lo consume productos para validar que solo un proveedor APROBADO "
                    + "publique. Requiere rol PROVEEDOR, ADMINISTRADOR o INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Proveedor encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe proveedor con ese id")})
    @GetMapping("{id}")
    public ResponseEntity<ProveedorResponse> buscarPorId(@PathVariable int id) {
        return ResponseEntity.ok(ProveedorMapper.toResponse(proveedorService.obtenerPorId(id)));
    }

    /**
     * Resuelve la postulación (lo invoca el servicio administrador vía WebClient).
     */
    @Operation(summary = "Aprueba o rechaza la postulación",
            description = "Solo se resuelve un proveedor POSTULADO; el rechazo exige "
                    + "observaciones. Lo invoca el servicio administrador vía WebClient. "
                    + "Requiere rol ADMINISTRADOR o INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Postulación resuelta (APROBADO o RECHAZADO)"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "No existe proveedor con ese id"),
            @ApiResponse(responseCode = "409", description = "Ya estaba resuelta, estado destino inválido o rechazo sin observaciones")})
    @PatchMapping("{id}/estado")
    public ResponseEntity<ProveedorResponse> cambiarEstado(@PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Resolución de la postulación",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"estado\": \"APROBADO\", \"observaciones\": \"Documentación en regla\"}")))
            @Valid @RequestBody CambioEstadoRequest request) {
        Proveedor proveedor = proveedorService.cambiarEstado(
                id, request.estado(), request.observaciones());
        return ResponseEntity.ok(ProveedorMapper.toResponse(proveedor));
    }

    @Operation(summary = "Lista los documentos de la postulación",
            description = "Requiere rol PROVEEDOR, ADMINISTRADOR o INTERNO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documentos adjuntos (puede ser vacío)"),
            @ApiResponse(responseCode = "404", description = "No existe proveedor con ese id")})
    @GetMapping("{id}/documentos")
    public ResponseEntity<List<DocumentoResponse>> listarDocumentos(@PathVariable int id) {
        List<DocumentoResponse> documentos = proveedorService.listarDocumentos(id).stream()
                .map(ProveedorMapper::toResponse)
                .toList();
        return ResponseEntity.ok(documentos);
    }

    @Operation(summary = "Adjunta un documento a la postulación",
            description = "Solo mientras la postulación está POSTULADA. Requiere rol PROVEEDOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Documento adjuntado"),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido (Bean Validation)"),
            @ApiResponse(responseCode = "404", description = "No existe proveedor con ese id"),
            @ApiResponse(responseCode = "409", description = "La postulación ya fue resuelta")})
    @PostMapping("{id}/documentos")
    public ResponseEntity<DocumentoResponse> agregarDocumento(@PathVariable int id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Documento de respaldo de la postulación",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"tipo\": \"PATENTE_COMERCIAL\", \"nombreArchivo\": \"patente-2026.pdf\", "
                                    + "\"url\": \"https://drive.google.com/patente-condor\"}")))
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
