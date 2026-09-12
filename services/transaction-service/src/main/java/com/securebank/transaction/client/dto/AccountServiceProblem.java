package com.securebank.transaction.client.dto;

public record AccountServiceProblem(
    String title,
    Integer status,
    String detail,
    String code
) {
}
