package com.orbit.alert.services;

import com.orbit.alert.dto.AlertDTO;
import com.orbit.enums.AlertStatus;
import com.orbit.response.Response;

import java.util.List;

public interface AlertService {

    Response<AlertDTO> createAlert(AlertDTO dto);

    Response<List<AlertDTO>> getAllAlerts();

    Response<AlertDTO> getAlertById(Long id);

    Response<List<AlertDTO>> getAlertsByStatus(AlertStatus status);

    Response<List<AlertDTO>> getAlertsBySiloId(Long siloId);

    Response<List<AlertDTO>> getAlertsByShipmentId(Long shipmentId);

    Response<AlertDTO> updateAlertStatus(Long id, AlertStatus status);
}
