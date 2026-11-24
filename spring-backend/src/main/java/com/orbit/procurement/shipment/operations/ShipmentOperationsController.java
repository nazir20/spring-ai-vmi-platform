package com.orbit.procurement.shipment.operations;

import com.orbit.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/shipments/operations")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class ShipmentOperationsController {
    private final ShipmentOperationsService operations;

    @GetMapping
    public Response<ShipmentOperations> board() {
        return ok(operations.board());
    }

    @PostMapping("/{shipmentId}/slot/{slotId}")
    public Response<ShipmentOperations> book(@PathVariable long shipmentId, @PathVariable long slotId) {
        return ok(operations.book(shipmentId, slotId));
    }

    @PostMapping("/{shipmentId}/exceptions/{code}/review")
    public Response<ShipmentOperations> review(@PathVariable long shipmentId, @PathVariable String code, Principal principal) {
        return ok(operations.review(shipmentId, code, principal.getName()));
    }

    @PostMapping("/{shipmentId}/brief")
    public Response<String> brief(@PathVariable long shipmentId) {
        return ok(operations.brief(shipmentId));
    }

    private static <T> Response<T> ok(T data) {
        return Response.<T>builder().statusCode(200).data(data).build();
    }
}
