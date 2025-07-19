package org.pantherslabs.chimera.conduit.api.controller;



import org.pantherslabs.chimera.conduit.api.model.DataSourcesConnections;
import org.pantherslabs.chimera.conduit.api.service.DataSourcesConnectionsService;
import org.pantherslabs.chimera.unisca.api_nexus.api_nexus_client.response.StandardResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/source-connections")
public class DataSourcesConnectionsController {
    @Autowired
    private DataSourcesConnectionsService dataSourcesConnectionsService;

    @GetMapping
    public ResponseEntity<StandardResponse<List<DataSourcesConnections>>> getAllSourceConnections() {
        List<DataSourcesConnections> sourcesConnectionsList = dataSourcesConnectionsService.getAllSourcesConnections();
        return ResponseEntity.ok(StandardResponse.success(sourcesConnectionsList));
    }

    @GetMapping("/{sourceConnectionName}")
    public ResponseEntity<StandardResponse<DataSourcesConnections>> getSourceConnectionByName(@PathVariable("sourceConnectionName") String sourceConnectionName) {
        DataSourcesConnections sourcesConnectionsList = dataSourcesConnectionsService.getSourceConnectionByName(sourceConnectionName);
        return ResponseEntity.ok(StandardResponse.success(sourcesConnectionsList));
    }

    @PostMapping("/create")
    public ResponseEntity<StandardResponse<DataSourcesConnections>> createSourceConnection(@RequestBody DataSourcesConnections sourceConnection) {
        DataSourcesConnections createdSourceConnection = dataSourcesConnectionsService.createSourceConnection(sourceConnection);
        return ResponseEntity.ok(StandardResponse.success("Source Connection : " + sourceConnection.getDataSourceConnectionName() + " created.", createdSourceConnection));
    }
    @PutMapping("/update")
    public ResponseEntity<StandardResponse<DataSourcesConnections>> updateSourceConnection(@RequestBody DataSourcesConnections sourceConnection) {
        DataSourcesConnections updatedSourceConnection = dataSourcesConnectionsService.updateSourceConnection(sourceConnection);
        return ResponseEntity.ok(StandardResponse.success(updatedSourceConnection));
    }
    @DeleteMapping("/delete/{sourceConnectionName}")
    public ResponseEntity<StandardResponse<String>> deleteSourceConnection(@PathVariable("sourceConnectionName") String sourceConnectionName) {
        dataSourcesConnectionsService.deleteSourceConnection(sourceConnectionName);
        return ResponseEntity.ok(StandardResponse.success("Source Connection config with name : " + sourceConnectionName + " deleted successfully"));
    }
}
