package com.empresa.pos.application.service;

import com.empresa.pos.application.dto.command.FiltroVentasCommand;
import com.empresa.pos.application.dto.command.PagoCommand;
import com.empresa.pos.application.dto.command.ReembolsarVentaCommand;
import com.empresa.pos.application.dto.command.RegistrarVentaCommand;
import com.empresa.pos.application.dto.response.DetalleVentaResponse;
import com.empresa.pos.application.dto.response.PagoResponse;
import com.empresa.pos.application.dto.response.ReembolsoResponse;
import com.empresa.pos.application.dto.response.VentaResponse;
import com.empresa.pos.application.port.in.venta.*;
import com.empresa.pos.application.port.out.*;
import com.empresa.pos.domain.exception.RecursoNoEncontradoException;
import com.empresa.pos.domain.exception.VentaYaReembolsadaException;
import com.empresa.pos.domain.model.*;
import com.empresa.pos.domain.service.GeneradorNumeroFactura;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para gestión de ventas.
 * Implementa registro, consulta, listado y reembolso de ventas.
 *
 * @version 3.2.0
 */
@Service
public class VentaService implements
        RegistrarVentaUseCase,
        ListarVentasUseCase,
        ObtenerVentaUseCase,
        ObtenerVentaPorFacturaUseCase,
        ReembolsarVentaUseCase {

    private static final Logger log = LoggerFactory.getLogger(VentaService.class);

    private final ProductoRepositoryPort productoRepository;
    private final VentaRepositoryPort ventaRepository;
    private final PagoVentaRepositoryPort pagoVentaRepository;
    private final ReembolsoRepositoryPort reembolsoRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final SecuenciaFacturaService secuenciaFacturaService;
    private final ConfiguracionService configuracionService;

    public VentaService(ProductoRepositoryPort productoRepository,
                        VentaRepositoryPort ventaRepository,
                        PagoVentaRepositoryPort pagoVentaRepository,
                        ReembolsoRepositoryPort reembolsoRepository,
                        UsuarioRepositoryPort usuarioRepository,
                        SecuenciaFacturaService secuenciaFacturaService,
                        ConfiguracionService configuracionService) {
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
        this.pagoVentaRepository = pagoVentaRepository;
        this.reembolsoRepository = reembolsoRepository;
        this.usuarioRepository = usuarioRepository;
        this.secuenciaFacturaService = secuenciaFacturaService;
        this.configuracionService = configuracionService;
    }

    // ─── 5.2: registrar ──────────────────────────────────────────────────────

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public VentaResponse registrar(RegistrarVentaCommand command) {
        // Obtener usuario autenticado desde JWT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", username));

        // Normalizar nombre del cliente (trim + espacios múltiples)
        String nombreCliente = command.getNombreCliente().trim().replaceAll("\\s+", " ");
        validarNombreCliente(nombreCliente);

        // Obtener tasa de impuesto global
        var tasaImpuesto = configuracionService.obtenerTasaImpuesto();

        // Generar número de factura único
        String numeroFactura = secuenciaFacturaService.obtenerSiguienteNumeroFactura();

        // Construir detalles con SELECT FOR UPDATE para stock
        List<DetalleVenta> detalles = command.getItems().stream().map(item -> {
            Producto producto = productoRepository.findByIdForUpdate(item.getProductoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto", item.getProductoId()));
            producto.descontarStock(item.getCantidad());
            productoRepository.save(producto);
            return new DetalleVenta(producto, item.getCantidad());
        }).collect(Collectors.toList());

        // Construir pagos
        List<Pago> pagos = command.getPagos().stream()
                .map(p -> new Pago(p.getMetodoPago(), p.getMonto()))
                .collect(Collectors.toList());

        // Construir venta con cálculo de totales
        Venta venta = new Venta(
                usuario.getId(),
                username,
                nombreCliente,
                command.getCedulaCliente(),
                detalles,
                pagos,
                tasaImpuesto
        );
        venta.setNumeroFactura(numeroFactura);

        // Validar que suma de pagos = total
        venta.validarPagos();

        // Persistir venta
        Venta saved = ventaRepository.save(venta);

        // Persistir pagos asociando el ventaId
        List<Pago> pagosConVentaId = pagos.stream().map(p -> {
            p.setVentaId(saved.getId());
            return p;
        }).collect(Collectors.toList());
        pagoVentaRepository.saveAll(pagosConVentaId);

        return toResponse(saved, pagosConVentaId, null);
    }

    // ─── 5.2: obtenerPorId ───────────────────────────────────────────────────

    @Override
    public VentaResponse obtenerPorId(Long id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta", id));
        List<Pago> pagos = pagoVentaRepository.findByVentaId(id);
        Reembolso reembolso = reembolsoRepository.findByVentaId(id).orElse(null);
        return toResponse(venta, pagos, reembolso);
    }

    // ─── 5.2: obtenerPorNumeroFactura ────────────────────────────────────────

    @Override
    public VentaResponse obtenerPorNumeroFactura(String numeroFactura) {
        Venta venta = ventaRepository.findByNumeroFactura(numeroFactura)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta", numeroFactura));
        List<Pago> pagos = pagoVentaRepository.findByVentaId(venta.getId());
        Reembolso reembolso = reembolsoRepository.findByVentaId(venta.getId()).orElse(null);
        return toResponse(venta, pagos, reembolso);
    }

    // ─── 5.2: listar ─────────────────────────────────────────────────────────

    @Override
    public Page<VentaResponse> listar(FiltroVentasCommand filtro) {
        VentaRepositoryPort.FiltroVentas filtroVentas = new VentaRepositoryPort.FiltroVentas();
        // Normalizar fecha ISO-8601 (RF-06.4.1, RF-06.4.2)
        filtroVentas.setFecha(filtro.getFecha());
        filtroVentas.setCajeroId(filtro.getCajeroId());
        filtroVentas.setCedulaCliente(filtro.getCedulaCliente());
        filtroVentas.setMetodoPago(filtro.getMetodoPago());
        filtroVentas.setPage(filtro.getPage());
        filtroVentas.setSize(filtro.getSize());

        // Si página no existe, Spring Data retorna lista vacía (no lanza excepción, RF-06.7)
        return ventaRepository.findAll(filtroVentas).map(venta -> {
            List<Pago> pagos = pagoVentaRepository.findByVentaId(venta.getId());
            Reembolso reembolso = reembolsoRepository.findByVentaId(venta.getId()).orElse(null);
            return toResponse(venta, pagos, reembolso);
        });
    }

    // ─── 5.2: reembolsar ─────────────────────────────────────────────────────

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public ReembolsoResponse reembolsar(ReembolsarVentaCommand command) {
        // Obtener venta
        Venta venta = ventaRepository.findById(command.getVentaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta", command.getVentaId()));

        // Validar que no esté ya reembolsada
        if (venta.isReembolsada()) {
            throw new VentaYaReembolsadaException(venta.getId(), venta.getNumeroFactura());
        }
        if (reembolsoRepository.existsByVentaId(venta.getId())) {
            throw new VentaYaReembolsadaException(venta.getId(), venta.getNumeroFactura());
        }

        // Devolver stock de TODOS los productos (reembolso total)
        // NOTA: Para reembolsos parciales futuros, se necesitaría una lista de items a reembolsar
        for (DetalleVenta detalle : venta.getDetalles()) {
            // Usar findByIdForUpdate para garantizar consistencia concurrente
            // Permitir reembolso aunque el producto esté inactivo (RF-08.6)
            Producto producto = productoRepository.findByIdForUpdate(detalle.getProducto().getId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Producto", detalle.getProducto().getId()));
            producto.incrementarStock(detalle.getCantidad());
            productoRepository.save(producto);
        }

        // Obtener usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", username));

        // Crear y persistir reembolso
        Reembolso reembolso = new Reembolso(
                venta.getId(),
                command.getMotivo(),
                usuario.getId(),
                username
        );
        Reembolso savedReembolso = reembolsoRepository.save(reembolso);

        // Marcar venta como reembolsada
        venta.setReembolsada(true);
        ventaRepository.save(venta);

        return toReembolsoResponse(savedReembolso);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void validarNombreCliente(String nombre) {
        String[] palabras = nombre.split(" ");
        if (palabras.length < 2) {
            throw new jakarta.validation.ValidationException(
                    "El nombre del cliente debe contener al menos nombre y apellido");
        }
    }

    private VentaResponse toResponse(Venta v, List<Pago> pagos, Reembolso reembolso) {
        List<DetalleVentaResponse> detallesResponse = v.getDetalles().stream()
                .map(d -> DetalleVentaResponse.builder()
                        .productoId(d.getProducto().getId())
                        .nombreProducto(d.getProducto().getNombre())
                        .cantidad(d.getCantidad())
                        .precioUnit(d.getPrecioUnit())
                        .subtotal(d.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        List<PagoResponse> pagosResponse = pagos == null ? List.of() : pagos.stream()
                .map(p -> PagoResponse.builder()
                        .id(p.getId())
                        .metodoPago(p.getMetodoPago())
                        .monto(p.getMonto())
                        .build())
                .collect(Collectors.toList());

        ReembolsoResponse reembolsoResponse = reembolso != null ? toReembolsoResponse(reembolso) : null;

        return VentaResponse.builder()
                .id(v.getId())
                .numeroFactura(v.getNumeroFactura())
                .usuarioId(v.getUsuarioId())
                .nombreCajero(v.getNombreCajero())
                .nombreCliente(v.getNombreCliente())
                .cedulaCliente(v.getCedulaCliente())
                .detalles(detallesResponse)
                .pagos(pagosResponse)
                .subtotal(v.getSubtotal())
                .tasaImpuesto(v.getTasaImpuesto())
                .impuesto(v.getImpuesto())
                .total(v.getTotal())
                .fecha(v.getFecha())
                .reembolsada(v.isReembolsada())
                .reembolso(reembolsoResponse)
                .build();
    }

    private ReembolsoResponse toReembolsoResponse(Reembolso r) {
        return ReembolsoResponse.builder()
                .id(r.getId())
                .ventaId(r.getVentaId())
                .motivo(r.getMotivo())
                .fecha(r.getFecha())
                .usuarioId(r.getUsuarioId())
                .nombreUsuario(r.getNombreUsuario())
                .build();
    }
}
