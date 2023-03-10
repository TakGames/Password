package org.acme.password;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.acme.Hash.HashMethods;
import org.acme.password.user.User;
import org.acme.password.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRepositoryTest {

    @Inject
    UserRepository userRepository;

    @Test
    @TestTransaction
    @Order(13)
    public void testRegister() {
        User user = new User("peter@gmail.com","12345678","+43667667");

        User savedUser = userRepository.registerUser(user);

        Assertions.assertThat(savedUser.getEmail()).isEqualTo("peter@gmail.com");
    }

    @Test
    @TestTransaction
    @Order(14)
    public void testRegisterAlreadyExistingUser() {

        User user = new User("peter2@gmail.com","12345678","+43667667");
        User savedUser = userRepository.registerUser(user);
        Assertions.assertThat(savedUser.getEmail()).isEqualTo("peter2@gmail.com");
        User savedUser2 = userRepository.registerUser(user);
        Assertions.assertThat(savedUser2).isEqualTo(null);
    }
    @Test
    @TestTransaction
    public void testRegisterUserWithWrongEmail() {
        User user = new User("peter2@gmailcom","12345678","+43667667");
        try {
            userRepository.registerUser(user);
        }catch(ConstraintViolationException e){
            Assertions.assertThatExceptionOfType(ConstraintViolationException.class);
        }

        User user2 = new User("peter2gmail.com","12345678","+43667667");
        try {
            userRepository.registerUser(user2);
        }catch(ConstraintViolationException e){
            Assertions.assertThatExceptionOfType(ConstraintViolationException.class);
        }
    }

    @Test
    @TestTransaction
    public void testRegisterUserWithNoEmail() {
        User user = new User(null,"12345678","+43667667");
        try {
            userRepository.registerUser(user);
        }catch(IllegalArgumentException e){
            Assertions.assertThatExceptionOfType(IllegalArgumentException.class);
        }

    }

    @Test
    @TestTransaction
    public void testRegisterUserWithNoPassword() {
        User user = new User("jona@gmail.com",null,"+43667667");
        try {
            userRepository.registerUser(user);
        }catch(IllegalArgumentException e){
            Assertions.assertThatExceptionOfType(IllegalArgumentException.class);
        }

    }

    @Test
    @TestTransaction
    @Order(15)
    public void testCheckPassword(){
        String email = "hans@gmail.com";
        User user = new User(email,"1234567B",null);
        user = userRepository.registerUser(user);
        System.out.println("1234567B");
        Assertions.assertThat(userRepository.checkPassword(email, "1234567B")).isEqualTo(true);
        System.out.println("1234567b");
        Assertions.assertThat(userRepository.checkPassword(email, "1234457b")).isEqualTo(false);
    }

    @Test
    @TestTransaction
    @Order(16)
    public void testCheckPasswordWithNotExistingUser(){
        Assertions.assertThat(userRepository.checkPassword(null, "12345")).isEqualTo(false);
    }

    @Test
    @TestTransaction
    public void testForgotCode() {
        String email = "hans@gmail.com";
        User user = new User(email, "1234567B", null);
        user = userRepository.registerUser(user);

        String code = userRepository.forgotPassword(email);
        String check = userRepository.checkCode(email, code);

        Assertions.assertThat(check).isNotEqualTo(null);
        Assertions.assertThat(userRepository.checkPassword(email, check)).isEqualTo(true);
    }

    @Test
    @TestTransaction
    public void testChangePassword(){
        String email = "hans@gmail.com";
        String pwOld = "1234567B";
        String pwNew = "Test123";
        User user = new User(email, pwOld, null);
        user = userRepository.registerUser(user);

        user.setPassword(HashMethods.hashPassword(pwNew,user.getSalt()));
        boolean change = userRepository.changePassword(email,pwOld,pwNew);
        Assertions.assertThat(userRepository.checkPassword(email,pwNew)).isEqualTo(true);
    }
}
