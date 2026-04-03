package com.example.backedapi.model.db;

import com.example.backedapi.model.Vo.FunctionVo;
import com.example.backedapi.model.Vo.UserVo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Table(name = "Member")
@Getter
@Setter
@Entity
@NoArgsConstructor

public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "`key`")
    private UUID key ;
    private String name;
    private String email;
    private String password;
    private String phone;
    private boolean disabled =false;
    private String createdBy;
    private String updatedBy;
    private Date createdTime;
    private Date updatedTime;
    @JsonIgnore
    @OneToMany(mappedBy = "user",fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    private List<UserRole> roles =new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user",fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    private List<SkillMapUserAndProject> skillMapUserAndProjects =new ArrayList<>();
    @Transient
    private List<String> roleArr;

    @JsonIgnore
    @Transient
    private List<FunctionVo> permissions;

    public UserVo toUserVo(){
        UserVo userVo = new UserVo();
        userVo.setKey(this.key.toString());
        userVo.setName(this.name);
        userVo.setEmail(this.email);
        userVo.setPhone(this.phone);
        userVo.setDisabled(this.disabled);
        userVo.setCreatedBy(this.createdBy);
        userVo.setUpdatedBy(this.updatedBy);
        userVo.setCreatedTime(this.createdTime);
        userVo.setUpdatedTime(this.updatedTime);

        roleArr=roles.stream().map(userRole -> userRole.getRole().getKey().toString()).toList();
        userVo.setRoleArr(this.roleArr);
        this.permissions = new ArrayList<>();
        roles.forEach(
                userRole -> userRole.getRole().getRoleFunctions().forEach(
                        roleFunction -> this.permissions.add(roleFunction.getFunction().toVo())
                ));
        userVo.setPermissions(this.permissions);
        return userVo;
    }

}
