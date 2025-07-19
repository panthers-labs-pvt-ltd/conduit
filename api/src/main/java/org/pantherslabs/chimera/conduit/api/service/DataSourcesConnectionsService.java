package org.pantherslabs.chimera.conduit.api.service;

import org.apache.ibatis.exceptions.PersistenceException;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.pantherslabs.chimera.conduit.api.mapper.DataSourcesConnectionsMapper;
import org.pantherslabs.chimera.conduit.api.model.DataSourcesConnections;
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

import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.select;
import static org.pantherslabs.chimera.conduit.api.mapper.DataSourcesConnectionsDynamicSqlSupport.dataSourcesConnections;


@Service
public class DataSourcesConnectionsService {
    static ChimeraLogger DSCLogger = ChimeraLoggerFactory.getLogger(DataSourcesConnectionsService.class);
    @Autowired
    DataSourcesConnectionsMapper dataSourcesConnectionsMapper;

    public List<DataSourcesConnections> getAllSourcesConnections() {
        DSCLogger.logInfo("Fetching all Data Sources Connections");
        try {
            SelectStatementProvider selectStatement = select(DataSourcesConnectionsMapper.selectList)
                    .from(dataSourcesConnections)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<DataSourcesConnections> SourcesConnectionsList = dataSourcesConnectionsMapper.selectMany(selectStatement);
            if (SourcesConnectionsList.isEmpty()) {
                DSCLogger.logInfo("No Data Source Connection found");
                throw new ChimeraException("APIException.404",
                        Map.of("exception", "No Data Source Connections found"),
                        null,
                        HttpStatus.NO_CONTENT
                );
            }
            DSCLogger.logInfo(String.format("Successfully fetched %s Data Sources Connections ", SourcesConnectionsList.size()));
            return SourcesConnectionsList;
        } catch (Exception e) {
            DSCLogger.logError("Unexpected error while retrieving Data Sources Connections: " + e.getMessage());
            throw new ChimeraException(
                    "APIException.500",
                    Map.of("exception", e.getMessage()),
                    null,
                    HttpStatus.EXPECTATION_FAILED
            );
        }
    }


    public DataSourcesConnections getSourceConnectionByName(String sourceConnectionName) {
        DSCLogger.logInfo(String.format("Fetching source Connection: %s", sourceConnectionName));
        if (sourceConnectionName == null || sourceConnectionName.isEmpty()) {
            DSCLogger.logError("Data Source Connection Name cannot be null or empty");
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "Data Source Connection Name cannot be null or empty"),
                    null,
                    HttpStatus.BAD_REQUEST
            );
        }
        SelectStatementProvider selectStatement = select(DataSourcesConnectionsMapper.selectList)
                .from(dataSourcesConnections)
                .where(dataSourcesConnections.dataSourceConnectionName, isEqualTo(sourceConnectionName))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        return dataSourcesConnectionsMapper.selectOne(selectStatement)
                .orElseThrow(() -> new ChimeraException("APIException.404",
                        Map.of("exception", String.format("No Data Source Connection found with Name : %s and sub-type %s", sourceConnectionName)),
                        null,
                        HttpStatus.NOT_FOUND
                ));

    }


    @Transactional
    public DataSourcesConnections createSourceConnection(DataSourcesConnections sourceConnection) {
        if (sourceConnection == null || sourceConnection.getDataSourceConnectionName() == null || sourceConnection.getDataSourceConnectionName().isEmpty()) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "Data Source Connection details cannot be null"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }
        if (sourceConnection.getCreatedBy() == null) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "createdBy column cannot be null"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }

        DSCLogger.logInfo(String.format("Creating new Data Source Connection with name : %s ", sourceConnection.getDataSourceConnectionName()));

        if (sourceConnection.getCreatedTimestamp() == null) {
            sourceConnection.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));
        }


        try {
            int result = dataSourcesConnectionsMapper.insert(sourceConnection);

            if (result != 1) {
                throw new ChimeraException("APIException.500",
                        Map.of("exception", String.format("Failed to create data source connection with name : %s", sourceConnection.getDataSourceConnectionName())),
                        null,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return sourceConnection;
        } catch (PersistenceException e) {
            // These catch DB-level exceptions like constraint violations, bad SQL, etc.
            DSCLogger.logError("Database error while inserting Data Source Connection : " + e.getMessage(), e);
            throw new ChimeraException(
                    "APIException.500",
                    Map.of("exception", "Database error: " + e.getMessage()),
                    null,
                    HttpStatus.NOT_ACCEPTABLE
            );
        } catch (ChimeraException e) {
            throw e;
        } catch (Exception e) {
            DSCLogger.logError("Error creating Data Source Connection : " + e.getMessage());
            throw new ChimeraException("APIException.500",
                    Map.of("exception", e.getMessage()),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public DataSourcesConnections updateSourceConnection(DataSourcesConnections sourceConnection) {
        if (sourceConnection.getDataSourceConnectionName() == null || sourceConnection.getDataSourceConnectionName().isEmpty()) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "Invalid input parameters"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }
        if (sourceConnection.getUpdatedBy() == null) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "updatedBy column cannot be null"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }
        if (sourceConnection.getUpdatedTimestamp() == null) {
            sourceConnection.setUpdatedTimestamp(new Timestamp(System.currentTimeMillis()));
        }

        DSCLogger.logInfo(String.format("Updating Connection details with name : %s", sourceConnection.getDataSourceConnectionName()));

        // Verify the organization exists first
        getSourceConnectionByName(sourceConnection.getDataSourceConnectionName());

        try {
            int rowsUpdated = dataSourcesConnectionsMapper.updateByPrimaryKeySelective(sourceConnection);

            if (rowsUpdated != 1) {
                throw new ChimeraException("APIException.500",
                        Map.of("exception", "Failed to update source Connection with name: %s" + sourceConnection.getDataSourceConnectionName()),
                        null,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            DSCLogger.logInfo("Data Source Connection with name : " + sourceConnection.getDataSourceConnectionName() + " updated successfully");
            return getSourceConnectionByName(sourceConnection.getDataSourceConnectionName());

        } catch (PersistenceException e) {
            DSCLogger.logError("Database error during update: " + e.getMessage(), e);
            throw new ChimeraException(
                    "APIException.500",
                    Map.of("exception", "Database error during update: " + e.getMessage()),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (ChimeraException e) {
            throw e;

        } catch (Exception e) {
            DSCLogger.logError("Unexpected error during update: " + e.getMessage(), e);
            throw new ChimeraException(
                    "APIException.500",
                    Map.of("exception", "Unexpected error while updating Team " + e.getMessage()),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    public void deleteSourceConnection(String sourceConnectionName) {
        if (sourceConnectionName == null || sourceConnectionName.isBlank()) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "Data Source connection Name cannot be empty"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }

        DSCLogger.logInfo(String.format("Deleting Source Connection with name: %s", sourceConnectionName));

        // Verify the organization exists first
        getSourceConnectionByName(sourceConnectionName);

        try {
            int result = dataSourcesConnectionsMapper.deleteByPrimaryKey(sourceConnectionName);

            if (result != 1) {
                throw new ChimeraException("APIException.500",
                        Map.of("exception", String.format("Failed to delete data source connections with name %s", sourceConnectionName)),
                        null,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            DSCLogger.logError(String.format("Failed to delete data source connection with name %s", sourceConnectionName) + e.getMessage());
            throw new ChimeraException("APIException.500",
                    Map.of("exception", e.getMessage()),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
