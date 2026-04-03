package com.example.backedapi.model.db;

import com.example.backedapi.model.Vo.RoleOutVo;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
@Getter
@Setter
@Table
@Entity
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "key")

public class Role implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "`key`")
    private UUID key;
    private String name;
    private String description;
    private String permissions;
    private String createdBy;
    private String updatedBy;
    private Date createdTime;
    private Date updatedTime;
    @JsonIgnore
    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    private List<UserRole> userRoles =new ArrayList<>();
    @JsonIgnore
    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    private List<RoleFunction> roleFunctions =new ArrayList<>();

    public RoleOutVo transToVo(){
        RoleOutVo roleOutVo = new RoleOutVo();
        roleOutVo.setKey(this.key);
        roleOutVo.setName(this.name);
        roleOutVo.setDescription(this.description);
        roleOutVo.setPermissions(this.permissions);
        roleOutVo.setCreatedBy(this.createdBy);
        roleOutVo.setUpdatedBy(this.updatedBy);
        roleOutVo.setCreatedTime(this.createdTime);
        roleOutVo.setUpdatedTime(this.updatedTime);
        roleOutVo.setFunctionKeys(this.roleFunctions.stream().map(RoleFunction::getFunction).map(Function::getId).map(UUID::toString).toList());
        return roleOutVo;
    }

}
