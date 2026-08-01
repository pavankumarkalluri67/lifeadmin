package com.LifeAdmin.ai.lifeadmin.obligation.api;



import com.LifeAdmin.ai.lifeadmin.common.security.CurrentUserProvider;
import com.LifeAdmin.ai.lifeadmin.obligation.application.ObligationService;
import com.LifeAdmin.ai.lifeadmin.obligation.domain.Obligation;
import com.LifeAdmin.ai.lifeadmin.obligation.domain.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;

/**
 * ObligationController: manages obligations.
 * Requirement 18
 */
@RestController
@RequestMapping("/api/v1/obligations")
public class ObligationController {

    private final ObligationService obligationService;
    private final CurrentUserProvider currentUserProvider;

    public ObligationController(ObligationService obligationService, CurrentUserProvider currentUserProvider) {
        this.obligationService = obligationService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    public ResponseEntity<Page<Obligation>> list(Pageable pageable) {
        Page<Obligation> obligations = obligationService.list(pageable);
        return ResponseEntity.ok(obligations);
    }

    @GetMapping("/{obligationId}")
    public ResponseEntity<Obligation> get(@PathVariable UUID obligationId) {
        Obligation obligation = obligationService.get(obligationId);
        return ResponseEntity.ok(obligation);
    }

    @PostMapping("/{obligationId}/confirm")
    public ResponseEntity<Obligation> confirm(@PathVariable UUID obligationId) {
        Obligation obligation = obligationService.confirm(obligationId);
        return ResponseEntity.ok(obligation);
    }

    @PostMapping("/{obligationId}/dismiss")
    public ResponseEntity<Obligation> dismiss(@PathVariable UUID obligationId) {
        Obligation obligation = obligationService.dismiss(obligationId);
        return ResponseEntity.ok(obligation);
    }

    @PostMapping("/{obligationId}/complete")
    public ResponseEntity<Obligation> complete(@PathVariable UUID obligationId) {
        Obligation obligation = obligationService.complete(obligationId);
        return ResponseEntity.ok(obligation);
    }

    @PutMapping("/{obligationId}")
    public ResponseEntity<Obligation> update(
            @PathVariable UUID obligationId,
            @RequestBody ObligationUpdateRequest request) {
        Priority priority = request.getPriority() != null ? Priority.valueOf(request.getPriority()) : null;
        Obligation obligation = obligationService.update(obligationId, request.getTitle(), request.getDueDate(), priority);
        return ResponseEntity.ok(obligation);
    }
}
