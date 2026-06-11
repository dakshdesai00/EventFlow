package dev.itsdaksh.controlplane.controller;



import dev.itsdaksh.controlplane.dto.FunctionRequests.CreateFunctionRequest;
import dev.itsdaksh.controlplane.dto.FunctionRequests.FunctionResponse;
import dev.itsdaksh.controlplane.service.FuncitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("/api")
@RequiredArgsConstructor
public class FunctionController {
    private final FuncitionService funcitionService;

    @PostMapping("/projects/{projectId}/functions")
    public ResponseEntity<FunctionResponse> addFunction(@PathVariable Long projectId, @RequestBody CreateFunctionRequest function) {
        return funcitionService.saveFunction(projectId, function)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/projects/{projectId}/functions")
    public ResponseEntity<List<FunctionResponse>> getFunctions(@PathVariable Long projectId) {
        return funcitionService.getProjectFunction(projectId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/functions/{funcId}")
    public ResponseEntity<FunctionResponse> getFunction(@PathVariable Long funcId) {
        return funcitionService.getFunction(funcId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/functions/{funcId}")
    public ResponseEntity<FunctionResponse> deleteFunction(@PathVariable Long funcId) {
        return funcitionService.deleteFunction(funcId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/functions/{funcId}")
    public ResponseEntity<FunctionResponse> updateFunction(@PathVariable Long funcId, @RequestBody CreateFunctionRequest function) {
        return funcitionService.updateFunction(funcId, function)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
