package com.example.backedapi.model.db;

import com.example.backedapi.model.Vo.RoleOutVo;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Getter
@Setter
@Table(name = "role")
@Entity
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")

public class Role extends BaseEntity {
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "permissions")
    private String permissions;
    @JsonIgnore
    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    private List<UserRole> userRoles =new ArrayList<>();
    @JsonIgnore
    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    private List<RoleFunction> roleFunctions =new ArrayList<>();

}
