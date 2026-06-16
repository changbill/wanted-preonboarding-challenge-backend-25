package com.wanted.clone.oneport.payments.presentation.web;

import com.wanted.clone.oneport.payments.application.service.dto.PaymentRequest;
import com.wanted.clone.oneport.payments.application.port.in.*;
import com.wanted.clone.oneport.payments.domain.entity.payment.PgCorp;
import com.wanted.clone.oneport.payments.presentation.web.request.payment.ReqPaymentApprove;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/payment")
public class PaymentController {
    private final PgWidgetUseCase pgWidgetUseCase;
    private final PaymentFullfillUseCase paymentFullfillUseCase;

    // http://localhost:8080/payment/checkout?orderId=20241112115994&ordererName=%EC%9C%A0%EC%A7%84%ED%98%B8&ordererPhoneNumber=01012341234&userId=jinho123&amount=13400&productName=%EC%86%8D%EC%9D%B4%ED%8E%B8%ED%95%9C%EC%9A%B0%EC%9C%A0%EC%99%B81&pgCorpName=toss
    @GetMapping("checkout")
    public String paymentCheckout(@RequestParam(value = "orderId") String orderId,
                                  @RequestParam(value = "ordererName") String ordererName,
                                  @RequestParam(value = "ordererPhoneNumber") String ordererPhoneNumber,
                                  @RequestParam(value = "userId") String userId,
                                  @RequestParam(value = "amount") String amount,
                                  @RequestParam(value = "productName") String productName,
                                  @RequestParam(value = "pgCorpName") String pgCorpName,
                                  Model model) throws Exception {
        model.addAttribute("orderId", orderId);
        model.addAttribute("ordererName", ordererName);
        model.addAttribute("ordererPhoneNumber", ordererPhoneNumber);
        model.addAttribute("userId", userId);
        model.addAttribute("amount", amount);
        model.addAttribute("productName", productName);
        return pgWidgetUseCase.renderPgUi(PaymentRequest.of(pgCorpName), "checkout");
    }

    @GetMapping("success")
    public String paymentFullfill(@RequestParam(value = "paymentType") String paymentType,
                                  @RequestParam(value = "orderId") String orderId,
                                  @RequestParam(value = "paymentKey") String paymentKey,
                                  @RequestParam(value = "amount") String amount,
                                  @RequestParam(value = "pgCorpName") String pgCorpName
    ) throws Exception {

        String result = paymentFullfillUseCase.paymentApproved(ReqPaymentApprove.builder()
            .orderId(orderId).paymentKey(paymentKey).selectedPgCorp(PgCorp.from(pgCorpName)).totalAmount(Integer.parseInt(amount))
            .build()
            .toCommand());
        return pgWidgetUseCase.renderPgUi(PaymentRequest.of(pgCorpName), result);
    }

    @GetMapping("fail")
    public String paymentFail(@RequestParam(value = "message") String message,
                              @RequestParam(value = "message") String pgCorpName) throws Exception {
        return pgWidgetUseCase.renderPgUi(PaymentRequest.of(pgCorpName), "fail");
    }

    @PostMapping("confirm")
    public String paymentApprove(@RequestBody ReqPaymentApprove message) {
        log.info("message -> {}", message);
        return "toss/fail";
    }

}
