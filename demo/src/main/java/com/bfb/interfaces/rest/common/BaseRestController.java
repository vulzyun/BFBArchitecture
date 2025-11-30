package com.bfb.interfaces.rest.common;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Base controller providing common HTTP response patterns.
 * All REST controllers should extend this class for consistency.
 * 
 * @param <T> Domain entity type
 * @param <D> DTO type
 */
public abstract class BaseRestController<T, D> {

    /**
     * Returns a 201 CREATED response with the created resource.
     */
    protected ResponseEntity<D> created(D dto) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(dto);
    }

    /**
     * Returns a 200 OK response with the resource.
     */
    protected ResponseEntity<D> ok(D dto) {
        return ResponseEntity.ok(dto);
    }

    /**
     * Returns a 204 NO CONTENT response.
     */
    protected ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns a 200 OK response with a paginated result.
     */
    protected ResponseEntity<Page<D>> okPage(Page<D> page) {
        return ResponseEntity.ok(page);
    }

    /**
     * Returns a 202 ACCEPTED response with a resource.
     */
    protected ResponseEntity<D> accepted(D dto) {
        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(dto);
    }
}
