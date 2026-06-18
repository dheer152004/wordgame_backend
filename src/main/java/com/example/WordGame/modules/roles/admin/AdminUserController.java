package com.example.WordGame.modules.roles.admin;

import com.example.WordGame.modules.roles.Role;
import com.example.WordGame.modules.roles.user.DTO.RoleUpdateRequest;
import com.example.WordGame.modules.roles.user.Entities.User;
import com.example.WordGame.modules.roles.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/admin/users", produces = "application/json")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/{id}/roles")
    public ResponseEntity<?> updateUserRoles(@PathVariable Long id, @RequestBody RoleUpdateRequest req) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        Set<String> rolesStr = req.getRoles() == null ? Set.of() : req.getRoles();
        Set<Role> roles = new HashSet<>();
        for (String r : rolesStr) {
            try {
                roles.add(Role.valueOf(r.trim().toUpperCase()));
            } catch (Exception ignored) {
                // skip invalid role strings
            }
        }

        if (roles.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("success", false, "message", "No valid roles provided"));
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "Roles updated", "roles", roles));
    }

    @GetMapping("")
    public ResponseEntity<java.util.Map<String, Object>> listUsersWithRoles() {
        String viewSql = "SELECT ID, USERNAME, ROLES FROM USER_WITH_ROLES";
        try {
            List<UserWithRolesDTO> list = jdbcTemplate.query(viewSql, (rs, rowNum) -> {
                UserWithRolesDTO dto = new UserWithRolesDTO();
                dto.setId(rs.getLong("ID"));
                dto.setUsername(rs.getString("USERNAME"));
                String rolesStr = rs.getString("ROLES");
                List<String> roles = rolesStr == null || rolesStr.isBlank()
                        ? List.of()
                        : Arrays.stream(rolesStr.split(",")).map(String::trim).collect(Collectors.toList());
                dto.setRoles(roles);
                dto.setRoleCount(roles.size());
                return dto;
            });
            return ResponseEntity.ok(java.util.Map.of("total", list.size(), "users", list));
        } catch (org.springframework.dao.DataAccessException ex) {
            // View might not exist at runtime; fall back to join-aggregate query
            String joinSql = "SELECT U.ID, U.USERNAME, GROUP_CONCAT(UR.ROLES) AS ROLES " +
                    "FROM USERS U LEFT JOIN USER_ROLES UR ON U.ID = UR.USER_ID " +
                    "GROUP BY U.ID, U.USERNAME";
            List<UserWithRolesDTO> list = jdbcTemplate.query(joinSql, (rs, rowNum) -> {
                UserWithRolesDTO dto = new UserWithRolesDTO();
                dto.setId(rs.getLong("ID"));
                dto.setUsername(rs.getString("USERNAME"));
                String rolesStr = rs.getString("ROLES");
                List<String> roles = rolesStr == null || rolesStr.isBlank()
                        ? List.of()
                        : Arrays.stream(rolesStr.split(",")).map(String::trim).collect(Collectors.toList());
                dto.setRoles(roles);
                dto.setRoleCount(roles.size());
                return dto;
            });
            return ResponseEntity.ok(java.util.Map.of("total", list.size(), "users", list));
        }
    }
}
