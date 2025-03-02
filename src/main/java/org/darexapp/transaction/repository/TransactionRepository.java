package org.darexapp.transaction.repository;

import org.darexapp.transaction.model.Transaction;
import org.darexapp.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

}
