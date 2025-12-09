package engine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserManagerTest {

    @Mock
    private FileManager fileManagerMock;
    @Mock
    private AchievementManager achievementManagerMock;

    private MockedStatic<FileManager> fileManagerStaticMock;
    private MockedStatic<AchievementManager> achievementManagerStaticMock;

    private Map<String, User> userMap;

    @BeforeEach
    void setUp() {
        // Prepare a fresh user map for each test
        userMap = new HashMap<>();

        // Start mocking static getInstance() methods
        fileManagerStaticMock = Mockito.mockStatic(FileManager.class);
        achievementManagerStaticMock = Mockito.mockStatic(AchievementManager.class);

        // Define behavior for the mocked static methods
        fileManagerStaticMock.when(FileManager::getInstance).thenReturn(fileManagerMock);
        achievementManagerStaticMock.when(AchievementManager::getInstance).thenReturn(achievementManagerMock);

        try {
            // By default, assume loading returns our in-memory map
            when(fileManagerMock.loadUsers()).thenReturn(userMap);
            // By default, do nothing on save to avoid file system interaction
            doNothing().when(fileManagerMock).saveUsers(anyMap());
        } catch (IOException e) {
            fail("Setup failed due to IOException: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        // Close static mocks to avoid leakage between tests
        fileManagerStaticMock.close();
        achievementManagerStaticMock.close();

        // Reset the singleton instance using reflection to ensure test isolation
        try {
            java.lang.reflect.Field instanceField = UserManager.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset UserManager singleton instance", e);
        }
    }

    @Test
    @DisplayName("Login with existing user and correct password should succeed")
    void testLogin_Success_ExistingUser() {
        // Given: An existing user
        User existingUser = new User("test", "pass");
        userMap.put("test", existingUser);
        
        // When: UserManager is instantiated and login is called
        UserManager userManager = UserManager.getInstance();
        User loggedInUser = userManager.login("test", "pass");

        // Then: Login should be successful
        assertNotNull(loggedInUser);
        assertEquals("test", loggedInUser.getUsername());
    }

    @Test
    @DisplayName("Login with existing user and incorrect password should fail")
    void testLogin_Failure_WrongPassword() {
        // Given: An existing user
        User existingUser = new User("test", "pass");
        userMap.put("test", existingUser);

        // When: UserManager is instantiated and login is called with wrong password
        UserManager userManager = UserManager.getInstance();
        User loggedInUser = userManager.login("test", "wrongpass");

        // Then: Login should fail
        assertNull(loggedInUser);
    }

    @Test
    @DisplayName("Login with a new user should automatically register and log them in")
    void testLogin_Success_NewUser_AutoRegisters() throws IOException {
        // Given: An empty user database and no achievements
        when(achievementManagerMock.getAchievements()).thenReturn(new ArrayList<>());

        // When: A new user tries to log in
        UserManager userManager = UserManager.getInstance();
        User loggedInUser = userManager.login("newuser", "newpass");

        // Then: The user should be registered and logged in successfully
        assertNotNull(loggedInUser);
        assertEquals("newuser", loggedInUser.getUsername());
        
        // And the new user data should be saved
        verify(fileManagerMock).saveUsers(anyMap());
    }

    @Test
    @DisplayName("Registering a new user should succeed")
    void testRegister_Success() throws IOException {
        // Given: An empty user database
        when(achievementManagerMock.getAchievements()).thenReturn(new ArrayList<>());
        
        // When: A new user is registered
        UserManager userManager = UserManager.getInstance();
        boolean result = userManager.register("newuser", "newpass");
        
        // Then: Registration should be successful
        assertTrue(result);
        assertNotNull(userManager.getUsers().get("newuser"));
        verify(fileManagerMock).saveUsers(anyMap());
    }

    @Test
    @DisplayName("Registering an existing user should fail")
    void testRegister_Failure_UserExists() throws IOException {
        // Given: A user already exists
        userMap.put("existing", new User("existing", "pass"));
        
        // When: The same user tries to register again
        UserManager userManager = UserManager.getInstance();
        boolean result = userManager.register("existing", "pass");
        
        // Then: Registration should fail
        assertFalse(result);
        
        // And save should not be called
        verify(fileManagerMock, never()).saveUsers(anyMap());
    }

    @Test
    @DisplayName("Registration should fail if saving to file throws IOException")
    void testRegister_Failure_SaveFails() throws IOException {
        // Given: The file manager will fail to save
        doThrow(new IOException("Disk is full")).when(fileManagerMock).saveUsers(anyMap());
        when(achievementManagerMock.getAchievements()).thenReturn(new ArrayList<>());
        
        // When: A user is registered
        UserManager userManager = UserManager.getInstance();
        boolean result = userManager.register("newuser", "newpass");
        
        // Then: Registration should fail
        assertFalse(result);
        
        // And the user should not be in the in-memory map either
        assertNull(userManager.getUsers().get("newuser"));
    }
}
