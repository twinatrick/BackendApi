package com.example.backedapi.model.db;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "key")

public class RoleFunction implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "`key`")
    private UUID key;
    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    private Role role;
    @JsonIgnore

    @ManyToOne(cascade = CascadeType.ALL)
    private Function function;
}
