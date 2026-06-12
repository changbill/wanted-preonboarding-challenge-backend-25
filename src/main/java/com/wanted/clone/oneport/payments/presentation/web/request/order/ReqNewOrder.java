package com.wanted.clone.oneport.payments.presentation.web.request.order;

import com.wanted.clone.oneport.payments.application.command.CreateOrderCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReqNewOrder {
    @Valid
    @NotNull(message = "The orderer is required.")
    private Orderer orderer;

    @Valid
    @Size(min = 1)
    private List<OrderedItem> newlyOrderedItem;

    @Getter
    @AllArgsConstructor
    public static class OrderedItem {
        @Min(1)
        private int itemIdx;

        private UUID productId;

        @NotBlank
        private String productName;

        private int price;    // 가격

        @Min(1)
        private int quantity; // 수량

        private int amounts;  // price * quantity
    }

    public CreateOrderCommand toCommand() {
        return new CreateOrderCommand(
            orderer.getName(),
            orderer.getPhoneNumber(),
            newlyOrderedItem.stream()
                .map(item -> new CreateOrderCommand.OrderedItemCommand(
                    item.getItemIdx(),
                    item.getProductId(),
                    item.getProductName(),
                    item.getPrice(),
                    item.getQuantity(),
                    item.getAmounts()
                ))
                .toList()
        );
    }
}
