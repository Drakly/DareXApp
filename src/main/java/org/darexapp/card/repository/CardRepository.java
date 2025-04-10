package org.darexapp.card.repository;

import org.darexapp.card.model.Card;
import org.darexapp.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    List<Card> findByOwner(User user);
}
