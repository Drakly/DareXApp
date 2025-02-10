package org.darexapp.wallet.repository;

import jakarta.validation.constraints.NotNull;
import org.darexapp.wallet.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {


    Optional<Wallet> findAllByOwnerUsername(String username);
}
