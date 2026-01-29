package com.groupproject.ecommerce.service.impl;

import com.groupproject.ecommerce.config.VNPayConfig;
import com.groupproject.ecommerce.dto.request.PaymentRequest;
import com.groupproject.ecommerce.dto.response.PaymentResponse;
import com.groupproject.ecommerce.entity.Order;
import com.groupproject.ecommerce.entity.PaymentTransaction;
import com.groupproject.ecommerce.enums.OrderStatus;
import com.groupproject.ecommerce.enums.PaymentProvider;
import com.groupproject.ecommerce.enums.PaymentStatus;
import com.groupproject.ecommerce.repository.OrderRepository;
import com.groupproject.ecommerce.repository.PaymentTransactionRepository;
import com.groupproject.ecommerce.service.inter.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private static final String VNPAY_VERSION = "2.1.0";
    private static final String VNPAY_COMMAND = "pay";
    private static final String CURRENCY_CODE = "VND";
    private static final String ORDER_TYPE = "other";
    private static final String LOCALE = "vn";
    private static final int PAYMENT_TIMEOUT_MINUTES = 15;
    private static final int VNPAY_AMOUNT_MULTIPLIER = 100;
    private static final String TRANSACTION_PREFIX = "ORD";
    private static final String SUCCESS_RESPONSE_CODE = "00";
    private static final String DATE_FORMAT = "yyyyMMddHHmmss";

    private final VNPayConfig vnPayConfig;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;

    /**
     * Tạo URL thanh toán VNPay
     * @param request Payment request chứa thông tin đơn hàng
     * @param ipAddress Địa chỉ IP của khách hàng
     * @return PaymentResponse chứa URL thanh toán
     */
    @Override
    @Transactional
    public PaymentResponse createVNPayPayment(PaymentRequest request, String ipAddress) {
        try {
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException(
                            "Order not found with ID: " + request.getOrderId()));

            // 🔥 1. SET ORDER → AWAITING_PAYMENT
            order.setStatus(OrderStatus.AWAITING_PAYMENT);
            orderRepository.save(order);

            String txnRef = generateTxnRef(order.getOrderId());
            String paymentUrl = buildVNPayUrl(request, txnRef, ipAddress);

            PaymentTransaction tx = new PaymentTransaction();
            tx.setOrder(order);
            tx.setProvider(PaymentProvider.VNPAY);
            tx.setTxnRef(txnRef);
            tx.setAmount(BigDecimal.valueOf(request.getAmount()));
            tx.setCurrency(CURRENCY_CODE);
            tx.setStatus(PaymentStatus.PENDING);
            tx.setCreatedAt(LocalDateTime.now());

            paymentTransactionRepository.save(tx);

            log.info("VNPay PENDING - orderId={}, txnRef={}", order.getOrderId(), txnRef);

            return new PaymentResponse(
                    "success",
                    "Payment URL created",
                    paymentUrl
            );

        } catch (Exception e) {
            log.error("Failed to create VNPay payment", e);
            return new PaymentResponse("error", e.getMessage(), null);
        }
    }


    /**
     * Xử lý callback từ VNPay sau khi thanh toán
     * @param params Parameters từ VNPay
     * @return true nếu xử lý thành công, false nếu thất bại
     */
    @Override
    @Transactional
    public boolean handleVNPayCallback(Map<String, String> params) {
        try {
            log.info("Processing VNPay callback...");
            
            if (!verifySignature(params)) {
                log.error("Signature verification failed");
                return false;
            }
            
            savePaymentTransaction(params);
            log.info("VNPay callback processed successfully");
            return true;
            
        } catch (Exception e) {
            log.error("Error processing VNPay callback: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Lấy trạng thái thanh toán của đơn hàng
     * @param orderId ID đơn hàng
     * @return PaymentResponse chứa trạng thái thanh toán
     */
    @Override
    public PaymentResponse getPaymentStatus(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            List<PaymentTransaction> transactions = paymentTransactionRepository.findByOrderOrderByCreatedAtDesc(order);

            if (transactions.isEmpty()) {
                return new PaymentResponse("error", "No payment transaction found", null);
            }

            PaymentTransaction latest = transactions.get(0);
            String status = latest.getStatus().name();
            String message = getStatusMessage(status);

            return new PaymentResponse(status, message, null);

        } catch (Exception e) {
            log.error("Failed to get payment status for order {}: {}", orderId, e.getMessage(), e);
            return new PaymentResponse("error", "Failed to get payment status: " + e.getMessage(), null);
        }
    }


    // ==================== Private Helper Methods ====================

    private void validateOrder(Long orderId) {
        orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    private String buildVNPayUrl(PaymentRequest request, String txnRef, String ipAddress) {
        try {
            Map<String, String> vnpParams = buildVNPayParams(request, txnRef, ipAddress);
            return buildPaymentUrl(vnpParams);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build VNPay URL", e);
        }
    }

    private Map<String, String> buildVNPayParams(PaymentRequest request, String txnRef, String ipAddress) {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", VNPAY_VERSION);
        params.put("vnp_Command", VNPAY_COMMAND);
        params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        params.put("vnp_Amount", String.valueOf(request.getAmount() * VNPAY_AMOUNT_MULTIPLIER));
        params.put("vnp_CurrCode", CURRENCY_CODE);
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", request.getOrderInfo());
        params.put("vnp_OrderType", ORDER_TYPE);
        params.put("vnp_Locale", LOCALE);
        params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        params.put("vnp_IpAddr", ipAddress);

        if (request.getBankCode() != null && !request.getBankCode().isEmpty()) {
            params.put("vnp_BankCode", request.getBankCode());
        }

        addTimestampParams(params);
        return params;
    }

    private void addTimestampParams(Map<String, String> params) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        
        String createDate = formatter.format(calendar.getTime());
        params.put("vnp_CreateDate", createDate);
        
        calendar.add(Calendar.MINUTE, PAYMENT_TIMEOUT_MINUTES);
        String expireDate = formatter.format(calendar.getTime());
        params.put("vnp_ExpireDate", expireDate);
    }

    private boolean verifySignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null) {
            log.error("Missing vnp_SecureHash in callback params");
            return false;
        }
        
        Map<String, String> verifyParams = new HashMap<>(params);
        verifyParams.remove("vnp_SecureHash");
        verifyParams.remove("vnp_SecureHashType");

        String calculatedHash = VNPayConfig.hashAllFields(verifyParams, vnPayConfig.getHashSecret());
        
        boolean isValid = calculatedHash.equals(receivedHash);
        log.info("Signature verification: {}", isValid ? "SUCCESS" : "FAILED");
        
        return isValid;
    }

    private void savePaymentTransaction(Map<String, String> params) {
        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        PaymentTransaction tx = paymentTransactionRepository.findByTxnRef(txnRef).orElseGet(() -> {
            Long orderId = extractOrderIdFromTxnRef(txnRef);
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

            PaymentTransaction t = new PaymentTransaction();
            t.setOrder(order);
            t.setProvider(PaymentProvider.VNPAY);
            t.setTxnRef(txnRef);
            t.setCurrency(CURRENCY_CODE);
            t.setStatus(PaymentStatus.PENDING);
            t.setCreatedAt(LocalDateTime.now());
            return t;
        });

        String amountStr = params.get("vnp_Amount");
        if (amountStr != null && !amountStr.isEmpty()) {
            tx.setAmount(new BigDecimal(amountStr).divide(new BigDecimal(VNPAY_AMOUNT_MULTIPLIER)));
        }

        tx.setResponseCode(responseCode);
        tx.setTransactionNo(params.get("vnp_TransactionNo"));
        tx.setBankCode(params.get("vnp_BankCode"));
        tx.setCardType(params.get("vnp_CardType"));

        if (SUCCESS_RESPONSE_CODE.equals(responseCode)) {
            tx.setStatus(PaymentStatus.SUCCESS);
            setPaymentDate(tx, params.get("vnp_PayDate"));
        } else {
            tx.setStatus(PaymentStatus.FAILED);
        }

        paymentTransactionRepository.save(tx);

        log.info("Saved/Updated payment transaction txnRef: {}, status: {}", txnRef, tx.getStatus());
    }


    private PaymentTransaction buildPaymentTransaction(Order order, Map<String, String> params) {
        String responseCode = params.get("vnp_ResponseCode");
        String amountStr = params.get("vnp_Amount");
        
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrder(order);
        transaction.setTxnRef(params.get("vnp_TxnRef"));
        transaction.setProvider(PaymentProvider.VNPAY);
        transaction.setAmount(new BigDecimal(amountStr).divide(new BigDecimal(VNPAY_AMOUNT_MULTIPLIER)));
        transaction.setCurrency(CURRENCY_CODE);
        transaction.setResponseCode(responseCode);
        transaction.setTransactionNo(params.get("vnp_TransactionNo"));
        transaction.setBankCode(params.get("vnp_BankCode"));
        transaction.setCardType(params.get("vnp_CardType"));
        transaction.setCreatedAt(LocalDateTime.now());

        if (SUCCESS_RESPONSE_CODE.equals(responseCode)) {
            transaction.setStatus(PaymentStatus.SUCCESS);
            setPaymentDate(transaction, params.get("vnp_PayDate"));
        } else {
            transaction.setStatus(PaymentStatus.FAILED);
        }

        return transaction;
    }

    private void setPaymentDate(PaymentTransaction transaction, String payDateStr) {
        if (payDateStr != null && !payDateStr.isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
                transaction.setPayDate(LocalDateTime.parse(payDateStr, formatter));
            } catch (Exception e) {
                log.warn("Failed to parse payment date: {}", payDateStr, e);
            }
        }
    }

    private String buildPaymentUrl(Map<String, String> params) {
        try {
            List<String> fieldNames = new ArrayList<>(params.keySet());
            Collections.sort(fieldNames);
            
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (String fieldName : fieldNames) {
                String fieldValue = params.get(fieldName);
                if (fieldValue != null && !fieldValue.isEmpty()) {
                    String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString());
                    
                    hashData.append(fieldName).append('=').append(encodedValue);
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                         .append('=').append(encodedValue);

                    if (!fieldName.equals(fieldNames.get(fieldNames.size() - 1))) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String secureHash = VNPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
            return vnPayConfig.getVnpayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to build payment URL", e);
        }
    }

    private String generateTxnRef(Long orderId) {
        return TRANSACTION_PREFIX + orderId + "_" + System.currentTimeMillis();
    }

    private Long extractOrderIdFromTxnRef(String txnRef) {
        try {
            int startIndex = TRANSACTION_PREFIX.length();
            int endIndex = txnRef.indexOf("_");
            String orderIdStr = txnRef.substring(startIndex, endIndex);
            return Long.parseLong(orderIdStr);
        } catch (Exception e) {
            throw new RuntimeException("Invalid transaction reference format: " + txnRef, e);
        }
    }

    private String getStatusMessage(String status) {
        return switch (status) {
            case "PENDING" -> "Đang chờ thanh toán";
            case "SUCCESS" -> "Thanh toán thành công";
            case "FAILED" -> "Thanh toán thất bại";
            case "REFUNDED" -> "Đã hoàn tiền";
            default -> "Trạng thái không xác định";
        };
    }

}
