package com.orbit.procurement.purchase_order.services;

import com.orbit.enums.PurchaseOrderStatus;
import com.orbit.enums.SupplierProductStatus;
import com.orbit.exception.BadRequestException;
import com.orbit.exception.NotFoundException;
import com.orbit.inventory.product.entity.Product;
import com.orbit.inventory.product.repository.ProductRepository;
import com.orbit.inventory.supplier_product.entity.SupplierProduct;
import com.orbit.inventory.supplier_product.repository.SupplierProductRepository;
import com.orbit.organization.customer.entity.Customer;
import com.orbit.organization.customer.repository.CustomerRepository;
import com.orbit.organization.supplier.entity.Supplier;
import com.orbit.organization.supplier.repository.SupplierRepository;
import com.orbit.procurement.purchase_order.dto.PurchaseOrderDTO;
import com.orbit.procurement.purchase_order.dto.PurchaseOrderItemDTO;
import com.orbit.procurement.purchase_order.entity.PurchaseOrder;
import com.orbit.procurement.purchase_order.entity.PurchaseOrderItem;
import com.orbit.procurement.purchase_order.repository.PurchaseOrderRepository;
import com.orbit.procurement.shipment.repository.ShipmentItemRepository;
import com.orbit.response.Response;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final CustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final ShipmentItemRepository shipmentItemRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response<PurchaseOrderDTO> createPurchaseOrder(PurchaseOrderDTO dto) {

        Customer customer = findCustomerById(dto.getCustomerId());
        Supplier supplier = findSupplierById(dto.getSupplierId());

        boolean supplierAssignedToCustomer = customer.getSuppliers()
                .stream()
                .anyMatch(customerSupplier -> customerSupplier.getId().equals(supplier.getId()));

        if (!supplierAssignedToCustomer) {
            throw new BadRequestException("The selected supplier is not associated with this customer.");
        }

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BadRequestException("A purchase order must contain at least one item.");
        }

        LocalDate orderDate = dto.getOrderDate() != null ? dto.getOrderDate() : LocalDate.now();

        if (dto.getExpectedDeliveryDate() != null && dto.getExpectedDeliveryDate().isBefore(orderDate)) {
            throw new BadRequestException("Expected delivery date cannot be before the order date.");
        }

        Set<Long> productIds = new HashSet<>();

        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .orderNumber(generateOrderNumber())
                .customer(customer)
                .supplier(supplier)
                .orderDate(orderDate)
                .expectedDeliveryDate(dto.getExpectedDeliveryDate())
                .status(PurchaseOrderStatus.DRAFT)
                .currency(dto.getCurrency() != null ? dto.getCurrency().trim().toUpperCase() : "EUR")
                .build();

        for (PurchaseOrderItemDTO itemDTO : dto.getItems()) {

            if (!productIds.add(itemDTO.getProductId())) {
                throw new BadRequestException("Duplicate products are not allowed in the same purchase order.");
            }

            SupplierProduct supplierProduct = supplierProductRepository.findBySupplier_IdAndProduct_IdAndStatus(
                            supplier.getId(),
                            itemDTO.getProductId(),
                            SupplierProductStatus.ACTIVE
                    )
                    .orElseThrow(() -> new BadRequestException("This supplier does not actively offer the selected product."));

            Product product = findProductById(itemDTO.getProductId());
            BigDecimal unitPrice = supplierProduct.getUnitPrice();
            BigDecimal totalPrice = itemDTO.getQuantity().multiply(unitPrice);

            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(purchaseOrder)
                    .product(product)
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(totalPrice)
                    .build();

            purchaseOrder.getItems().add(item);
        }

        PurchaseOrder savedOrder = purchaseOrderRepository.save(purchaseOrder);

        return Response.<PurchaseOrderDTO>builder()
                .statusCode(201)
                .message("Purchase order created successfully.")
                .data(mapToDTO(savedOrder))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<PurchaseOrderDTO>> getAllPurchaseOrders() {

        List<PurchaseOrderDTO> orders = purchaseOrderRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();

        return Response.<List<PurchaseOrderDTO>>builder()
                .statusCode(200)
                .message("Purchase orders retrieved successfully.")
                .data(orders)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<PurchaseOrderDTO> getPurchaseOrderById(Long id) {

        PurchaseOrder order = findPurchaseOrderById(id);

        return Response.<PurchaseOrderDTO>builder()
                .statusCode(200)
                .message("Purchase order retrieved successfully.")
                .data(mapToDTO(order))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<PurchaseOrderDTO>> getPurchaseOrdersByCustomerId(Long customerId) {

        findCustomerById(customerId);

        List<PurchaseOrderDTO> orders = purchaseOrderRepository
                .findAllByCustomer_IdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::mapToDTO)
                .toList();

        return Response.<List<PurchaseOrderDTO>>builder()
                .statusCode(200)
                .message("Customer purchase orders retrieved successfully.")
                .data(orders)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Response<List<PurchaseOrderDTO>> getPurchaseOrdersBySupplierId(Long supplierId) {

        findSupplierById(supplierId);

        List<PurchaseOrderDTO> orders = purchaseOrderRepository
                .findAllBySupplier_IdOrderByCreatedAtDesc(supplierId)
                .stream()
                .map(this::mapToDTO)
                .toList();

        return Response.<List<PurchaseOrderDTO>>builder()
                .statusCode(200)
                .message("Supplier purchase orders retrieved successfully.")
                .data(orders)
                .build();
    }

    @Override
    @Transactional
    public Response<PurchaseOrderDTO> updatePurchaseOrderStatus(Long id, PurchaseOrderStatus status){

        PurchaseOrder purchaseOrder = findPurchaseOrderById(id);

        if (status == null) {
            throw new BadRequestException("Purchase order status is required.");
        }

        PurchaseOrderStatus currentStatus = purchaseOrder.getStatus();

        boolean validTransition = switch (currentStatus) {
            case DRAFT ->
                    status == PurchaseOrderStatus.SUBMITTED || status == PurchaseOrderStatus.CANCELLED;

            case SUBMITTED ->
                    status == PurchaseOrderStatus.CONFIRMED || status == PurchaseOrderStatus.CANCELLED;

            case CONFIRMED ->
                    status == PurchaseOrderStatus.PARTIALLY_DELIVERED || status == PurchaseOrderStatus.DELIVERED;

            case PARTIALLY_DELIVERED ->
                    status == PurchaseOrderStatus.DELIVERED;

            case DELIVERED, CANCELLED -> false;
        };

        if (!validTransition) {
            throw new BadRequestException("Invalid purchase order status transition from " + currentStatus + " to " + status + ".");
        }

        purchaseOrder.setStatus(status);
        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        return Response.<PurchaseOrderDTO>builder()
                .statusCode(200)
                .message("Purchase order status updated successfully.")
                .data(mapToDTO(savedPurchaseOrder))
                .build();
    }

    private String generateOrderNumber() {
        return "PO-" + LocalDate.now().getYear() + "-"
                + UUID.randomUUID().toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found with ID: " + id));
    }

    private Supplier findSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Supplier not found with ID: " + id));
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + id));
    }

    private PurchaseOrder findPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Purchase order not found with ID: " + id));
    }

    private PurchaseOrderDTO mapToDTO(PurchaseOrder order) {

        PurchaseOrderDTO dto = modelMapper.map(order, PurchaseOrderDTO.class);

        dto.setCustomerId(order.getCustomer().getId());
        dto.setSupplierId(order.getSupplier().getId());

        List<PurchaseOrderItemDTO> itemDTOs = order.getItems()
                .stream()
                .map(item -> {

                    PurchaseOrderItemDTO itemDTO =
                            modelMapper.map(item, PurchaseOrderItemDTO.class);

                    itemDTO.setProductId(item.getProduct().getId());

                    BigDecimal shippedQuantity =
                            shipmentItemRepository.getTotalShippedQuantityByPurchaseOrderItemId(item.getId());

                    BigDecimal deliveredQuantity =
                            shipmentItemRepository.getTotalDeliveredQuantityByPurchaseOrderItemId(item.getId());

                    itemDTO.setShippedQuantity(shippedQuantity);
                    itemDTO.setDeliveredQuantity(deliveredQuantity);

                    itemDTO.setRemainingToShip(
                            item.getQuantity().subtract(shippedQuantity)
                    );

                    itemDTO.setRemainingToDeliver(
                            item.getQuantity().subtract(deliveredQuantity)
                    );

                    return itemDTO;
                })
                .toList();

        dto.setItems(itemDTOs);

        return dto;
    }
}
