package com.wanted.clone.oneport.payments.presentation.web;

import com.wanted.clone.oneport.payments.application.port.in.CreateNewOrderUseCase;
import com.wanted.clone.oneport.payments.presentation.web.request.order.ReqNewOrder;
import com.wanted.clone.oneport.payments.presentation.web.response.RespNewPurchaseOrderMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final CreateNewOrderUseCase createNewOrderUseCase;
//    private final OrderRepository orderRepository;

    @PostMapping("/new")
    public RespNewPurchaseOrderMessage newOrder(@RequestBody @Valid ReqNewOrder newOrder) throws Exception {
        return RespNewPurchaseOrderMessage.from(createNewOrderUseCase.createOrder(newOrder.toCommand()));
    }

    @PostMapping("/test")
    public String test(@RequestBody @Valid ReqNewOrder newOrder) throws Exception {
        log.info(newOrder.toString());
        return "hello";
    }

    @GetMapping("info")
    public  Map<String, String> requestParams(@RequestParam(value = "username") String username){
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        return params;
    }

}
