package screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import engine.*;
import java.awt.event.KeyEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoginScreenTest {

    @Mock
    private InputManager inputManager;
    @Mock
    private DrawManager drawManager;
    @Mock
    private Cooldown navigationCooldown;
    @Mock
    private Cooldown typingCooldown;
    @Mock
    private Cooldown errorCooldown;
    @Mock
    private Cooldown inputDelay;
    @Mock
    private AchievementManager achievementManagerMock;

    private LoginScreen loginScreen;

    @BeforeEach
    void setUp() {
        // Create an instance of LoginScreen with real parameters
        loginScreen = new LoginScreen(448, 520, 60);

        // Inject mocks manually using the setters
        loginScreen.setInputManager(inputManager);
        loginScreen.setDrawManager(drawManager);
        loginScreen.setNavigationCooldown(navigationCooldown);
        loginScreen.setTypingCooldown(typingCooldown);
        loginScreen.setErrorCooldown(errorCooldown);
        loginScreen.setInputDelay(inputDelay);
        
        loginScreen.initialize();
    }

    @Test
    void testInitialState() {
        assertEquals("", loginScreen.getUsername(), "Initial username should be empty.");
        assertEquals("", loginScreen.getPassword(), "Initial password should be empty.");
        assertEquals(0, loginScreen.getSelectedField(), "Initial selected field should be username (0).");
        assertNull(loginScreen.getErrorMessage(), "Initial error message should be null.");
    }

    @Test
    void testNavigation_DownAndUp() {
        // Given: Cooldowns are finished
        when(inputDelay.checkFinished()).thenReturn(true);
        when(navigationCooldown.checkFinished()).thenReturn(true);

        // When: Down arrow is pressed
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        loginScreen.update();
        
        // Then: Selected field should be 1 (password)
        assertEquals(1, loginScreen.getSelectedField(), "Should navigate down to password field.");

        // Given: Next state
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        
        // When: Down arrow is pressed again
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(true);
        loginScreen.update();

        // Then: Selected field should be 2 (login button)
        assertEquals(2, loginScreen.getSelectedField(), "Should navigate down to login button.");

        // When: Up arrow is pressed
        when(inputManager.isKeyDown(KeyEvent.VK_DOWN)).thenReturn(false);
        when(inputManager.isKeyDown(KeyEvent.VK_UP)).thenReturn(true);
        loginScreen.update();

        // Then: Selected field should be 1 (password)
        assertEquals(1, loginScreen.getSelectedField(), "Should navigate up to password field.");
    }

    @Test
    void testUsernameInput_AppendCharacter() {
        // Given: Username field is selected and cooldown is finished
        loginScreen.setSelectedField(0);
        when(inputDelay.checkFinished()).thenReturn(true);
        when(typingCooldown.checkFinished()).thenReturn(true);

        // When: 'A' key is pressed
        when(inputManager.isKeyDown(KeyEvent.VK_A)).thenReturn(true);
        loginScreen.update();

        // Then: Username should be "a"
        assertEquals("a", loginScreen.getUsername());
        verify(typingCooldown).reset();
    }

    @Test
    void testPasswordInput_Backspace() {
        // Given: Password field is selected, has text, and cooldown is finished
        loginScreen.setSelectedField(1); // Password field
        // Set initial password text to "pass"
        when(inputDelay.checkFinished()).thenReturn(true);
        when(typingCooldown.checkFinished()).thenReturn(true);
        when(inputManager.isKeyDown(KeyEvent.VK_P)).thenReturn(true); loginScreen.update(); when(inputManager.isKeyDown(KeyEvent.VK_P)).thenReturn(false);
        when(inputManager.isKeyDown(KeyEvent.VK_A)).thenReturn(true); loginScreen.update(); when(inputManager.isKeyDown(KeyEvent.VK_A)).thenReturn(false);
        when(inputManager.isKeyDown(KeyEvent.VK_S)).thenReturn(true); loginScreen.update();

        // When: Backspace is pressed
        when(inputManager.isKeyDown(KeyEvent.VK_BACK_SPACE)).thenReturn(true);
        loginScreen.update();

        // Then: Password should be "pa"
        assertEquals("pa", loginScreen.getPassword());
        verify(typingCooldown, times(4)).reset(); // 3 for chars, 1 for backspace
    }

    @Test
    void testLogin_Success() {
        // Mock static getInstance methods
        try (MockedStatic<UserManager> userManagerMock = mockStatic(UserManager.class);
             MockedStatic<AchievementManager> achievementManagerStaticMock = mockStatic(AchievementManager.class);
             MockedStatic<Core> coreMock = mockStatic(Core.class)) {
            
            // Given: UserManager and AchievementManager are mocked
            UserManager mockUserManagerInstance = mock(UserManager.class);
            when(UserManager.getInstance()).thenReturn(mockUserManagerInstance);
            when(AchievementManager.getInstance()).thenReturn(achievementManagerMock);

            // Given: Login credentials are valid
            User user = new User("test", "pass");
            when(mockUserManagerInstance.login("test", "pass")).thenReturn(user);

            // Given: LoginScreen has username and password typed
            loginScreen.setSelectedField(0); // Username
            when(inputDelay.checkFinished()).thenReturn(true);
            when(typingCooldown.checkFinished()).thenReturn(true);
            when(inputManager.isKeyDown(KeyEvent.VK_T)).thenReturn(true); loginScreen.update(); when(inputManager.isKeyDown(KeyEvent.VK_T)).thenReturn(false);
            when(inputManager.isKeyDown(KeyEvent.VK_E)).thenReturn(true); loginScreen.update(); when(inputManager.isKeyDown(KeyEvent.VK_E)).thenReturn(false);
            when(inputManager.isKeyDown(KeyEvent.VK_S)).thenReturn(true); loginScreen.update(); when(inputManager.isKeyDown(KeyEvent.VK_S)).thenReturn(false);
            when(inputManager.isKeyDown(KeyEvent.VK_T)).thenReturn(true); loginScreen.update(); when(inputManager.isKeyDown(KeyEvent.VK_T)).thenReturn(false);
            
            loginScreen.setSelectedField(1); // Password
            when(inputManager.isKeyDown(KeyEvent.VK_P)).thenReturn(true); loginScreen.update(); when(inputManager.isKeyDown(KeyEvent.VK_P)).thenReturn(false);
            when(inputManager.isKeyDown(KeyEvent.VK_A)).thenReturn(true); loginScreen.update(); when(inputManager.isKeyDown(KeyEvent.VK_A)).thenReturn(false);
            when(inputManager.isKeyDown(KeyEvent.VK_S)).thenReturn(true); loginScreen.update(); when(inputManager.isKeyDown(KeyEvent.VK_S)).thenReturn(false);
            when(inputManager.isKeyDown(KeyEvent.VK_S)).thenReturn(true); loginScreen.update(); when(inputManager.isKeyDown(KeyEvent.VK_S)).thenReturn(false);

            // When: Login button is pressed
            loginScreen.setSelectedField(2);
            when(inputManager.isKeyDown(KeyEvent.VK_ENTER)).thenReturn(true);
            loginScreen.update();

            // Then: Login should be successful
            assertEquals(1, loginScreen.getReturnCode(), "Return code should be 1 for success.");
            assertFalse(loginScreen.isRunning(), "Login screen should stop running on success.");
            coreMock.verify(() -> Core.setCurrentUser(user)); // Verify that the current user was set
            verify(achievementManagerMock).syncAchievementsWithUser(user); // Verify achievement sync
        }
    }

    @Test
    void testLogin_Failure() {
        try (MockedStatic<UserManager> userManagerMock = mockStatic(UserManager.class)) {
            // Given: UserManager is mocked and will fail login
            UserManager mockUserManagerInstance = mock(UserManager.class);
            when(UserManager.getInstance()).thenReturn(mockUserManagerInstance);
            when(mockUserManagerInstance.login("test", "wrong")).thenReturn(null);

            // Given: LoginScreen has username and password typed
            loginScreen.setSelectedField(0);
            when(inputDelay.checkFinished()).thenReturn(true);
            when(typingCooldown.checkFinished()).thenReturn(true);
            when(inputManager.isKeyDown(KeyEvent.VK_T)).thenReturn(true); loginScreen.update(); when(inputManager.isKeyDown(KeyEvent.VK_T)).thenReturn(false);
            when(inputManager.isKeyDown(KeyEvent.VK_E)).thenReturn(true); loginScreen.update(); when(inputManager.isKeyDown(KeyEvent.VK_E)).thenReturn(false);
            when(inputManager.isKeyDown(KeyEvent.VK_S)).thenReturn(true); loginScreen.update(); when(inputManager.isKeyDown(KeyEvent.VK_S)).thenReturn(false);
            when(inputManager.isKeyDown(KeyEvent.VK_T)).thenReturn(true); loginScreen.update(); when(inputManager.isKeyDown(KeyEvent.VK_T)).thenReturn(false);
            
            loginScreen.setSelectedField(1); // Password
            when(inputManager.isKeyDown(KeyEvent.VK_W)).thenReturn(true); loginScreen.update(); when(inputManager.isKeyDown(KeyEvent.VK_W)).thenReturn(false);
            when(inputManager.isKeyDown(KeyEvent.VK_R)).thenReturn(true); loginScreen.update(); when(inputManager.isKeyDown(KeyEvent.VK_R)).thenReturn(false);
            when(inputManager.isKeyDown(KeyEvent.VK_O)).thenReturn(true); loginScreen.update(); when(inputManager.isKeyDown(KeyEvent.VK_O)).thenReturn(false);
            when(inputManager.isKeyDown(KeyEvent.VK_N)).thenReturn(true); loginScreen.update(); when(inputManager.isKeyDown(KeyEvent.VK_N)).thenReturn(false);
            when(inputManager.isKeyDown(KeyEvent.VK_G)).thenReturn(true); loginScreen.update();
            
            // When: Login button is pressed
            loginScreen.setSelectedField(2);
            when(inputManager.isKeyDown(KeyEvent.VK_ENTER)).thenReturn(true);
            loginScreen.update();

            // Then: An error message should be displayed
            assertNotNull(loginScreen.getErrorMessage(), "Error message should be displayed on failed login.");
            assertEquals("Invalid password.", loginScreen.getErrorMessage());
            verify(errorCooldown).reset();
        }
    }
}
