package app.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import app.models.User;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

}