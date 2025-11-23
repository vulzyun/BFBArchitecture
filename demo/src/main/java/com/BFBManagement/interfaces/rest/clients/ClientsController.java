package com.BFBManagement.interfaces.rest.clients;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import com.BFBManagement.business.clients;

import java.util.Collection;

/**
 * @author vynx
 */
@RestController
@RequestMapping("/api/clients")
public class ClientsController {

    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<ClientsDto> getClients() {
        return null; // TODO: à implémenter
    }
}


// controller qui recoit un DTO et il renvoi un dto 