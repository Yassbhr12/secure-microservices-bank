package com.securebank.audit.repository;

import com.securebank.audit.domain.model.AuditEvent;
import com.securebank.audit.domain.model.AuditSeverity;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AuditEventSpecifications {

    private AuditEventSpecifications() {
    }

    public static Specification<AuditEvent> withFilters(
        String actorId,
        String action,
        String sourceService,
        AuditSeverity severity,
        Instant from,
        Instant to
    ) {
        List<Specification<AuditEvent>> specifications =
            new ArrayList<>();

        if (hasText(actorId)) {
            String normalized = actorId.trim();

            specifications.add(
                (root, query, builder) ->
                    builder.equal(
                        root.get("actorId"),
                        normalized
                    )
            );
        }

        if (hasText(action)) {
            String normalized = action
                .trim()
                .toUpperCase(Locale.ROOT);

            specifications.add(
                (root, query, builder) ->
                    builder.equal(
                        root.get("action"),
                        normalized
                    )
            );
        }

        if (hasText(sourceService)) {
            String normalized = sourceService
                .trim()
                .toLowerCase(Locale.ROOT);

            specifications.add(
                (root, query, builder) ->
                    builder.equal(
                        root.get("sourceService"),
                        normalized
                    )
            );
        }

        if (severity != null) {
            specifications.add(
                (root, query, builder) ->
                    builder.equal(
                        root.get("severity"),
                        severity
                    )
            );
        }

        if (from != null) {
            specifications.add(
                (root, query, builder) ->
                    builder.greaterThanOrEqualTo(
                        root.get("occurredAt"),
                        from
                    )
            );
        }

        if (to != null) {
            specifications.add(
                (root, query, builder) ->
                    builder.lessThanOrEqualTo(
                        root.get("occurredAt"),
                        to
                    )
            );
        }

        return Specification.allOf(specifications);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
