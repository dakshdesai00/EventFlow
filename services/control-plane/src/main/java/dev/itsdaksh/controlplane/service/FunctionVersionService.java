package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.FunctionRequests.FunctionVersionResponse;
import dev.itsdaksh.controlplane.entity.Function;
import dev.itsdaksh.controlplane.entity.FunctionVersion;
import dev.itsdaksh.controlplane.repository.FunctionRepo;
import dev.itsdaksh.controlplane.repository.FunctionVersionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FunctionVersionService {

    private final FunctionRepo functionRepo;
    private final FunctionVersionRepo functionVersionRepo;
    private final StorageService storageService;

    public Optional<FunctionVersionResponse> uploadVersion(
            Long functionId,
            MultipartFile file
    ) throws Exception {

        if (file.isEmpty()) {
            return Optional.empty();
        }

        if (!file.getOriginalFilename().endsWith(".js")) {
            throw new IllegalArgumentException(
                    "Only .js files are allowed"
            );
        }

        return functionRepo.findById(functionId)
                .map(function -> {

                    try {

                        int nextVersion =
                                functionVersionRepo
                                        .findTopByFunctionIdOrderByVersionNumberDesc(
                                                functionId
                                        )
                                        .map(v -> v.getVersionNumber() + 1)
                                        .orElse(1);

                        String storageKey =
                                "functions/"
                                        + functionId
                                        + "/v"
                                        + nextVersion
                                        + ".js";

                        storageService.uploadFile(
                                file,
                                storageKey
                        );

                        String hash =
                                calculateSha256(
                                        new String(
                                                file.getBytes(),
                                                StandardCharsets.UTF_8
                                        )
                                );

                        FunctionVersion version =
                                FunctionVersion.builder()
                                        .function(function)
                                        .versionNumber(nextVersion)
                                        .storageKey(storageKey)
                                        .fileHash(hash)
                                        .fileSizeBytes(file.getSize())
                                        .build();

                        version =
                                functionVersionRepo.save(version);

                        if (function.getActiveVersion() == null) {

                            function.setActiveVersion(version);

                            functionRepo.save(function);
                        }

                        return map(version);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public List<FunctionVersionResponse> getVersions(
            Long functionId
    ) {

        return functionVersionRepo.findByFunctionId(functionId)
                .stream()
                .map(this::map)
                .toList();
    }

    public Optional<FunctionVersionResponse> getVersion(
            Long versionId
    ) {

        return functionVersionRepo.findById(versionId)
                .map(this::map);
    }

    public Optional<String> getCode(
            Long versionId
    ) {

        return functionVersionRepo.findById(versionId)
                .map(version -> {

                    try {

                        return new String(
                                storageService
                                        .downloadFile(
                                                version.getStorageKey()
                                        )
                                        .readAllBytes(),
                                StandardCharsets.UTF_8
                        );

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public Optional<FunctionVersionResponse> setActiveVersion(
            Long functionId,
            Long versionId
    ) {

        return functionRepo.findById(functionId)
                .flatMap(function ->
                        functionVersionRepo.findById(versionId)
                                .filter(version ->
                                        version.getFunction()
                                                .getId()
                                                .equals(functionId)
                                )
                                .map(version -> {

                                    function.setActiveVersion(version);

                                    functionRepo.save(function);

                                    return map(version);
                                })
                );
    }

    public Optional<FunctionVersionResponse> deleteVersion(
            Long versionId
    ) {

        return functionVersionRepo.findById(versionId)
                .filter(version -> {

                    Function function =
                            version.getFunction();

                    return function.getActiveVersion() == null
                            || !function.getActiveVersion()
                            .getId()
                            .equals(versionId);
                })
                .map(version -> {

                    try {

                        storageService.deleteFile(
                                version.getStorageKey()
                        );

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    functionVersionRepo.delete(version);

                    return map(version);
                });
    }

    private FunctionVersionResponse map(
            FunctionVersion version
    ) {

        return new FunctionVersionResponse(
                version.getId(),
                version.getFunction().getId(),
                version.getVersionNumber(),
                version.getStorageKey(),
                version.getFileHash(),
                version.getFileSizeBytes()
        );
    }

    private String calculateSha256(
            String data
    ) throws Exception {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] hash =
                digest.digest(
                        data.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        return HexFormat.of()
                .formatHex(hash);
    }
}