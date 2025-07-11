package org.openlake.workSync.app.domain.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class TaskAssignmentPayload {
    @JsonProperty("taskId")
    private UUID taskId;
    
    @JsonProperty("title")
    private String title;

    public TaskAssignmentPayload() {}

    public TaskAssignmentPayload(UUID taskId, String title) {
        this.taskId = taskId;
        this.title = title;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
} 