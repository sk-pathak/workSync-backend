package org.openlake.workSync.app.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.openlake.workSync.app.domain.dto.User;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private int statusCode;
    private String message;

    private List<User> userList;
}
