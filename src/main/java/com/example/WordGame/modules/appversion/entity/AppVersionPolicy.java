package com.example.WordGame.modules.appversion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "app_version_policies")
@Getter
@Setter
public class AppVersionPolicy {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AppPlatform platform;

    @Column(name = "latest_version", nullable = false, length = 30)
    private String latestVersion;

    @Column(name = "minimum_version", nullable = false, length = 30)
    private String minimumVersion;

    @Column(name = "force_update", nullable = false)
    private boolean forceUpdate;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "button_text", nullable = false, length = 80)
    private String buttonText;

    @Column(name = "store_url", nullable = false, length = 500)
    private String storeUrl;

    @Column(nullable = false)
    private boolean enabled = true;
}