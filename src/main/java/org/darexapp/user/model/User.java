package org.darexapp.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.darexapp.card.model.Card;
import org.darexapp.subscription.model.Subscription;
import org.darexapp.wallet.model.Wallet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String firstName;

    private String lastName;

    private String profilePicture;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    @Column(nullable = false)
    private LocalDateTime modifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Country country;

    
    @OneToMany(fetch = FetchType.EAGER,mappedBy = "owner")
    @OrderBy("createdAt ASC")
    private List<Wallet> wallets = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER,mappedBy = "owner")
    @OrderBy("createdAt DESC")
    private List<Subscription> subscriptions = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER,mappedBy = "owner")
    private List<Card> cards = new ArrayList<>();
}
