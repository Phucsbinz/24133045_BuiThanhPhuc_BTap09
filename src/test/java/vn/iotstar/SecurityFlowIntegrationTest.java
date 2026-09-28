package vn.iotstar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.UserMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
public class SecurityFlowIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserMapper userMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("Portal page (/) is public and accessible without authentication")
    void portalPageIsPublic() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    // ==========================================
    // VÍ DỤ 1 (VD1) TESTS
    // ==========================================

    @Test
    @DisplayName("VD1: Unauthenticated request to /vd1/home redirects to /vd1/login")
    void vd1UnauthenticatedRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/vd1/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vd1/login"));
    }

    @Test
    @DisplayName("VD1: Login with valid email succeeds and redirects to /vd1/home")
    void vd1LoginWithValidEmailSucceeds() throws Exception {
        mockMvc.perform(post("/vd1/login")
                        .with(csrf())
                        .param("email", "user01@gmail.com")
                        .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vd1/home"));
    }

    @Test
    @DisplayName("VD1: Login with raw username fails because VD1 only authenticates by email")
    void vd1LoginWithUsernameFails() throws Exception {
        mockMvc.perform(post("/vd1/login")
                        .with(csrf())
                        .param("email", "user01") // username entered in email field
                        .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vd1/login?error=true"));
    }

    @Test
    @DisplayName("VD1: Login with incorrect password fails")
    void vd1LoginWithWrongPasswordFails() throws Exception {
        mockMvc.perform(post("/vd1/login")
                        .with(csrf())
                        .param("email", "user01@gmail.com")
                        .param("password", "wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vd1/login?error=true"));
    }

    @Test
    @DisplayName("VD1: Login with disabled account fails")
    void vd1LoginWithDisabledAccountFails() throws Exception {
        mockMvc.perform(post("/vd1/login")
                        .with(csrf())
                        .param("email", "locked@example.com")
                        .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vd1/login?error=true"));
    }

    @Test
    @DisplayName("VD1: Access /vd1/admin with authenticated ROLE_ADMIN succeeds")
    void vd1AdminAccessWithRoleAdminSucceeds() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/vd1/login")
                        .with(csrf())
                        .param("email", "trungnh@hcmute.edu.vn")
                        .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vd1/home"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);

        mockMvc.perform(get("/vd1/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("vd1/admin"));
    }

    @Test
    @DisplayName("VD1: Access /vd1/admin with authenticated ROLE_USER is denied (HTTP 403)")
    void vd1AdminAccessWithRoleUserDenied() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/vd1/login")
                        .with(csrf())
                        .param("email", "user01@gmail.com")
                        .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vd1/home"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);

        mockMvc.perform(get("/vd1/admin").session(session))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // VÍ DỤ 2 (VD2) TESTS
    // ==========================================

    @Test
    @DisplayName("VD2: Unauthenticated request to /vd2/home redirects to /vd2/login")
    void vd2UnauthenticatedRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/vd2/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vd2/login"));
    }

    @Test
    @DisplayName("VD2: Login with username succeeds and redirects to /vd2/home")
    void vd2LoginWithUsernameSucceeds() throws Exception {
        mockMvc.perform(post("/vd2/login")
                        .with(csrf())
                        .param("username", "user01")
                        .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vd2/home"));
    }

    @Test
    @DisplayName("VD2: Login with email succeeds and redirects to /vd2/home")
    void vd2LoginWithEmailSucceeds() throws Exception {
        mockMvc.perform(post("/vd2/login")
                        .with(csrf())
                        .param("username", "user01@gmail.com")
                        .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vd2/home"));
    }

    @Test
    @DisplayName("VD2: Access /vd2/admin with authenticated ROLE_ADMIN succeeds")
    void vd2AdminAccessWithRoleAdminSucceeds() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/vd2/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vd2/home"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);

        mockMvc.perform(get("/vd2/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("vd2/admin"));
    }

    @Test
    @DisplayName("VD2: Access /vd2/admin with authenticated ROLE_USER is denied (HTTP 403)")
    void vd2AdminAccessWithRoleUserDenied() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/vd2/login")
                        .with(csrf())
                        .param("username", "user01")
                        .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vd2/home"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);

        mockMvc.perform(get("/vd2/admin").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("VD3: Public login page renders and protected home redirects to VD3 login")
    void vd3LoginAndUnauthenticatedRedirect() throws Exception {
        mockMvc.perform(get("/vd3/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("vd3/auth/login"));
        mockMvc.perform(get("/vd3/register"))
                .andExpect(status().isOk()).andExpect(view().name("vd3/auth/register"));
        mockMvc.perform(get("/vd3/forgot-password"))
                .andExpect(status().isOk()).andExpect(view().name("vd3/auth/forgot-password"));
        mockMvc.perform(get("/vd3/verify-otp"))
                .andExpect(status().isOk()).andExpect(view().name("vd3/auth/verify-otp"));
        mockMvc.perform(get("/vd3/reset-password"))
                .andExpect(status().isOk()).andExpect(view().name("vd3/auth/reset-password"));
        mockMvc.perform(get("/vd3/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vd3/login"));
    }

    @Test
    @DisplayName("VD3: Authenticated user can open dashboard and product list")
    void vd3UserCanOpenOwnPages() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/vd3/login")
                        .with(csrf())
                        .param("username", "user01")
                        .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);
        mockMvc.perform(get("/vd3/home").session(session))
                .andExpect(status().isOk()).andExpect(view().name("vd3/home"));
        mockMvc.perform(get("/vd3/products").session(session))
                .andExpect(status().isOk()).andExpect(view().name("vd3/products/list"));
        mockMvc.perform(get("/vd3/products/create").session(session))
                .andExpect(status().isOk()).andExpect(view().name("vd3/products/form"));
    }

    @Test
    @DisplayName("VD3: Admin user-management pages render")
    void vd3AdminPagesRender() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/vd3/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);
        mockMvc.perform(get("/vd3/users").session(session))
                .andExpect(status().isOk()).andExpect(view().name("vd3/users/list"));
        mockMvc.perform(get("/vd3/users/create").session(session))
                .andExpect(status().isOk()).andExpect(view().name("vd3/users/form"));
    }

    // ==========================================
    // SESSION DÙNG CHUNG & LOGOUT TESTS
    // ==========================================

    @Test
    @DisplayName("Shared Session: Logging in via VD1 allows accessing VD2 home without re-authenticating")
    void sharedSessionAcrossVd1AndVd2() throws Exception {
        // 1. Login at VD1 with email
        MvcResult loginResult = mockMvc.perform(post("/vd1/login")
                        .with(csrf())
                        .param("email", "user01@gmail.com")
                        .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vd1/home"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session, "Session must exist after login");

        // 2. Use same session to visit VD2 home directly
        mockMvc.perform(get("/vd2/home").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("vd2/home"));

        // 3. Logout from VD1
        mockMvc.perform(post("/vd1/logout").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vd1/login?logout=true"));

        // 4. Visiting VD2 home after logout must now redirect to login
        mockMvc.perform(get("/vd2/home").session(session))
                .andExpect(status().is3xxRedirection());
    }

    // ==========================================
    // MAPSTRUCT MAPPER TEST
    // ==========================================

    @Test
    @DisplayName("MapStruct: UserMapper converts User entity to UserDTO correctly without password")
    void testUserMapper() {
        Role role = Role.builder().id(2L).name("ROLE_USER").build();
        User user = User.builder()
                .id(10L)
                .username("testuser")
                .email("test@example.com")
                .password("supersecret_bcrypt_hash")
                .fullName("Test User")
                .images("/images/test.png")
                .enabled(true)
                .role(role)
                .build();

        UserDTO dto = userMapper.toDto(user);

        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("Test User", dto.getFullName());
        assertEquals("ROLE_USER", dto.getRoleName());
        assertEquals(2L, dto.getRoleId());
        assertEquals("/images/test.png", dto.getImages());
        assertTrue(dto.isEnabled());
    }
}
