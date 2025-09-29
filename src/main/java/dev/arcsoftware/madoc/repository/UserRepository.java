package dev.arcsoftware.madoc.repository;

import dev.arcsoftware.madoc.auth.model.UserEntity;
import dev.arcsoftware.madoc.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcClient jdbcClient;

    @Autowired
    public UserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<UserEntity> findUserByUsername(String username) {
        Optional<UserEntity> optUser = jdbcClient
                .sql(UsersSql.GET_USER_BY_USERNAME)
                .param("username", username)
                .query(UserEntity.class)
                .optional();

        if(optUser.isEmpty()) return optUser;

        //Fetch roles
        var roles = jdbcClient
                .sql(UsersSql.FETCH_USER_ROLES)
                .param("username", username)
                .query(Role.class)
                .list();
        optUser.get().setRoles(roles);

        return optUser;
    }

    public void updateUserPasswordHash(UserEntity user) {
        user.setUpdatedAt(LocalDateTime.now());
        jdbcClient
                .sql(UsersSql.UPDATE_USER_PASSWORD)
                .param("username", user.getUsername())
                .param("password_hash", user.getPasswordHash())
                .param("updated_at", user.getUpdatedAt())
                .update();
    }

    public void insertUser(UserEntity user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        jdbcClient
                .sql(UsersSql.INSERT_USER)
                .param("username", user.getUsername())
                .param("password_hash", user.getPasswordHash())
                .param("created_at", user.getCreatedAt())
                .param("updated_at", user.getUpdatedAt())
                .update();
        //Insert the roles
        for (Role roleName : Optional.ofNullable(user.getRoles()).orElse(new ArrayList<>())) {
            jdbcClient
                    .sql(UsersSql.INSERT_ROLE_ASSIGNMENT)
                    .param("username", user.getUsername())
                    .param("role_name", roleName.name())
                    .update();
        }
    }


    public static class UsersSql {
        public static final String INSERT_USER = """
        INSERT INTO madoc.users (username, password_hash, created_at, updated_at)
        VALUES (:username, :password_hash, :created_at, :updated_at)
        """;

        public static final String GET_USER_BY_USERNAME = """
        SELECT username, password_hash, created_at, updated_at from madoc.users
        WHERE username = :username
        """;

        public static final String UPDATE_USER_PASSWORD = """
        UPDATE madoc.users
        SET password_hash = :password_hash, updated_at = :updated_at
        WHERE username = :username
        """;

        public static final String INSERT_ROLE_ASSIGNMENT = """
        INSERT INTO madoc.role_assignments (username, role_id)
        SELECT :username, r.id FROM madoc.roles r WHERE r.role = :role_name
        """;

        public static final String FETCH_USER_ROLES = """
        SELECT r.role FROM madoc.roles r
        JOIN madoc.role_assignments ra ON r.id = ra.role_id
        WHERE ra.username = :username
        """;
    }
}
