package org.pantherslabs.chimera.conduit.api.controller;

import org.pantherslabs.chimera.conduit.api.model.OrganizationTypes;
import org.pantherslabs.chimera.conduit.api.service.OrganizationTypesService;
import org.pantherslabs.chimera.unisca.api_nexus.api_nexus_client.response.StandardResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organization-types")
public class OrganizationTypesController {
    @Autowired
    private OrganizationTypesService organizationTypesService;

    @GetMapping
    public ResponseEntity<StandardResponse<List<OrganizationTypes>>> getAllOrganizationTypes() {
        List<OrganizationTypes> organizationTypesList = organizationTypesService.getAllOrganizationTypes();
        return ResponseEntity.ok(StandardResponse.success(organizationTypesList));
    }

    @GetMapping("/{orgName}")
    public ResponseEntity<StandardResponse<OrganizationTypes>> getOrganizationTypesByName(@PathVariable("orgName") String orgName) {
        OrganizationTypes organization = organizationTypesService.getOrganizationTypesByName(orgName);
        return ResponseEntity.ok(StandardResponse.success(organization));
    }

    @PostMapping("/create")
    public ResponseEntity<StandardResponse<OrganizationTypes>> createOrganizationType(@RequestBody OrganizationTypes organizationType) {
        OrganizationTypes createdOrganizationType = organizationTypesService.createOrganizationType(organizationType);
        return ResponseEntity.ok(StandardResponse.success("Organization " + organizationType.getOrgTypeName() + " created.", createdOrganizationType));
    }
    @PutMapping("/update")
    public ResponseEntity<StandardResponse<OrganizationTypes>> updateOrganizationType(@RequestBody OrganizationTypes organizationType) {
        OrganizationTypes updatedOrganizationType = organizationTypesService.updateOrganizationType(organizationType);
        return ResponseEntity.ok(StandardResponse.success(updatedOrganizationType));
    }
    @DeleteMapping("/delete/{orgName}")
    public ResponseEntity<StandardResponse<String>> deleteOrganizationType(@PathVariable("orgName") String orgName) {
        organizationTypesService.deleteOrganizationType(orgName);
        return ResponseEntity.ok(StandardResponse.success("Organization type deleted successfully"));
    }
}
