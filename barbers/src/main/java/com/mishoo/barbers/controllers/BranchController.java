package com.mishoo.barbers.controllers;

import com.mishoo.barbers.schema.BranchInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final Map<String, BranchInfo> branches = new ConcurrentHashMap<>();

    public BranchController() {
        branches.put("centro", new BranchInfo("centro", "Sucursal Centro", "Av. Central 123", "555-100-0001"));
        branches.put("norte", new BranchInfo("norte", "Sucursal Norte", "Plaza Norte Local 8", "555-200-0002"));
        branches.put("sur", new BranchInfo("sur", "Sucursal Sur", "Av. Insurgentes Sur 450", "555-300-0003"));
    }

    @GetMapping("")
    public ResponseEntity<List<BranchInfo>> getAll() {
        return ResponseEntity.ok(new ArrayList<>(branches.values()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BranchInfo> getById(@PathVariable String id) {
        BranchInfo branch = branches.get(id.toLowerCase().trim());
        return branch != null ? ResponseEntity.ok(branch) : ResponseEntity.notFound().build();
    }

    @PostMapping("")
    public ResponseEntity<BranchInfo> register(@RequestBody BranchInfo branchInfo) {
        if (branchInfo.id() == null || branchInfo.id().isBlank() || branchInfo.name() == null || branchInfo.name().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String cleanId = branchInfo.id().trim().toLowerCase().replaceAll("[^a-z0-9_-]", "");
        BranchInfo saved = new BranchInfo(
                cleanId,
                branchInfo.name().trim(),
                branchInfo.address() != null ? branchInfo.address().trim() : "",
                branchInfo.phone() != null ? branchInfo.phone().trim() : ""
        );
        branches.put(cleanId, saved);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
}
