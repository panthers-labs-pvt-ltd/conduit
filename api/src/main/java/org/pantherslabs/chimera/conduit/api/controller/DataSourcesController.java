package org.pantherslabs.chimera.conduit.api.controller;


import org.pantherslabs.chimera.conduit.api.model.DataSources;
import org.pantherslabs.chimera.conduit.api.service.DataSourcesService;
import org.pantherslabs.chimera.unisca.api_nexus.api_nexus_client.response.StandardResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/data-sources")
public class DataSourcesController {
    @Autowired
    private DataSourcesService dataSourcesService;

    @GetMapping
    public ResponseEntity<StandardResponse<List<DataSources>>> getAllDataSources() {
        List<DataSources> dataSourcesList = dataSourcesService.getAllDataSources();
        return ResponseEntity.ok(StandardResponse.success(dataSourcesList));
    }

    @GetMapping("/{dataSourceType}")
    public ResponseEntity<StandardResponse<List<DataSources>>> getDataSourceByName(@PathVariable("dataSourceType") String dataSourceType) {
        List<DataSources> dataSourcesList = dataSourcesService.getDataSourceByType(dataSourceType);
        return ResponseEntity.ok(StandardResponse.success(dataSourcesList));
    }

    @PostMapping("/create")
    public ResponseEntity<StandardResponse<DataSources>> createOrganizationType(@RequestBody DataSources dataSource) {
        DataSources createdDataSource = dataSourcesService.createDataSource(dataSource);
        return ResponseEntity.ok(StandardResponse.success("Data Source type : " + dataSource.getDataSourceType() + "and  sub-type: " + dataSource.getDataSourceSubType() + " created.", createdDataSource));
    }
    @PutMapping("/update")
    public ResponseEntity<StandardResponse<DataSources>> updateOrganizationType(@RequestBody DataSources dataSource) {
        DataSources updatedDataSource = dataSourcesService.updateDataSource(dataSource);
        return ResponseEntity.ok(StandardResponse.success(updatedDataSource));
    }
    @DeleteMapping("/delete/{dataSourceType}/{subType}")
    public ResponseEntity<StandardResponse<String>> deleteOrganizationType(@PathVariable("dataSourceType") String dataSourceType, @PathVariable("subType")String subType) {
        dataSourcesService.deleteDataSource(dataSourceType, subType);
        return ResponseEntity.ok(StandardResponse.success("DataSourceType : " + dataSourceType + "and sub-type: " + subType + " deleted successfully"));
    }
}
