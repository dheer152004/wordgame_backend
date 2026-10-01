package com.example.WordGame.modules.appversion.service;

import com.example.WordGame.modules.appversion.dto.AdminAppVersionResponse;
import com.example.WordGame.modules.appversion.dto.AppVersionPolicyRequest;
import com.example.WordGame.modules.appversion.dto.AppVersionResponse;
import com.example.WordGame.modules.appversion.entity.AppPlatform;
import com.example.WordGame.modules.appversion.entity.AppVersionPolicy;
import com.example.WordGame.modules.appversion.repository.AppVersionPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AppVersionPolicyService {
    private static final Pattern VERSION_PATTERN = Pattern.compile("\\d+\\.\\d+\\.\\d+");
    private final AppVersionPolicyRepository repository;

    @Transactional
    public AdminAppVersionResponse create(AppVersionPolicyRequest request) {
        AppPlatform platform = parsePlatform(request.platform());
        if (repository.existsById(platform)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A version policy already exists for this platform.");
        }
        return toAdminResponse(repository.save(toEntity(new AppVersionPolicy(), platform, request)));
    }

    @Transactional
    public AdminAppVersionResponse update(String platformValue, AppVersionPolicyRequest request) {
        AppPlatform platform = parsePlatform(platformValue);
        if (!platform.name().equalsIgnoreCase(request.platform())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The platform in the path must match the request body.");
        }
        AppVersionPolicy policy = findPolicy(platform);
        return toAdminResponse(repository.save(toEntity(policy, platform, request)));
    }

    @Transactional(readOnly = true)
    public List<AdminAppVersionResponse> findAll() {
        return repository.findAll().stream().map(this::toAdminResponse).toList();
    }

    @Transactional(readOnly = true)
    public AdminAppVersionResponse findOne(String platformValue) {
        return toAdminResponse(findPolicy(parsePlatform(platformValue)));
    }

    @Transactional
    public AdminAppVersionResponse setEnabled(String platformValue, boolean enabled) {
        AppVersionPolicy policy = findPolicy(parsePlatform(platformValue));
        policy.setEnabled(enabled);
        return toAdminResponse(repository.save(policy));
    }

    @Transactional(readOnly = true)
    public AppVersionResponse findPublicPolicy(String platformValue) {
        AppPlatform platform = parsePlatform(platformValue);
        return repository.findById(platform)
                .filter(AppVersionPolicy::isEnabled)
                .map(this::toPublicResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active version policy exists."));
    }

    private AppVersionPolicy findPolicy(AppPlatform platform) {
        return repository.findById(platform)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Version policy not found."));
    }

    private AppVersionPolicy toEntity(AppVersionPolicy policy, AppPlatform platform, AppVersionPolicyRequest request) {
        if (compareVersions(request.minimumVersion(), request.latestVersion()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minimumVersion cannot exceed latestVersion.");
        }
        policy.setPlatform(platform);
        policy.setLatestVersion(request.latestVersion());
        policy.setMinimumVersion(request.minimumVersion());
        policy.setForceUpdate(request.forceUpdate());
        policy.setTitle(request.title());
        policy.setMessage(request.message());
        policy.setButtonText(request.buttonText());
        policy.setStoreUrl(request.storeUrl());
        return policy;
    }

    private AppPlatform parsePlatform(String value) {
        try {
            return AppPlatform.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "platform must be ANDROID or IOS.");
        }
    }

    private int compareVersions(String left, String right) {
        if (!VERSION_PATTERN.matcher(left).matches() || !VERSION_PATTERN.matcher(right).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Versions must use major.minor.patch format.");
        }
        int[] leftParts = Arrays.stream(left.split("\\.")).mapToInt(Integer::parseInt).toArray();
        int[] rightParts = Arrays.stream(right.split("\\.")).mapToInt(Integer::parseInt).toArray();
        for (int index = 0; index < leftParts.length; index++) {
            int comparison = Integer.compare(leftParts[index], rightParts[index]);
            if (comparison != 0) {
                return comparison;
            }
        }
        return 0;
    }

    private AppVersionResponse toPublicResponse(AppVersionPolicy policy) {
        return new AppVersionResponse(
                policy.getPlatform().name().toLowerCase(Locale.ROOT), policy.getLatestVersion(),
                policy.getMinimumVersion(), policy.isForceUpdate(), policy.getTitle(), policy.getMessage(),
                policy.getButtonText(), policy.getStoreUrl());
    }

    private AdminAppVersionResponse toAdminResponse(AppVersionPolicy policy) {
        return new AdminAppVersionResponse(
                policy.getPlatform().name(), policy.getLatestVersion(), policy.getMinimumVersion(),
                policy.isForceUpdate(), policy.getTitle(), policy.getMessage(), policy.getButtonText(),
                policy.getStoreUrl(), policy.isEnabled());
    }
}