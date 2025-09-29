package com.project.demo.logic.entity.rol;

import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Order(3)
@Component
public class UserSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserSeeder(RoleRepository roleRepository,
                      UserRepository userRepository,
                      PasswordEncoder encoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        initDefaultUser();
    }

    private void initDefaultUser() {
        String email = "user@gmail.com";

        Optional<User> existingUser = userRepository.findByEmail(email);
        Optional<Role> role = roleRepository.findByName(RoleEnum.USER);

        if (existingUser.isPresent() || role.isEmpty()) {
            return;
        }

        User defaultUser = new User();
        defaultUser.setName("User");
        defaultUser.setLastname("Client");
        defaultUser.setEmail(email);
        defaultUser.setPassword(encoder.encode("user123"));
        defaultUser.setRole(role.get());

        userRepository.save(defaultUser);
    }
}