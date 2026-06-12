package dev.itsdaksh.controlplane.service;

import dev.itsdaksh.controlplane.dto.FunctionRequests.FunctionVersionResponse;
import dev.itsdaksh.controlplane.entity.Function;
import dev.itsdaksh.controlplane.entity.FunctionVersion;
import dev.itsdaksh.controlplane.repository.FunctionVersionRepo;
import dev.itsdaksh.controlplane.service.FunctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FunctionVersionService {

    private final FunctionVersionRepo functionVersionRepo;
    private final FunctionService functionService;
    private final StorageService storageService;

    public Optional<FunctionVersionResponse> uploadVersion(
            Long functionId,
            MultipartFile file
    ) {

        if (file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File cannot be empty"
            );
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.endsWith(".js")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only .js files are allowed"
            );
        }

        return functionService.getFunctionEntity(functionId)
                .map(function -> {

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

                    String hash;
                    try {
                        hash =
                                calculateSha256(
                                        new String(
                                                file.getBytes(),
                                                StandardCharsets.UTF_8
                                        )
                                );
                    } catch (Exception e) {
                        throw new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Failed to read uploaded file",
                                e
                        );
                    }

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

                        functionService.saveFunctionEntity(function);
                    }

                    return map(version);
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
                    } catch (ResponseStatusException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "Failed to read function code",
                                e
                        );
                    }
                });
    }

    public Optional<FunctionVersionResponse> setActiveVersion(
            Long functionId,
            Long versionId
    ) {

        return functionService.getFunctionEntity(functionId)
                .flatMap(function ->
                        functionVersionRepo.findById(versionId)
                                .filter(version ->
                                        version.getFunction()
                                                .getId()
                                                .equals(functionId)
                                )
                                .map(version -> {

                                    function.setActiveVersion(version);

                                    functionService.saveFunctionEntity(function);

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

                    storageService.deleteFile(
                            version.getStorageKey()
                    );

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
    ) {
        try {
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
        } catch (NoSuchAlgorithmException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to calculate file hash",
                    e
            );
        }
    }
}
