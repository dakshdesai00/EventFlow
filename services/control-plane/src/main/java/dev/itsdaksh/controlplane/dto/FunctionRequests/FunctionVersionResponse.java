package dev.itsdaksh.controlplane.dto.FunctionRequests;

public record FunctionVersionResponse(

        Long id,

        Long functionId,

        Integer versionNumber,

        String storageKey,

        String fileHash,

        Long fileSizeBytes

) {
}