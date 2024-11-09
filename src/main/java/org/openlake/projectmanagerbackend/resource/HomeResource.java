package org.openlake.projectmanagerbackend.resource;

import org.openlake.projectmanagerbackend.domain.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeResource {

    @GetMapping({"/", "/home"})
    public ResponseEntity<Response> home() {
        Response response = new Response();
        response.setStatusCode(200);
        response.setMessage("Success");
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
