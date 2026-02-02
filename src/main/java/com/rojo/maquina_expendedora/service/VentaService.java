package com.rojo.maquina_expendedora.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rojo.maquina_expendedora.dto.PagoItem;
import com.rojo.maquina_expendedora.dto.VentaItem;
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

        List<VentaItem> items = request.getItems();
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("items es requerido");
        }

        Map<Integer, Integer> cantidades = new HashMap<>();
        for (VentaItem item : items) {
            if (item == null) {
                continue;
            }

            Integer idProducto = item.getIdProducto();
            int cantidad = (item.getCantidad() == null ? 1 : item.getCantidad());

            if (idProducto == null) {
                throw new RuntimeException("idProducto es requerido");
            }
            if (cantidad <= 0) {
                throw new RuntimeException("cantidad inválida");
            }

            cantidades.merge(idProducto, cantidad, Integer::sum);
        }

        if (cantidades.isEmpty()) {
            throw new RuntimeException("items inválidos");
        }

        Map<Integer, Producto> productos = new HashMap<>();
        BigDecimal totalVenta = BigDecimal.ZERO;

        for (Map.Entry<Integer, Integer> entry : cantidades.entrySet()) {
            Integer idProducto = entry.getKey();
            Integer cantidad = entry.getValue();

            Producto producto = productoRepo.findById(idProducto)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (producto.getStock() < cantidad) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            totalVenta = totalVenta.add(producto.getCosto().multiply(BigDecimal.valueOf(cantidad)));
            productos.put(idProducto, producto);
        }

        // Validar pago
        // Sumar el dinero ingresado
        PagoItem pago = request.getPago();
        if (pago == null) {
            throw new RuntimeException("pago es requerido");
        }

        Integer denominacion = pago.getDenominacion();
        Integer cantidadPago = pago.getCantidad();
        Byte tipo = pago.getTipo();

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

        BigDecimal totalPagado = valorUnitario.multiply(BigDecimal.valueOf(cantidadPago));

        BandejaEfectivo bandeja = bandejaRepo.findByDenominacionAndTipo(denominacion, tipo);
        if (bandeja == null) {
            bandeja = new BandejaEfectivo();
            bandeja.setDenominacion(denominacion);
            bandeja.setTipo(tipo);
            bandeja.setCantidad(0);
        }
        bandeja.setCantidad(bandeja.getCantidad() + cantidadPago);
        bandejaRepo.save(bandeja);

        totalPagado = totalPagado.setScale(2, RoundingMode.HALF_UP);
        totalVenta = totalVenta.setScale(2, RoundingMode.HALF_UP);

        if (totalPagado.compareTo(totalVenta) < 0) {
            throw new RuntimeException("Pago insuficiente");
        }

        // Descontar stock de los productos
        for (Map.Entry<Integer, Integer> entry : cantidades.entrySet()) {
            Producto producto = productos.get(entry.getKey());
            int cantidad = entry.getValue();
            producto.setStock(producto.getStock() - cantidad);
            productoRepo.save(producto);
        }
    }
}
