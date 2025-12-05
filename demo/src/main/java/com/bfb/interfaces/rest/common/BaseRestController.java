package com.bfb.interfaces.rest.common;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseRestController<T, D> {

    protected ResponseEntity<D> created(D dto) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(dto);
    }

    protected ResponseEntity<D> ok(D dto) {
        return ResponseEntity.ok(dto);
    }

    protected ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    protected ResponseEntity<Page<D>> okPage(Page<D> page) {
        return ResponseEntity.ok(page);
    }

    protected ResponseEntity<D> accepted(D dto) {
        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(dto);
    }
}
