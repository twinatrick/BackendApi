package com.example.backedapi.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;
@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
public class UserRole  implements Serializable {
    @Id
    @GeneratedValue
    @Column(name = "`key`")
    private UUID key;
    @ManyToOne(cascade = CascadeType.ALL)
    @JsonIgnore
    private Role role;
    @ManyToOne(cascade = CascadeType.ALL)
    @JsonIgnore
    private User user;
}
