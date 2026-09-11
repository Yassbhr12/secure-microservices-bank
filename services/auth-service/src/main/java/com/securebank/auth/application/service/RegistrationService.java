package com.securebank.auth.application.service;

import com.securebank.auth.application.exception.EmailAlreadyExistsException;
import com.securebank.auth.domain.model.User;
import com.securebank.auth.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String email, String rawPassword) {

        String passwordHash = passwordEncoder.encode(rawPassword);

        User user = User.registerClient(email, passwordHash);

        if (userRepository.existsByEmailIgnoreCase(user.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        try {
            //flush() force immédiatement l’envoi de l’insertion vers PostgreSQL.
            //Cela permet de recevoir la violation de contrainte à l’intérieur du try/catch
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
           //Si deux inscriptions concurrentes utilisent le même e-mail, PostgreSQL refuse la seconde.
            throw new EmailAlreadyExistsException();
        }
    }
}
