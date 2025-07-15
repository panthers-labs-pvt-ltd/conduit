package org.pantherslabs.chimera.conduit.api.controller;

import org.pantherslabs.chimera.conduit.api.model.OrganizationHierarchy;
import org.pantherslabs.chimera.conduit.api.service.OrganizationHierarchyService;
import org.pantherslabs.chimera.unisca.api_nexus.api_nexus_client.response.StandardResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/team")
public class OrganizationHierarchyController {
    @Autowired
    private OrganizationHierarchyService organizationHierarchyService;

    @GetMapping
    public ResponseEntity<StandardResponse<List<OrganizationHierarchy>>> getAllOrganizationTypes() {
        List<OrganizationHierarchy> teamList = organizationHierarchyService.getAllOrganizationHierarchy();
        return ResponseEntity.ok(StandardResponse.success(teamList));
    }

    @GetMapping("/{teamName}")
    public ResponseEntity<StandardResponse<OrganizationHierarchy>> getOrganizationByName(@PathVariable("teamName") String teamName) {
        OrganizationHierarchy organization = organizationHierarchyService.getOrganizationHierarchyByName(teamName);
        return ResponseEntity.ok(StandardResponse.success(organization));
    }

    @PostMapping("/create")
    public ResponseEntity<StandardResponse<OrganizationHierarchy>> createOrganizationType(@RequestBody OrganizationHierarchy team) {
        OrganizationHierarchy createdTeam = organizationHierarchyService.createOrganizationHierarchy(team);
        return ResponseEntity.ok(StandardResponse.success("Organization " + team.getOrgTypeName() + " created.", createdTeam));
    }
    @PutMapping("/update")
    public ResponseEntity<StandardResponse<OrganizationHierarchy>> updateOrganizationType(@RequestBody OrganizationHierarchy team) {
        OrganizationHierarchy updatedTeam = organizationHierarchyService.updateOrganizationHierarchy(team);
        return ResponseEntity.ok(StandardResponse.success(updatedTeam));
    }
    @DeleteMapping("/delete/{teamName}")
    public ResponseEntity<StandardResponse<String>> deleteOrganizationType(@PathVariable("teamName") String teamName) {
        organizationHierarchyService.deleteOrganizationHierarchy(teamName);
        return ResponseEntity.ok(StandardResponse.success("Team " + teamName + " deleted successfully"));
    }
}
