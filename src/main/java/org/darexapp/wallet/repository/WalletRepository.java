package org.darexapp.wallet.repository;

import jakarta.validation.constraints.NotNull;
import org.darexapp.user.model.User;
import org.darexapp.wallet.model.Wallet;
import org.darexapp.wallet.model.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {


    List<Wallet> findAllByOwnerUsername(String username);


    Optional<Wallet> findByIdAndOwnerId(UUID ownerId, UUID walletId);

    List<Wallet> findAllByOwnerIdOrderByBalanceDesc(UUID ownerId);
}
