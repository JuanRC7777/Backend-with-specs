package com.empresa.pos.application.service;

import com.empresa.pos.application.dto.command.*;
import com.empresa.pos.application.dto.response.VentaResponse;
import com.empresa.pos.application.dto.response.ReembolsoResponse;
import com.empresa.pos.application.port.out.*;
import com.empresa.pos.domain.exception.*;
import com.empresa.pos.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VentaService - Tests Unitarios")
class VentaServiceTest {

    @Mock private ProductoRepositoryPort productoRepository;
    @Mock private VentaRepositoryPort ventaRepository;
    @Mock private PagoVentaRepositoryPort pagoVentaRepository;
    @Mock private ReembolsoRepositoryPort reembolsoRepository;
    @Mock private UsuarioRepositoryPort usuarioRepository;
    @Mock private SecuenciaFacturaService secuenciaFacturaService;
    @Mock private ConfiguracionService configuracionService;

    @InjectMocks
    private VentaService ventaService;

    private Producto producto;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        producto = new Producto(1L, "Café", new BigDecimal("10.00"), 50);
        producto.setActivo(true);

        usuario = new Usuario(1L, "cajero1", "hash", "Juan Pérez", "USER", true);

        // Mock del SecurityContext (lenient para tests que no lo usan)
        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn("cajero1");
        SecurityContext ctx = mock(SecurityContext.class);
        lenient().when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private RegistrarVentaCommand buildCommand(BigDecimal montoPago) {
        return RegistrarVentaCommand.builder()
                .nombreCliente("Juan Pérez")
                .cedulaCliente("1234567890")
                .items(List.of(new ItemVentaCommand(1L, 2)))
                .pagos(List.of(new PagoCommand("EFECTIVO", montoPago)))
                .build();
    }

    private Venta buildVentaGuardada() {
        Venta v = new Venta();
        v.setId(1L);
        v.setNumeroFactura("FAC-20260508-000001");
        v.setUsuarioId(1L);
        v.setNombreCajero("cajero1");
        v.setNombreCliente("Juan Pérez");
        v.setCedulaCliente("1234567890");
        v.setDetalles(List.of(new DetalleVenta(producto, 2)));
        v.setTasaImpuesto(new BigDecimal("0.05"));
        v.calcularTotales();
        v.setFecha(LocalDateTime.now());
        return v;
    }

    private void mockRegistrar(BigDecimal montoPago) {
        given(usuarioRepository.findByUsername("cajero1")).willReturn(Optional.of(usuario));
        given(configuracionService.obtenerTasaImpuesto()).willReturn(new BigDecimal("0.05"));
        given(secuenciaFacturaService.obtenerSiguienteNumeroFactura()).willReturn("FAC-20260508-000001");
        given(productoRepository.findByIdForUpdate(1L)).willReturn(Optional.of(producto));
        given(productoRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(ventaRepository.save(any())).willAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setId(1L);
            return v;
        });
        given(pagoVentaRepository.saveAll(any())).willAnswer(inv -> inv.getArgument(0));
    }

    // ─── 5.7 Tests ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("registrar debe generar número de factura único")
    void registrar_debeGenerarNumeroFacturaUnico_alConfirmarVenta() {
        // 2 items × $10 = $20 subtotal, $1 impuesto, $21 total
        mockRegistrar(new BigDecimal("21.00"));
        VentaResponse response = ventaService.registrar(buildCommand(new BigDecimal("21.00")));
        assertThat(response.getNumeroFactura()).isEqualTo("FAC-20260508-000001");
    }

    @Test
    @DisplayName("registrar debe obtener nombre cajero desde JWT")
    void registrar_debeObtenerNombreCajeroDesdeJWT_alConfirmarVenta() {
        mockRegistrar(new BigDecimal("21.00"));
        VentaResponse response = ventaService.registrar(buildCommand(new BigDecimal("21.00")));
        assertThat(response.getNombreCajero()).isEqualTo("cajero1");
    }

    @Test
    @DisplayName("registrar debe obtener tasa de impuesto desde configuración")
    void registrar_debeObtenerTasaImpuestoDesdeConfiguracion_alConfirmarVenta() {
        mockRegistrar(new BigDecimal("21.00"));
        VentaResponse response = ventaService.registrar(buildCommand(new BigDecimal("21.00")));
        assertThat(response.getTasaImpuesto()).isEqualByComparingTo("0.05");
    }

    @Test
    @DisplayName("registrar debe calcular subtotal por línea con redondeo")
    void registrar_debeCalcularSubtotalPorLineaConRedondeo_conMultiplesProductos() {
        mockRegistrar(new BigDecimal("21.00"));
        VentaResponse response = ventaService.registrar(buildCommand(new BigDecimal("21.00")));
        // 2 × $10.00 = $20.00
        assertThat(response.getSubtotal()).isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("registrar debe calcular impuesto con redondeo ROUND_HALF_UP")
    void registrar_debeCalcularImpuestoConRedondeo_conTasaImpuesto() {
        mockRegistrar(new BigDecimal("21.00"));
        VentaResponse response = ventaService.registrar(buildCommand(new BigDecimal("21.00")));
        // $20.00 × 0.05 = $1.00
        assertThat(response.getImpuesto()).isEqualByComparingTo("1.00");
    }

    @Test
    @DisplayName("registrar debe calcular total con redondeo")
    void registrar_debeCalcularTotalConRedondeo_sumandoSubtotalEImpuesto() {
        mockRegistrar(new BigDecimal("21.00"));
        VentaResponse response = ventaService.registrar(buildCommand(new BigDecimal("21.00")));
        // $20.00 + $1.00 = $21.00
        assertThat(response.getTotal()).isEqualByComparingTo("21.00");
    }

    @Test
    @DisplayName("registrar debe guardar datos del cliente")
    void registrar_debeGuardarDatosCliente_alConfirmarVenta() {
        mockRegistrar(new BigDecimal("21.00"));
        VentaResponse response = ventaService.registrar(buildCommand(new BigDecimal("21.00")));
        assertThat(response.getNombreCliente()).isEqualTo("Juan Pérez");
        assertThat(response.getCedulaCliente()).isEqualTo("1234567890");
    }

    @Test
    @DisplayName("registrar debe guardar múltiples pagos")
    void registrar_debeGuardarMultiplesPagos_alConfirmarVenta() {
        given(usuarioRepository.findByUsername("cajero1")).willReturn(Optional.of(usuario));
        given(configuracionService.obtenerTasaImpuesto()).willReturn(new BigDecimal("0.05"));
        given(secuenciaFacturaService.obtenerSiguienteNumeroFactura()).willReturn("FAC-20260508-000001");
        given(productoRepository.findByIdForUpdate(1L)).willReturn(Optional.of(producto));
        given(productoRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(ventaRepository.save(any())).willAnswer(inv -> { Venta v = inv.getArgument(0); v.setId(1L); return v; });
        given(pagoVentaRepository.saveAll(any())).willAnswer(inv -> inv.getArgument(0));

        RegistrarVentaCommand command = RegistrarVentaCommand.builder()
                .nombreCliente("Juan Pérez")
                .cedulaCliente("1234567890")
                .items(List.of(new ItemVentaCommand(1L, 2)))
                .pagos(List.of(
                        new PagoCommand("EFECTIVO", new BigDecimal("11.00")),
                        new PagoCommand("TARJETA", new BigDecimal("10.00"))
                ))
                .build();

        VentaResponse response = ventaService.registrar(command);
        assertThat(response.getPagos()).hasSize(2);
    }

    @Test
    @DisplayName("registrar debe lanzar PagosInvalidosException cuando suma no coincide")
    void registrar_debeLanzarPagosInvalidosException_cuandoSumaDePagosNoCoincide() {
        given(usuarioRepository.findByUsername("cajero1")).willReturn(Optional.of(usuario));
        given(configuracionService.obtenerTasaImpuesto()).willReturn(new BigDecimal("0.05"));
        given(secuenciaFacturaService.obtenerSiguienteNumeroFactura()).willReturn("FAC-20260508-000001");
        given(productoRepository.findByIdForUpdate(1L)).willReturn(Optional.of(producto));
        given(productoRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // Total real = $21.00, pero pago = $15.00
        assertThatThrownBy(() -> ventaService.registrar(buildCommand(new BigDecimal("15.00"))))
                .isInstanceOf(PagosInvalidosException.class);
    }

    @Test
    @DisplayName("registrar debe descontar stock al confirmar venta")
    void registrar_debeDescontarStock_alConfirmarVenta() {
        mockRegistrar(new BigDecimal("21.00"));
        ventaService.registrar(buildCommand(new BigDecimal("21.00")));
        // Stock inicial 50, se descuentan 2
        assertThat(producto.getStock()).isEqualTo(48);
    }

    @Test
    @DisplayName("registrar debe lanzar StockInsuficienteException cuando no hay stock")
    void registrar_debeLanzarStockInsuficienteException_cuandoStockEsInsuficiente() {
        producto.setStock(1); // Solo 1 en stock
        given(usuarioRepository.findByUsername("cajero1")).willReturn(Optional.of(usuario));
        given(configuracionService.obtenerTasaImpuesto()).willReturn(new BigDecimal("0.05"));
        given(secuenciaFacturaService.obtenerSiguienteNumeroFactura()).willReturn("FAC-20260508-000001");
        given(productoRepository.findByIdForUpdate(1L)).willReturn(Optional.of(producto));

        // Intenta comprar 2 pero solo hay 1
        assertThatThrownBy(() -> ventaService.registrar(buildCommand(new BigDecimal("21.00"))))
                .isInstanceOf(StockInsuficienteException.class);
    }

    @Test
    @DisplayName("obtenerPorId debe retornar venta cuando existe")
    void obtenerPorId_debeRetornarVenta_cuandoExiste() {
        Venta venta = buildVentaGuardada();
        given(ventaRepository.findById(1L)).willReturn(Optional.of(venta));
        given(pagoVentaRepository.findByVentaId(1L)).willReturn(List.of());
        given(reembolsoRepository.findByVentaId(1L)).willReturn(Optional.empty());

        VentaResponse response = ventaService.obtenerPorId(1L);
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNumeroFactura()).isEqualTo("FAC-20260508-000001");
    }

    @Test
    @DisplayName("obtenerPorId debe lanzar excepción cuando no existe")
    void obtenerPorId_debeLanzarExcepcion_cuandoNoExiste() {
        given(ventaRepository.findById(99L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> ventaService.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("obtenerPorNumeroFactura debe retornar venta cuando existe")
    void obtenerPorNumeroFactura_debeRetornarVenta_cuandoExiste() {
        Venta venta = buildVentaGuardada();
        given(ventaRepository.findByNumeroFactura("FAC-20260508-000001")).willReturn(Optional.of(venta));
        given(pagoVentaRepository.findByVentaId(1L)).willReturn(List.of());
        given(reembolsoRepository.findByVentaId(1L)).willReturn(Optional.empty());

        VentaResponse response = ventaService.obtenerPorNumeroFactura("FAC-20260508-000001");
        assertThat(response.getNumeroFactura()).isEqualTo("FAC-20260508-000001");
    }

    @Test
    @DisplayName("obtenerPorNumeroFactura debe lanzar excepción cuando no existe")
    void obtenerPorNumeroFactura_debeLanzarExcepcion_cuandoNoExiste() {
        given(ventaRepository.findByNumeroFactura("FAC-INVALIDA")).willReturn(Optional.empty());
        assertThatThrownBy(() -> ventaService.obtenerPorNumeroFactura("FAC-INVALIDA"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("listar debe retornar ventas paginadas con filtros")
    void listar_debeRetornarVentasPaginadas_conFiltros() {
        Venta venta = buildVentaGuardada();
        Page<Venta> page = new PageImpl<>(List.of(venta));
        given(ventaRepository.findAll(any(VentaRepositoryPort.FiltroVentas.class))).willReturn(page);
        given(pagoVentaRepository.findByVentaId(1L)).willReturn(List.of());
        given(reembolsoRepository.findByVentaId(1L)).willReturn(Optional.empty());

        Page<VentaResponse> result = ventaService.listar(new FiltroVentasCommand());
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("reembolsar debe crear reembolso y devolver stock cuando venta es válida")
    void reembolsar_debeCrearReembolsoYDevolverStock_cuandoVentaValida() {
        Venta venta = buildVentaGuardada();
        given(ventaRepository.findById(1L)).willReturn(Optional.of(venta));
        given(reembolsoRepository.existsByVentaId(1L)).willReturn(false);
        given(productoRepository.findByIdForUpdate(1L)).willReturn(Optional.of(producto));
        given(productoRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(usuarioRepository.findByUsername("cajero1")).willReturn(Optional.of(usuario));
        given(reembolsoRepository.save(any())).willAnswer(inv -> {
            Reembolso r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        given(ventaRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        ReembolsarVentaCommand cmd = new ReembolsarVentaCommand(1L, "Producto defectuoso recibido", null);
        ReembolsoResponse response = ventaService.reembolsar(cmd);

        assertThat(response.getVentaId()).isEqualTo(1L);
        assertThat(response.getMotivo()).isEqualTo("Producto defectuoso recibido");
    }

    @Test
    @DisplayName("reembolsar debe lanzar VentaYaReembolsadaException cuando ya fue reembolsada")
    void reembolsar_debeLanzarVentaYaReembolsadaException_cuandoVentaYaReembolsada() {
        Venta venta = buildVentaGuardada();
        venta.setReembolsada(true);
        given(ventaRepository.findById(1L)).willReturn(Optional.of(venta));

        ReembolsarVentaCommand cmd = new ReembolsarVentaCommand(1L, "Motivo de prueba largo", null);
        assertThatThrownBy(() -> ventaService.reembolsar(cmd))
                .isInstanceOf(VentaYaReembolsadaException.class);
    }

    @Test
    @DisplayName("reembolsar debe incrementar stock de productos al reembolsar")
    void reembolsar_debeIncrementarStockDeProductos_alReembolsar() {
        Venta venta = buildVentaGuardada();
        int stockAntes = producto.getStock();
        given(ventaRepository.findById(1L)).willReturn(Optional.of(venta));
        given(reembolsoRepository.existsByVentaId(1L)).willReturn(false);
        given(productoRepository.findByIdForUpdate(1L)).willReturn(Optional.of(producto));
        given(productoRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(usuarioRepository.findByUsername("cajero1")).willReturn(Optional.of(usuario));
        given(reembolsoRepository.save(any())).willAnswer(inv -> { Reembolso r = inv.getArgument(0); r.setId(1L); return r; });
        given(ventaRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        ventaService.reembolsar(new ReembolsarVentaCommand(1L, "Motivo de prueba largo", null));

        // Stock debe incrementarse en 2 (cantidad del detalle)
        assertThat(producto.getStock()).isEqualTo(stockAntes + 2);
    }

    @Test
    @DisplayName("reembolsar debe marcar venta como reembolsada")
    void reembolsar_debeMarcarVentaComoReembolsada_alReembolsar() {
        Venta venta = buildVentaGuardada();
        given(ventaRepository.findById(1L)).willReturn(Optional.of(venta));
        given(reembolsoRepository.existsByVentaId(1L)).willReturn(false);
        given(productoRepository.findByIdForUpdate(1L)).willReturn(Optional.of(producto));
        given(productoRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(usuarioRepository.findByUsername("cajero1")).willReturn(Optional.of(usuario));
        given(reembolsoRepository.save(any())).willAnswer(inv -> { Reembolso r = inv.getArgument(0); r.setId(1L); return r; });
        given(ventaRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        ventaService.reembolsar(new ReembolsarVentaCommand(1L, "Motivo de prueba largo", null));

        assertThat(venta.isReembolsada()).isTrue();
    }
}
