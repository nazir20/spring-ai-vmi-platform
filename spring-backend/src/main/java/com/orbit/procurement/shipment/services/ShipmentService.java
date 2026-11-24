package com.orbit.procurement.shipment.services;

import com.orbit.enums.ShipmentStatus;
import com.orbit.procurement.shipment.dto.ShipmentDTO;
import com.orbit.response.Response;

import java.util.List;

public interface ShipmentService {

    Response<ShipmentDTO> createShipment(ShipmentDTO dto);
    Response<List<ShipmentDTO>> getAllShipments();
    Response<ShipmentDTO> getShipmentById(Long id);
    Response<List<ShipmentDTO>> getShipmentsByPurchaseOrderId(Long purchaseOrderId);
    Response<ShipmentDTO> updateShipmentStatus(Long id, ShipmentStatus status);
}
