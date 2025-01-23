package org.darexapp.user.model;


import jakarta.persistence.*;
import lombok.*;
import org.darexapp.card.model.Card;
import org.darexapp.subscription.model.Subscription;
import org.darexapp.wallet.model.Wallet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String profilePicture;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    @Column(nullable = false)
    private LocalDateTime updatedOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    private boolean active;

    @Enumerated
    @Column(nullable = false)
    private Country country;

    @OneToMany( fetch = FetchType.EAGER, mappedBy = "user" )
    private List<Wallet> wallets = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    private List<Subscription> subscriptions = new ArrayList<>();

    @OneToMany( fetch = FetchType.EAGER, mappedBy = "user")
    private List<Card> cards = new ArrayList<>();
}
