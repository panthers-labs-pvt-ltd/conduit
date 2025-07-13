package org.pantherslabs.chimera.conduit.api.service;

import org.apache.ibatis.exceptions.PersistenceException;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.pantherslabs.chimera.conduit.api.mapper.OrganizationTypesMapper;
import org.pantherslabs.chimera.conduit.api.model.OrganizationTypes;
import org.pantherslabs.chimera.unisca.exception.ChimeraException;
import org.pantherslabs.chimera.unisca.logging.ChimeraLogger;
import org.pantherslabs.chimera.unisca.logging.ChimeraLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.mybatis.dynamic.sql.SqlBuilder.or;
import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.pantherslabs.chimera.conduit.api.mapper.OrganizationTypesDynamicSqlSupport.organizationTypes;

@Service
public class OrganizationTypesService {
    static ChimeraLogger OTLogger = ChimeraLoggerFactory.getLogger(OrganizationTypesService.class);

    @Autowired
    OrganizationTypesMapper organizationTypesMapper;

    public List<OrganizationTypes> getAllOrganizationTypes() {
        OTLogger.logInfo("Fetching all organization types");
        try {
            SelectStatementProvider selectStatement = select(OrganizationTypesMapper.selectList)
                    .from(organizationTypes)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<OrganizationTypes> organizationTypesList = organizationTypesMapper.selectMany(selectStatement);
            if (organizationTypesList.isEmpty()) {
                OTLogger.logInfo("No organization types found");
                throw new ChimeraException("APIException.404",
                        Map.of("exception", "No Organization found"),
                        null,
                        HttpStatus.NO_CONTENT
                );
            }
            OTLogger.logInfo(String.format("Successfully fetched %s Organization Types", organizationTypesList.size()));
            return organizationTypesList;
        } catch (Exception e) {
            OTLogger.logError("Unexpected error while retrieving Organization Types: " + e.getMessage());
            throw new ChimeraException(
                    "APIException.500",
                    Map.of("exception", e.getMessage()),
                    null,
                    HttpStatus.EXPECTATION_FAILED
            );
        }
    }

    public OrganizationTypes getOrganizationTypesByName(String orgName) {
        OTLogger.logInfo(String.format("Fetching organization type for organization name: %s", orgName));
        return organizationTypesMapper.selectByPrimaryKey(orgName)
                .orElseThrow(() -> new ChimeraException("APIException.404",
                        Map.of("exception", "Organization Type not found for name: " + orgName),
                        null,
                        HttpStatus.NOT_FOUND
                ));

    }

    @Transactional
    public OrganizationTypes createOrganizationType(OrganizationTypes org) {
        if (org == null || org.getOrgTypeName() == null) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "Organization type data cannot be null"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }
        if (org.getCreatedBy() == null) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "createdBy column cannot be null"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }

        OTLogger.logInfo(String.format("Creating new organization type: %s", org.getOrgTypeName()));

        if (org.getCreatedTimestamp() == null) {
            org.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));
        }


        try {
            int result = organizationTypesMapper.insert(org);

            if (result != 1) {
                throw new ChimeraException("APIException.500",
                        Map.of("exception", "Failed to create organization type"),
                        null,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return org;
        } catch (PersistenceException  e) {
            // These catch DB-level exceptions like constraint violations, bad SQL, etc.
            OTLogger.logError("Database error while inserting Organization: " + e.getMessage(), e);
            throw new ChimeraException(
                    "APIException.500",
                    Map.of("exception", "Database error: " + e.getMessage()),
                    null,
                    HttpStatus.NOT_ACCEPTABLE
            );
        } catch (ChimeraException e) {
            throw e;
        } catch (Exception e) {
            OTLogger.logError("Error creating organization type: " + e.getMessage());
            throw new ChimeraException("APIException.500",
                    Map.of("exception", e.getMessage()),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public OrganizationTypes updateOrganizationType(OrganizationTypes org) {
        if (org.getOrgTypeName() == null || org.getOrgTypeName().isBlank() || org == null) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "Invalid input parameters"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }
        if (org.getUpdatedBy() == null) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "updatedBy column cannot be null"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }
        if (org.getUpdatedTimestamp() == null) {
            org.setUpdatedTimestamp(new Timestamp(System.currentTimeMillis()));
        }

        OTLogger.logInfo(String.format("Updating organization type: %s", org.getOrgTypeName()));

        // Verify the organization exists first
        getOrganizationTypesByName(org.getOrgTypeName());

        try {
            int rowsUpdated = organizationTypesMapper.updateByPrimaryKeySelective(org);

            if (rowsUpdated != 1) {
                throw new ChimeraException("APIException.500",
                        Map.of("exception", "Failed to update organization type" + org.getOrgTypeName()),
                        null,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            OTLogger.logInfo("Organization " + org.getOrgTypeName() + " updated successfully.");
            return getOrganizationTypesByName(org.getOrgTypeName());

        } catch (PersistenceException e) {
            OTLogger.logError("Database error during update: " + e.getMessage(), e);
            throw new ChimeraException(
                    "APIException.500",
                    Map.of("exception", "Database error during update: " + e.getMessage()),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (ChimeraException e) {
            throw e;

        } catch (Exception e) {
            OTLogger.logError("Unexpected error during update: " + e.getMessage(), e);
            throw new ChimeraException(
                    "APIException.500",
                    Map.of("exception", "Unexpected error while updating DataControl " + e.getMessage()),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Transactional
    public void deleteOrganizationType(String orgTypeName) {
        if (orgTypeName == null || orgTypeName.isBlank()) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "Organization name cannot be empty"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }

        OTLogger.logInfo(String.format("Deleting organization : %s", orgTypeName));

        // Verify the organization exists first
        getOrganizationTypesByName(orgTypeName);

        try {
            int result = organizationTypesMapper.deleteByPrimaryKey(orgTypeName);

            if (result != 1) {
                throw new ChimeraException("APIException.500",
                        Map.of("exception", "Failed to delete organization " + orgTypeName),
                        null,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            OTLogger.logError("Error deleting organization type: " + orgTypeName + " " + e.getMessage());
            throw new ChimeraException("APIException.500",
                    Map.of("exception", e.getMessage()),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
