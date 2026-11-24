package com.orbit.procurement.purchase_order.services;

import com.orbit.enums.PurchaseOrderStatus;
import com.orbit.procurement.purchase_order.dto.PurchaseOrderDTO;
import com.orbit.response.Response;

import java.util.List;

public interface PurchaseOrderService {

    Response<PurchaseOrderDTO> createPurchaseOrder(PurchaseOrderDTO dto);
    Response<List<PurchaseOrderDTO>> getAllPurchaseOrders();
    Response<PurchaseOrderDTO> getPurchaseOrderById(Long id);
    Response<List<PurchaseOrderDTO>> getPurchaseOrdersByCustomerId(Long customerId);
    Response<List<PurchaseOrderDTO>> getPurchaseOrdersBySupplierId(Long supplierId);
    Response<PurchaseOrderDTO> updatePurchaseOrderStatus(Long id, PurchaseOrderStatus status);
}
