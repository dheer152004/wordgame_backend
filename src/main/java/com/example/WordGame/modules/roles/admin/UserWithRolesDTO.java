package com.example.WordGame.modules.roles.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserWithRolesDTO {
    private Long id;
    private String username;
    private List<String> roles;
    private Integer roleCount;
}
