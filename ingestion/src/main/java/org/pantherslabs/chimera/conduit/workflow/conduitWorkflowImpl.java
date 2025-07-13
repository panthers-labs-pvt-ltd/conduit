package org.pantherslabs.chimera.conduit.workflow;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import org.pantherslabs.chimera.conduit.activities.conduitActivities;
//import org.pantherslabs.chimera.unisca.pipeline_metadata_api.dto.ExtractMetadata;
//import org.pantherslabs.chimera.unisca.pipeline_metadata_api.dto.PersistMetadata;
//import org.pantherslabs.chimera.unisca.pipeline_metadata_api.dto.PipelineMetadata;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class conduitWorkflowImpl implements conduitworkflow {

    ActivityOptions options = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofMinutes(5))
            .build(); // Set the activity options with a timeout of 5 minutes

     // Activities stub for invoking activities in the workflow



    @Override
    public void runWorkflow(String workflowId, String pipelineName) throws IOException, InterruptedException {
        // Implementation for running the workflow
//        Workflow.getLogger(getClass().getSimpleName()).info("Starting workflow with ID: " + workflowId + " for pipeline: " + pipelineName);
//        conduitActivities activities = Workflow.newActivityStub(conduitActivities.class, options);
//        PipelineMetadata pipelineMetadata = activities.getPipelineMetadata(pipelineName);
//        if (pipelineMetadata == null) {
//            throw new IOException("Pipeline metadata not found for pipeline: " + pipelineName);
//        }
//        List<ExtractMetadata> extractMetadata = pipelineMetadata.getExtractMetadata();
//        List<PersistMetadata> persistMetadata = pipelineMetadata.getPersistMetadata();
//
//        if (extractMetadata != null && !extractMetadata.isEmpty()) {
//          // Loop through each ExtractMetadata configuration and call the extractData activity
//            extractMetadata.forEach(config -> {
//                Workflow.getLogger(getClass().getSimpleName()).info("Extracting data with configuration: " + config);
//                try {
//                    activities.extractData(config);
//                    } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            });
//        }
//
//        if (persistMetadata != null && !persistMetadata.isEmpty()) {
//            // Loop through each PersistMetadata configuration and call the extractData activity
//            persistMetadata.forEach(config -> {
//                Workflow.getLogger(getClass().getSimpleName()).info("Persisting data with configuration: " + config);
//                try {
//                    activities.persistData(config);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }
//            });
//        }
//    }
//        Workflow.getLogger(getClass().getSimpleName()).info("Workflow completed successfully for pipeline: " + pipelineName);}
    }
}
