package app.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import app.models.User;

@RequestMapping("/user")
public interface UserApi {

    @GetMapping(value = "/me")
    ResponseEntity<User> me();

}