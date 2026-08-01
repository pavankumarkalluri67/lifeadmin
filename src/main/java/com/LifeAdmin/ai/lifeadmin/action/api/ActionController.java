package com.LifeAdmin.ai.lifeadmin.action.api;


import com.LifeAdmin.ai.lifeadmin.action.application.ActionService;
import com.LifeAdmin.ai.lifeadmin.action.domain.ActionItem;
import com.LifeAdmin.ai.lifeadmin.obligation.domain.Priority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * ActionController: manages action items.
 * Requirement 19
 */
@RestController
@RequestMapping("/api/v1/actions")
public class ActionController {

    private final ActionService actionService;

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    @PostMapping
    public ResponseEntity<ActionItem> create(@RequestBody CreateActionRequest request) {
        Priority priority = Priority.valueOf(request.getPriority());
        UUID obligationId = request.getObligationId() != null ? UUID.fromString(request.getObligationId()) : null;
        ActionItem action = actionService.create(request.getTitle(), priority, obligationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(action);
    }

    @GetMapping
    public ResponseEntity<Page<ActionItem>> list(Pageable pageable) {
        Page<ActionItem> actions = actionService.list(pageable);
        return ResponseEntity.ok(actions);
    }

    @GetMapping("/{actionId}")
    public ResponseEntity<ActionItem> get(@PathVariable UUID actionId) {
        ActionItem action = actionService.get(actionId);
        return ResponseEntity.ok(action);
    }

    @PostMapping("/{actionId}/complete")
    public ResponseEntity<ActionItem> complete(@PathVariable UUID actionId) {
        ActionItem action = actionService.complete(actionId);
        return ResponseEntity.ok(action);
    }

    @PutMapping("/{actionId}")
    public ResponseEntity<ActionItem> update(@PathVariable UUID actionId, @RequestBody UpdateActionRequest request) {
        Priority priority = request.getPriority() != null ? Priority.valueOf(request.getPriority()) : null;
        ActionItem action = actionService.update(actionId, request.getTitle(), priority);
        return ResponseEntity.ok(action);
    }

    @DeleteMapping("/{actionId}")
    public ResponseEntity<Void> delete(@PathVariable UUID actionId) {
        actionService.delete(actionId);
        return ResponseEntity.noContent().build();
    }
}
