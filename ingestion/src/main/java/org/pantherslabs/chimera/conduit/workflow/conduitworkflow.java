package org.pantherslabs.chimera.conduit.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.io.IOException;

@WorkflowInterface
public interface conduitworkflow {
    @WorkflowMethod
    void runWorkflow(String workflowId, String pipelineName) throws IOException, InterruptedException;

}
