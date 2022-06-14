package com.kale.model;

import com.kale.constant.Role;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Table
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 15)
    private String email;

    @Column(nullable = false, length = 15)
    private String password;

    @Column(nullable = false, length = 20)
    @Enumerated(value = EnumType.STRING)
    private Role role;

    @Builder
    public User(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
