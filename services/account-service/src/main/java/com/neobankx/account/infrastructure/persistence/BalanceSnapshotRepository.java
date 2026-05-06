package com.neobankx.account.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BalanceSnapshotRepository extends JpaRepository<BalanceSnapshotEntity, UUID> {
}

