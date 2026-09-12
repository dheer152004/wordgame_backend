package com.example.WordGame.modules.roles.admin.Repositories;

import com.example.WordGame.modules.roles.admin.Entities.DeletedUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeletedUserRepository extends JpaRepository<DeletedUser, Long> {
    List<DeletedUser> findAllByOrderByDeletedAtDesc();
}