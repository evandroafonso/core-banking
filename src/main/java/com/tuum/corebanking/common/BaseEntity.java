package com.tuum.corebanking.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public abstract class BaseEntity {

    private Long id;
    private UUID businessId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void initializeNewAccount() {
        this.businessId = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

}

