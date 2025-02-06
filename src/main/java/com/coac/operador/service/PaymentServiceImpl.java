package com.coac.operador.service;

import java.util.List;

import com.coac.operador.controller.model.CreatePaymentRequest;
import com.coac.operador.controller.model.PaymentDto;
import com.coac.operador.data.PaymentRepository;
import com.coac.operador.data.model.Payment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<Payment> getPayments(String userId, Double amount) {

        if (StringUtils.hasLength(userId) || amount != null) {
            return repository.search(userId, amount);
        }

        List<Payment> products = repository.getPayments();
        return products.isEmpty() ? null : products;
    }

    @Override
    public Payment getPayment(String paymentId) {
        return repository.getById(Long.valueOf(paymentId));
    }

    @Override
    public Boolean removePayment(String paymentId) {

        Payment product = repository.getById(Long.valueOf(paymentId));

        if (product != null) {
            repository.delete(product);
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public Payment createPayment(CreatePaymentRequest request) {

        //Otra opcion: Jakarta Validation: https://www.baeldung.com/java-validation
        if (request != null && StringUtils.hasLength(request.getUserId().trim())
                && request.getAmount() != null) {

            Payment payment = Payment.builder().userId(request.getUserId()).amount(request.getAmount()).build();

            return repository.save(payment);
        } else {
            return null;
        }
    }

    @Override
    public Payment updatePayment(String paymentId, String request) {

        //PATCH se implementa en este caso mediante Merge Patch: https://datatracker.ietf.org/doc/html/rfc7386
        Payment payment = repository.getById(Long.valueOf(paymentId));
        if (payment != null) {
            try {
                JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(objectMapper.readTree(request));
                JsonNode target = jsonMergePatch.apply(objectMapper.readTree(objectMapper.writeValueAsString(payment)));
                Payment patched = objectMapper.treeToValue(target, Payment.class);
                repository.save(patched);
                return patched;
            } catch (JsonProcessingException | JsonPatchException e) {
                log.error("Error updating product {}", paymentId, e);
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public Payment updatePayment(String paymentId, PaymentDto updateRequest) {
        Payment pyment = repository.getById(Long.valueOf(paymentId));
        if (pyment != null) {
            pyment.update(updateRequest);
            repository.save(pyment);
            return pyment;
        } else {
            return null;
        }
    }
}
