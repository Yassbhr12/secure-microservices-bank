package com.securebank.transaction.client;

import com.securebank.transaction.client.dto.AccountServiceProblem;
import com.securebank.transaction.client.dto.AccountTransferRequest;
import com.securebank.transaction.client.dto.AccountTransferResponse;
import com.securebank.transaction.domain.model.Transfer;
import com.securebank.transaction.exception.AccountServiceUnavailableException;
import com.securebank.transaction.exception.AccountTransferRejectedException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class AccountServiceClient {

    private static final String TRANSFER_PATH =
        "/api/v1/internal/transfers";

    private final RestClient restClient;

    public AccountServiceClient(
        @Qualifier("accountServiceRestClient")
        RestClient restClient
    ) {
        this.restClient = restClient;
    }

    public AccountTransferResponse executeTransfer(
        Transfer transfer,
        String accessToken
    ) {
        if (accessToken == null
            || accessToken.isBlank()) {

            throw new IllegalArgumentException(
                "Access token is required"
            );
        }

        try {
            AccountTransferResponse response =
                restClient
                    .post()
                    .uri(TRANSFER_PATH)
                    .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + accessToken
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                        AccountTransferRequest.from(transfer)
                    )
                    .retrieve()
                    .body(AccountTransferResponse.class);

            if (response == null) {
                throw new AccountServiceUnavailableException(
                    "Account service returned an empty response"
                );
            }

            return response;
        } catch (
            HttpClientErrorException exception
        ) {
            int status = exception
                .getStatusCode()
                .value();

            if (status == 401 || status == 403) {
                throw new AccountServiceUnavailableException(
                    "Account service rejected internal authentication",
                    exception
                );
            }

            throw mapRejection(exception);
        } catch (
            HttpServerErrorException
            | ResourceAccessException exception
        ) {
            throw new AccountServiceUnavailableException(
                "Account service is temporarily unavailable",
                exception
            );
        }
    }

    private AccountTransferRejectedException mapRejection(
        HttpClientErrorException exception
    ) {
        AccountServiceProblem problem =
            readProblem(exception);

        String failureCode =
            problem != null
                && problem.code() != null
                && !problem.code().isBlank()
                ? problem.code()
                : "ACCOUNT_TRANSFER_REJECTED";

        String message =
            problem != null
                && problem.detail() != null
                && !problem.detail().isBlank()
                ? problem.detail()
                : "Account transfer was rejected";

        return new AccountTransferRejectedException(
            exception.getStatusCode().value(),
            failureCode,
            message
        );
    }

    private AccountServiceProblem readProblem(
        HttpClientErrorException exception
    ) {
        try {
            return exception.getResponseBodyAs(
                AccountServiceProblem.class
            );
        } catch (RuntimeException ignored) {
            return null;
        }
    }
}
