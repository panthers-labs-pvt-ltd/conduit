package org.pantherslabs.chimera.conduit.api.service;

import org.apache.ibatis.exceptions.PersistenceException;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.pantherslabs.chimera.conduit.api.mapper.DataSourcesMapper;
import org.pantherslabs.chimera.conduit.api.model.DataSources;
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
import static org.pantherslabs.chimera.conduit.api.mapper.DataSourcesDynamicSqlSupport.dataSources;

@Service
public class DataSourcesService {
    static ChimeraLogger DSLogger = ChimeraLoggerFactory.getLogger(DataSourcesService.class);
    @Autowired
    DataSourcesMapper dataSourcesMapper;
    public List<DataSources> getAllDataSources() {
        DSLogger.logInfo("Fetching all Data Sources");
        try {
            SelectStatementProvider selectStatement = select(DataSourcesMapper.selectList)
                    .from(dataSources)
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<DataSources> dataSourcesList = dataSourcesMapper.selectMany(selectStatement);
            if (dataSourcesList.isEmpty()) {
                DSLogger.logInfo("No Data Source found");
                throw new ChimeraException("APIException.404",
                        Map.of("exception", "No Data Source found"),
                        null,
                        HttpStatus.NO_CONTENT
                );
            }
            DSLogger.logInfo(String.format("Successfully fetched %s Data Sources ", dataSourcesList.size()));
            return dataSourcesList;
        } catch (Exception e) {
            DSLogger.logError("Unexpected error while retrieving Data Sources: " + e.getMessage());
            throw new ChimeraException(
                    "APIException.500",
                    Map.of("exception", e.getMessage()),
                    null,
                    HttpStatus.EXPECTATION_FAILED
            );
        }
    }

    public List<DataSources> getDataSourceByType(String dataSourceType) {
        DSLogger.logInfo(String.format("Fetching Data Sources of type: %s", dataSourceType));
        try {
            if (dataSourceType == null || dataSourceType.isEmpty()) {
                DSLogger.logError("Data Source Type cannot be null or empty");
                throw new ChimeraException("APIException.400",
                        Map.of("exception", "Data Source Type cannot be null or empty"),
                        null,
                        HttpStatus.BAD_REQUEST
                );
            }
            SelectStatementProvider selectStatement = select(DataSourcesMapper.selectList)
                    .from(dataSources)
                    .where(dataSources.dataSourceType, isEqualTo(dataSourceType))
                    .build()
                    .render(RenderingStrategies.MYBATIS3);
            List<DataSources> dataSourcesList = dataSourcesMapper.selectMany(selectStatement);
            if (dataSourcesList.isEmpty()) {
                DSLogger.logInfo("No Data Source found for Type: " + dataSourceType);
                throw new ChimeraException("APIException.404",
                        Map.of("exception", "No Data Source found"),
                        null,
                        HttpStatus.NO_CONTENT
                );
            }
            DSLogger.logInfo(String.format("Successfully fetched %s Data Sources ", dataSourcesList.size()));
            return dataSourcesList;
        } catch (Exception e) {
            DSLogger.logError("Unexpected error while retrieving Data Sources: " + e.getMessage());
            throw new ChimeraException(
                    "APIException.500",
                    Map.of("exception", e.getMessage()),
                    null,
                    HttpStatus.EXPECTATION_FAILED
            );
        }

    }

    public DataSources getDataSourceByType(String dataSourceType, String dataSourceSubType) {
        DSLogger.logInfo(String.format("Fetching Data Sources of type: %s", dataSourceType));
        if (dataSourceType == null || dataSourceType.isEmpty() || dataSourceSubType == null || dataSourceSubType.isEmpty()) {
            DSLogger.logError("Data Source Type cannot be null or empty");
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "Data Source Type cannot be null or empty"),
                    null,
                    HttpStatus.BAD_REQUEST
            );
        }
        SelectStatementProvider selectStatement = select(DataSourcesMapper.selectList)
                .from(dataSources)
                .where(dataSources.dataSourceType, isEqualTo(dataSourceType))
                .and(dataSources.dataSourceSubType, isEqualTo(dataSourceSubType))
                .build()
                .render(RenderingStrategies.MYBATIS3);
        return dataSourcesMapper.selectOne(selectStatement)
                .orElseThrow(() -> new ChimeraException("APIException.404",
                        Map.of("exception", String.format("No Data Source found for Type: %s and sub-type %s", dataSourceType, dataSourceSubType)),
                        null,
                        HttpStatus.NOT_FOUND
                ));

    }

    @Transactional
    public DataSources createDataSource(DataSources dataSource) {
        if (dataSource == null || dataSource.getDataSourceSubType() == null || dataSource.getDataSourceType() == null) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "Data Source details cannot be null"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }
        if (dataSource.getCreatedBy() == null) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "createdBy column cannot be null"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }

        DSLogger.logInfo(String.format("Creating new Data Source of type : %s and sub-type : %s", dataSource.getDataSourceType(), dataSource.getDataSourceSubType()));

        if (dataSource.getCreatedTimestamp() == null) {
            dataSource.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));
        }


        try {
            int result = dataSourcesMapper.insert(dataSource);

            if (result != 1) {
                throw new ChimeraException("APIException.500",
                        Map.of("exception", "Failed to create data source of type: %s and sub-type: %s", dataSource.getDataSourceType(), dataSource.getDataSourceSubType()),
                        null,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }

            return dataSource;
        } catch (PersistenceException e) {
            // These catch DB-level exceptions like constraint violations, bad SQL, etc.
            DSLogger.logError("Database error while inserting Data Source: " + e.getMessage(), e);
            throw new ChimeraException(
                    "APIException.500",
                    Map.of("exception", "Database error: " + e.getMessage()),
                    null,
                    HttpStatus.NOT_ACCEPTABLE
            );
        } catch (ChimeraException e) {
            throw e;
        } catch (Exception e) {
            DSLogger.logError("Error creating Data Source: " + e.getMessage());
            throw new ChimeraException("APIException.500",
                    Map.of("exception", e.getMessage()),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    public DataSources updateDataSource(DataSources dataSource) {
        if (dataSource.getDataSourceType() == null || dataSource.getDataSourceSubType().isBlank() || dataSource == null) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "Invalid input parameters"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }
        if (dataSource.getUpdatedBy() == null) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "updatedBy column cannot be null"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }
        if (dataSource.getUpdatedTimestamp() == null) {
            dataSource.setUpdatedTimestamp(new Timestamp(System.currentTimeMillis()));
        }

        DSLogger.logInfo(String.format("Updating DataSource with type : %s and sub-type: %s", dataSource.getDataSourceType(), dataSource.getDataSourceSubType()));

        // Verify the organization exists first
        getDataSourceByType(dataSource.getDataSourceType(), dataSource.getDataSourceSubType());

        try {
            int rowsUpdated = dataSourcesMapper.updateByPrimaryKeySelective(dataSource);

            if (rowsUpdated != 1) {
                throw new ChimeraException("APIException.500",
                        Map.of("exception", "Failed to update data source type: %s and sub-type: %s" + dataSource.getDataSourceType() + " and sub-type: " + dataSource.getDataSourceSubType()),
                        null,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            DSLogger.logInfo("Data Source type: " + dataSource.getDataSourceType() + " and sub-type: " + dataSource.getDataSourceSubType() + " updated successfully");
            return getDataSourceByType(dataSource.getDataSourceType(), dataSource.getDataSourceSubType());

        } catch (PersistenceException e) {
            DSLogger.logError("Database error during update: " + e.getMessage(), e);
            throw new ChimeraException(
                    "APIException.500",
                    Map.of("exception", "Database error during update: " + e.getMessage()),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        } catch (ChimeraException e) {
            throw e;

        } catch (Exception e) {
            DSLogger.logError("Unexpected error during update: " + e.getMessage(), e);
            throw new ChimeraException(
                    "APIException.500",
                    Map.of("exception", "Unexpected error while updating Team " + e.getMessage()),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

    }

    public void deleteDataSource(String dataSourceType, String dataSourceSubType) {
        if (dataSourceType == null || dataSourceType.isBlank() ||dataSourceSubType == null || dataSourceSubType.isBlank()) {
            throw new ChimeraException("APIException.400",
                    Map.of("exception", "Data Source Type or sub-type cannot be empty"),
                    null,
                    HttpStatus.BAD_REQUEST);
        }

        DSLogger.logInfo(String.format("Deleting Data Source with type: %s and sub-type", dataSourceType, dataSourceSubType));

        // Verify the organization exists first
        getDataSourceByType(dataSourceType, dataSourceSubType);

        try {
            int result = dataSourcesMapper.deleteByPrimaryKey(dataSourceType, dataSourceSubType);

            if (result != 1) {
                throw new ChimeraException("APIException.500",
                        Map.of("exception", "Failed to delete data source with type %s and sub-type %s", dataSourceType, dataSourceSubType),
                        null,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            DSLogger.logError(String.format("Failed to delete data source with type %s and sub-type %s", dataSourceType, dataSourceSubType) + e.getMessage());
            throw new ChimeraException("APIException.500",
                    Map.of("exception", e.getMessage()),
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
