package pact.shopping.price.controller;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

public interface IController {
    default URI getURI(Long id) {
        return ServletUriComponentsBuilder.fromCurrentServletMapping().path("/{id}").build()
                .expand(id).toUri();
    }
}
