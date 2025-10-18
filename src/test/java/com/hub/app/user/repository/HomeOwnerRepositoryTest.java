package com.hub.app.user.repository;

import com.hub.app.user.model.HomeOwner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class HomeOwnerRepositoryTest {

    @Autowired
    private HomeOwnerRepository homeOwnerRepository;

    @Test
    void findByEmail_ShouldReturnHomeOwnerWhenExists() {
        // Given
        HomeOwner homeOwner = new HomeOwner();
        homeOwner.setName("Test User");
        homeOwner.setEmail("test@example.com");
        homeOwner.setPhone("1234567890");

        HomeOwner saved = homeOwnerRepository.save(homeOwner);

        // When
        Optional<HomeOwner> found = homeOwnerRepository.findByEmail("test@example.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void findByEmail_ShouldReturnEmptyWhenNotExists() {
        // When
        Optional<HomeOwner> found = homeOwnerRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void save_ShouldPersistHomeOwner() {
        // Given
        HomeOwner homeOwner = new HomeOwner();
        homeOwner.setName("New User");
        homeOwner.setEmail("new@example.com");
        homeOwner.setPhone("0987654321");
        homeOwner.setKtpNumber("1234567890123456");

        // When
        HomeOwner saved = homeOwnerRepository.save(homeOwner);

        // Then
        assertNotNull(saved.getId());
        assertEquals("New User", saved.getName());
        assertEquals("new@example.com", saved.getEmail());
        assertEquals("0987654321", saved.getPhone());
        assertEquals("1234567890123456", saved.getKtpNumber());
    }

    @Test
    void findById_ShouldReturnHomeOwnerWhenExists() {
        // Given
        HomeOwner homeOwner = new HomeOwner();
        homeOwner.setName("Test User");
        homeOwner.setEmail("test@example.com");
        homeOwner.setPhone("1234567890");

        HomeOwner saved = homeOwnerRepository.save(homeOwner);

        // When
        Optional<HomeOwner> found = homeOwnerRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        // When
        Optional<HomeOwner> found = homeOwnerRepository.findById(999L);

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllHomeOwners() {
        // Given
        HomeOwner homeOwner1 = new HomeOwner();
        homeOwner1.setName("User 1");
        homeOwner1.setEmail("user1@example.com");
        homeOwner1.setPhone("1111111111");

        HomeOwner homeOwner2 = new HomeOwner();
        homeOwner2.setName("User 2");
        homeOwner2.setEmail("user2@example.com");
        homeOwner2.setPhone("2222222222");

        homeOwnerRepository.save(homeOwner1);
        homeOwnerRepository.save(homeOwner2);

        // When
        Iterable<HomeOwner> all = homeOwnerRepository.findAll();

        // Then
        int count = 0;
        for (HomeOwner h : all) {
            count++;
        }
        assertTrue(count >= 2); // At least the two we saved
    }

    @Test
    void existsById_ShouldReturnTrueWhenExists() {
        // Given
        HomeOwner homeOwner = new HomeOwner();
        homeOwner.setName("Test User");
        homeOwner.setEmail("test@example.com");
        homeOwner.setPhone("1234567890");

        HomeOwner saved = homeOwnerRepository.save(homeOwner);

        // When
        boolean exists = homeOwnerRepository.existsById(saved.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    void existsById_ShouldReturnFalseWhenNotExists() {
        // When
        boolean exists = homeOwnerRepository.existsById(999L);

        // Then
        assertFalse(exists);
    }

    @Test
    void delete_ShouldRemoveHomeOwner() {
        // Given
        HomeOwner homeOwner = new HomeOwner();
        homeOwner.setName("Test User");
        homeOwner.setEmail("test@example.com");
        homeOwner.setPhone("1234567890");

        HomeOwner saved = homeOwnerRepository.save(homeOwner);
        assertTrue(homeOwnerRepository.existsById(saved.getId()));

        // When
        homeOwnerRepository.delete(saved);

        // Then
        assertFalse(homeOwnerRepository.existsById(saved.getId()));
    }

    @Test
    void deleteById_ShouldRemoveHomeOwner() {
        // Given
        HomeOwner homeOwner = new HomeOwner();
        homeOwner.setName("Test User");
        homeOwner.setEmail("test@example.com");
        homeOwner.setPhone("1234567890");

        HomeOwner saved = homeOwnerRepository.save(homeOwner);
        assertTrue(homeOwnerRepository.existsById(saved.getId()));

        // When
        homeOwnerRepository.deleteById(saved.getId());

        // Then
        assertFalse(homeOwnerRepository.existsById(saved.getId()));
    }
}