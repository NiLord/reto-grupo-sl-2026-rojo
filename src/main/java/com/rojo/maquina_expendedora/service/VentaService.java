package com.rojo.maquina_expendedora.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rojo.maquina_expendedora.dto.PagoItem;
import com.rojo.maquina_expendedora.dto.VentaRequest;
import com.rojo.maquina_expendedora.model.BandejaEfectivo;
import com.rojo.maquina_expendedora.model.Producto;
import com.rojo.maquina_expendedora.repository.BandejaEfectivoRepository;
import com.rojo.maquina_expendedora.repository.ProductoRepository;

@Service
public class VentaService {

    private final ProductoRepository productoRepo;
    private final BandejaEfectivoRepository bandejaRepo;

    public VentaService(ProductoRepository productoRepo, BandejaEfectivoRepository bandejaRepo) {
        this.productoRepo = productoRepo;
        this.bandejaRepo = bandejaRepo;
    }

    // Venta de producto
    @Transactional
    public void venderProducto(VentaRequest request) {

        Integer idProducto = request.getIdProducto();
        int cantidad = (request.getCantidad() == null ? 1 : request.getCantidad());
        if (idProducto == null)
            throw new RuntimeException("idProducto es requerido");
        if (cantidad <= 0)
            throw new RuntimeException("cantidad inválida");
        // Verificamos stock y si no hay se detiene la venta
        // Buscar producto
        Producto producto = productoRepo.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Verificar stock
        if (producto.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
        }

        // Validar pago
        BigDecimal totalVenta = producto.getCosto().multiply(BigDecimal.valueOf(cantidad));
        // Sumar el dinero ingresado
        List<PagoItem> pagos = request.getPago();
        if (pagos == null || pagos.isEmpty()) {
            throw new RuntimeException("pago es requerido");
        }

        BigDecimal totalPagado = BigDecimal.ZERO;

        for (PagoItem item : pagos) {
            if (item == null) {
                continue;
            }

            Integer denominacion = item.getDenominacion();
            Integer cantidadPago = item.getCantidad();
            Byte tipo = item.getTipo();

            if (denominacion == null || denominacion <= 0) {
                throw new RuntimeException("denominacion inválida");
            }
            if (cantidadPago == null || cantidadPago <= 0) {
                throw new RuntimeException("cantidad de pago inválida");
            }
            if (tipo == null || (tipo != 0 && tipo != 1)) {
                throw new RuntimeException("tipo inválido");
            }

            BigDecimal valorUnitario;
            if (tipo == 0) {
                valorUnitario = BigDecimal.valueOf(denominacion);
            } else {
                valorUnitario = BigDecimal.valueOf(denominacion).movePointLeft(2);
            }

            BigDecimal subtotal = valorUnitario.multiply(BigDecimal.valueOf(cantidadPago));
            totalPagado = totalPagado.add(subtotal);

            BandejaEfectivo bandeja = bandejaRepo.findByDenominacionAndTipo(denominacion, tipo);
            if (bandeja == null) {
                bandeja = new BandejaEfectivo();
                bandeja.setDenominacion(denominacion);
                bandeja.setTipo(tipo);
                bandeja.setCantidad(0);
            }
            bandeja.setCantidad(bandeja.getCantidad() + cantidadPago);
            bandejaRepo.save(bandeja);
        }

        totalPagado = totalPagado.setScale(2, RoundingMode.HALF_UP);
        totalVenta = totalVenta.setScale(2, RoundingMode.HALF_UP);

        if (totalPagado.compareTo(totalVenta) < 0) {
            throw new RuntimeException("Pago insuficiente");
        }

        // Descontar stock del producto
        producto.setStock(producto.getStock() - cantidad);
        productoRepo.save(producto);
    }
}
