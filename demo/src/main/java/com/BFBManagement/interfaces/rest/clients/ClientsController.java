package com.BFBManagement.interfaces.rest.clients;

import java.util.Collection;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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