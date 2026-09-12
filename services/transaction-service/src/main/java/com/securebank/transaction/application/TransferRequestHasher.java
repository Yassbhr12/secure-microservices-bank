package com.securebank.transaction.application;

import com.securebank.transaction.api.dto.CreateTransferRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

@Component
public class TransferRequestHasher {

    private static final String REQUEST_VERSION = "v1";
    private static final String CURRENCY = "MAD";

    public String hash(CreateTransferRequest request) {
        Objects.requireNonNull(
            request,
            "request is required"
        );

        Objects.requireNonNull(
            request.sourceAccountId(),
            "sourceAccountId is required"
        );

        Objects.requireNonNull(
            request.destinationAccountId(),
            "destinationAccountId is required"
        );

        BigDecimal normalizedAmount =
            normalizeAmount(request.amount());

        String canonicalRequest = String.join(
            "\n",
            REQUEST_VERSION,
            request.sourceAccountId().toString(),
            request.destinationAccountId().toString(),
            normalizedAmount.toPlainString(),
            CURRENCY
        );

        try {
            MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                canonicalRequest.getBytes(
                    StandardCharsets.UTF_8
                )
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                "SHA-256 is not available",
                exception
            );
        }
    }

    private BigDecimal normalizeAmount(
        BigDecimal amount
    ) {
        Objects.requireNonNull(
            amount,
            "amount is required"
        );

        try {
            return amount.setScale(
                2,
                RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                "Amount must have at most 2 decimal places",
                exception
            );
        }
    }
}
