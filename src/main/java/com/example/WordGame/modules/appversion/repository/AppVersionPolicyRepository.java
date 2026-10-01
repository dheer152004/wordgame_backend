package com.example.WordGame.modules.appversion.repository;

import com.example.WordGame.modules.appversion.entity.AppPlatform;
import com.example.WordGame.modules.appversion.entity.AppVersionPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppVersionPolicyRepository extends JpaRepository<AppVersionPolicy, AppPlatform> {
}