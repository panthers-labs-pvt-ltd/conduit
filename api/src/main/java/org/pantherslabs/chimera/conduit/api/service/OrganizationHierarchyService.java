package org.pantherslabs.chimera.conduit.api.service;

import org.apache.ibatis.exceptions.PersistenceException;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.pantherslabs.chimera.conduit.api.mapper.OrganizationHierarchyMapper;
import org.pantherslabs.chimera.conduit.api.model.OrganizationHierarchy;
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

import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.pantherslabs.chimera.conduit.api.mapper.OrganizationHierarchyDynamicSqlSupport.organizationHierarchy;

@Service
public class OrganizationHierarchyService {
    static ChimeraLogger OhLogger = ChimeraLoggerFactory.getLogger(OrganizationHierarchyService.class);
    @Autowired
    OrganizationHierarchyMapper organizationHierarchyMapper;

    public List<OrganizationHierarchy> getAllOrganizationHierarchy() {
        OhLogger.logInfo("Fetching all Teams");
        try {
            SelectStatementProvider selectStatement = select(OrganizationHierarchyMapper.selectList)
                    .from(organizationHierarchy)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<OrganizationHierarchy> teamList = organizationHierarchyMapper.selectMany(selectStatement);
            if (teamList.isEmpty()) {
                OhLogger.logInfo("No Team found");
                throw new ChimeraException("APIException.404",
                        Map.of("exception", "No Team found"),
                        null,
                        HttpStatus.NO_CONTENT
                );
            }
            OhLogger.logInfo(String.format("Successfully fetched %s Teams ", teamList.size()));
            return teamList;
        } catch (Exception e) {
            OhLogger.logError("Unexpected error while retrieving Teams: " + e.getMessage());
            throw new ChimeraException(
                    "APIException.500",
                    Map.of("exception", e.getMessage()),
                    null,
                    HttpStatus.EXPECTATION_FAILED
            );
        }
    }

    public OrganizationHierarchy getOrganizationHierarchyByName(String teamName) {
        OhLogger.logInfo(String.format("Fetching Team with name: %s", teamName));
        return organizationHierarchyMapper.selectByPrimaryKey(teamName)
                .orElseThrow(() -> new ChimeraException("APIException.404",
                        Map.of("exception", "Team with name : " + teamName + "not found."),
                        null,
                        HttpStatus.NOT_FOUND
                ));

    }

    @Transactional
    public OrganizationHierarchy createOrganizationHierarchy(OrganizationHierarchy team) {
        if (team == null || team.getOrgHierName() == null) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "Team details cannot be null"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }
        if (team.getCreatedBy() == null) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "createdBy column cannot be null"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }

        OhLogger.logInfo(String.format("Creating new team : %s", team.getOrgHierName()));

        if (team.getCreatedTimestamp() == null) {
            team.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));
        }


        try {
            int result = organizationHierarchyMapper.insert(team);

            if (result != 1) {
                throw new ChimeraException("APIException.500",
                        Map.of("exception", "Failed to create team :" + team.getOrgHierName()),
                        null,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return team;
        } catch (PersistenceException e) {
            // These catch DB-level exceptions like constraint violations, bad SQL, etc.
            OhLogger.logError("Database error while inserting Team: " + e.getMessage(), e);
            throw new ChimeraException(
                    "APIException.500",
                    Map.of("exception", "Database error: " + e.getMessage()),
                    null,
                    HttpStatus.NOT_ACCEPTABLE
            );
        } catch (ChimeraException e) {
            throw e;
        } catch (Exception e) {
            OhLogger.logError("Error creating Team: " + e.getMessage());
            throw new ChimeraException("APIException.500",
                    Map.of("exception", e.getMessage()),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public OrganizationHierarchy updateOrganizationHierarchy(OrganizationHierarchy team) {
        if (team.getOrgHierName() == null || team.getOrgHierName().isBlank() || team == null) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "Invalid input parameters"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }
        if (team.getUpdatedBy() == null) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "updatedBy column cannot be null"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }
        if (team.getUpdatedTimestamp() == null) {
            team.setUpdatedTimestamp(new Timestamp(System.currentTimeMillis()));
        }

        OhLogger.logInfo(String.format("Updating team: %s", team.getOrgHierName()));

        // Verify the organization exists first
        getOrganizationHierarchyByName(team.getOrgHierName());

        try {
            int rowsUpdated = organizationHierarchyMapper.updateByPrimaryKeySelective(team);

            if (rowsUpdated != 1) {
                throw new ChimeraException("APIException.500",
                        Map.of("exception", "Failed to update team" + team.getOrgHierName()),
                        null,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            OhLogger.logInfo("Organization " + team.getOrgHierName() + " updated successfully.");
            return getOrganizationHierarchyByName(team.getOrgHierName());

        } catch (PersistenceException e) {
            OhLogger.logError("Database error during update: " + e.getMessage(), e);
            throw new ChimeraException(
                    "APIException.500",
                    Map.of("exception", "Database error during update: " + e.getMessage()),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (ChimeraException e) {
            throw e;

        } catch (Exception e) {
            OhLogger.logError("Unexpected error during update: " + e.getMessage(), e);
            throw new ChimeraException(
                    "APIException.500",
                    Map.of("exception", "Unexpected error while updating Team " + e.getMessage()),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }


    @Transactional
    public void deleteOrganizationHierarchy(String teamName) {
        if (teamName == null || teamName.isBlank()) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "Team name cannot be empty"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }

        OhLogger.logInfo(String.format("Deleting Team : %s", teamName));

        // Verify the organization exists first
        getOrganizationHierarchyByName(teamName);

        try {
            int result = organizationHierarchyMapper.deleteByPrimaryKey(teamName);

            if (result != 1) {
                throw new ChimeraException("APIException.500",
                        Map.of("exception", "Failed to delete team " + teamName),
                        null,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            OhLogger.logError("Error deleting team: " + teamName + " " + e.getMessage());
            throw new ChimeraException("APIException.500",
                    Map.of("exception", e.getMessage()),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
