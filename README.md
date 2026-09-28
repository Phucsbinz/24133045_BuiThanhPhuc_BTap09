# BÁO CÁO BÀI TẬP 09: SPRING SECURITY 7 & SPRING BOOT 4 (VÍ DỤ 1 & VÍ DỤ 2)

* **Học phần:** Lập trình Web  
* **Giảng viên hướng dẫn:** ThS. Nguyễn Hữu Trung  
* **Sinh viên thực hiện:** Bùi Thanh Phúc  
* **Mã số sinh viên (MSSV):** 24133045  
* **Kho lưu trữ GitHub:** [https://github.com/Phucsbinz/24133045_BuiThanhPhuc_BTap09.git](https://github.com/Phucsbinz/24133045_BuiThanhPhuc_BTap09.git)  
* **Cổng dịch vụ mặc định:** `8091`  
* **Cơ sở dữ liệu (MySQL):** `btap09_vd12`  

---

## 1. Tổng quan dự án

Dự án triển khai bài tập **BTAP09** tích hợp hoàn chỉnh cả hai ví dụ **Ví dụ 1 (VD1)** và **Ví dụ 2 (VD2)** trong cùng **một dự án Spring Boot 4** duy nhất, sử dụng chung cơ sở dữ liệu MySQL, tài khoản người dùng, tầng Service, DTO và MapStruct Mapper, đồng thời giữ nguyên vẹn hai luồng trải nghiệm độc lập theo sát tài liệu hướng dẫn của thầy:

1. **Ví dụ 1 (VD1 - vd1.pdf):**
   * Đăng nhập bảo mật **bắt buộc bằng Email**.
   * Hệ thống giao diện xây dựng hoàn toàn bằng **Thymeleaf Fragments thuần** (`th:fragment`, `th:replace`), không dùng Layout Dialect.
   * Header hiển thị: Họ tên, Email, Huy hiệu vai trò và Form Đăng xuất CSRF.
   * Có trang Quản trị Admin được bảo vệ (chỉ `ROLE_ADMIN` được phép truy cập).

2. **Ví dụ 2 (VD2 - vd2.pdf & trang 20–40 vd1.pdf):**
   * Đăng nhập linh hoạt bằng **Tên đăng nhập (Username) hoặc Email**.
   * Giao diện kế thừa qua **Thymeleaf Layout Dialect** (`layout:decorate`, `layout:fragment`).
   * Sử dụng `CustomUserDetails` lưu trữ trọn vẹn thông tin người dùng: Ảnh đại diện, Họ tên, Username, Email, Role.
   * Header hiển thị ảnh đại diện (kèm cơ chế fallback ảnh mặc định nếu người dùng chưa cập nhật ảnh).
   * Phân quyền quản trị Admin với bảng danh sách người dùng hiển thị ảnh và thông tin chi tiết.

3. **Cổng chọn bài (Portal - `/`):**
   * Trang chủ trung tâm giới thiệu tổng quan, cung cấp liên kết truy cập nhanh đến từng ví dụ.
   * Quản lý phiên đăng nhập dùng chung (**Shared Session**): Khi đăng nhập ở một ví dụ, người dùng có thể tự do xem trang chủ của ví dụ còn lại; khi nhấn Đăng xuất, toàn bộ phiên làm việc của ứng dụng sẽ được hủy an toàn.

---

## 2. Nền tảng công nghệ

* **Ngôn ngữ:** Java 26 (Tương thích Java 21+ LTS)
* **Framework:** Spring Boot 4.1.1
* **Bảo mật:** Spring Security 7 (Theo BOM chuẩn của Spring Boot 4.1.1)
* **Cơ sở dữ liệu:** MySQL 8.0 (Bảng mã `utf8mb4_unicode_ci`, driver `com.mysql:mysql-connector-j`)
* **ORM:** Spring Data JPA & Hibernate ORM (Cấu hình `ddl-auto = update`, `open-in-view = false`)
* **Mapper:** MapStruct 1.6.3 (`mapstruct-processor` + `lombok-mapstruct-binding:0.2.0`) - *Tự động sinh implementation `UserMapperImpl` trong quá trình biên dịch*
* **Template Engine:** Thymeleaf 3 + `thymeleaf-layout-dialect` + `thymeleaf-extras-springsecurity6`
* **Công cụ hỗ trợ:** Lombok, Jakarta Validation, Maven Wrapper (`mvnw`)
* **Kiểm thử tự động:** JUnit 5, Spring Security Test, Spring Boot Test, H2 In-Memory DB

---

## 3. Bảng so sánh 2 luồng chức năng trong cùng ứng dụng

| Tiêu chí | Ví dụ 1 (VD1) | Ví dụ 2 (VD2) |
| :--- | :--- | :--- |
| **URL Đăng nhập** | `GET /vd1/login` | `GET /vd2/login` |
| **Xử lý Đăng nhập** | `POST /vd1/login` | `POST /vd2/login` |
| **Tên trường input** | `name="email"` | `name="username"` |
| **Quy tắc xác thực** | **Chỉ chấp nhận Email** (Nhập username thuần sẽ bị từ chối) | **Chấp nhận Username hoặc Email** |
| **AuthenticationProvider** | `DaoAuthenticationProvider` gắn `Vd1UserDetailsService` | `DaoAuthenticationProvider` gắn `Vd2UserDetailsService` |
| **URL Trang chủ** | `/vd1/home` (Yêu cầu đăng nhập) | `/vd2/home` (Yêu cầu đăng nhập) |
| **Cơ chế Layout View** | **Thymeleaf Fragments thuần** (`th:fragment`, `th:replace`) | **Thymeleaf Layout Dialect** (`layout:decorate`, `layout:fragment`) |
| **Thông tin trên Header** | Họ tên, Email, Role badge, Nút Đăng xuất | Ảnh đại diện (fallback), Họ tên, Username, Email, Role badge, Đăng xuất |
| **URL Quản trị Admin** | `/vd1/admin` (Chỉ `ROLE_ADMIN`, `ROLE_USER` nhận 403) | `/vd2/admin` (Chỉ `ROLE_ADMIN`, `ROLE_USER` nhận 403) |
| **Xử lý Đăng xuất** | `POST /vd1/logout` (Hủy session, xóa cookie) | `POST /vd2/logout` (Hủy session, xóa cookie) |

---

## 4. Danh sách tài khoản kiểm thử (Demo Credentials)

> **Mật khẩu dùng chung cho tất cả tài khoản mẫu:** `123456` (Được băm bằng BCrypt trong CSDL).

| Loại tài khoản | Username | Email | Vai trò (Role) | Ảnh đại diện | Kịch bản kiểm chứng |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Quản trị viên (Admin)** | `admin` | `trungnh@hcmute.edu.vn` | `ROLE_ADMIN` | `/images/user.png` | • Đăng nhập được bằng Email (VD1)<br>• Đăng nhập được bằng Username hoặc Email (VD2)<br>• Truy cập thành công `/vd1/admin` và `/vd2/admin`. |
| **Người dùng 01** | `user01` | `user01@gmail.com` | `ROLE_USER` | `/images/user.png` | • Tài khoản mẫu chuẩn theo tài liệu của thầy.<br>• Khi vào `/vd1/admin` hoặc `/vd2/admin` sẽ bị chuyển về trang `403 Forbidden`. |
| **Người dùng 02** | `user02` | `phuc.bui@example.com` | `ROLE_USER` | `null` | • Kiểm thử tính năng **fallback ảnh mặc định** (`/images/avatar-default.png`) trên Header VD2 khi trường ảnh bị rỗng. |
| **Tài khoản bị khóa** | `disabled_user` | `locked@example.com` | `ROLE_USER` | `null` | • Kiểm thử thuộc tính `enabled = false`. Đăng nhập tại cả VD1 và VD2 đều bị từ chối với thông báo lỗi. |

---

## 5. Cấu trúc mã nguồn dự án

```text
24133045_BuiThanhPhuc_Security_VD12/
├── database/
│   ├── schema.sql                      # DDL tạo CSDL btap09_vd12, bảng roles, users và khóa ngoại
│   └── sample-data.sql                 # DML nạp dữ liệu mẫu (Roles, Admin, Users với mật khẩu BCrypt)
├── src/
│   ├── main/
│   │   ├── java/vn/iotstar/
│   │   │   ├── Application.java        # Class khởi chạy Spring Boot chính
│   │   │   ├── config/
│   │   │   │   ├── DataInitializer.java  # Tự động khởi tạo Roles và Users mẫu không trùng lặp
│   │   │   │   ├── EncodingConfig.java   # Bộ lọc CharacterEncodingFilter chuẩn UTF-8
│   │   │   │   └── SecurityConfig.java   # Cấu hình đa luồng SecurityFilterChain (@Order 1, 2, 3)
│   │   │   ├── controller/
│   │   │   │   ├── PortalController.java # Điều hướng trang chủ /, trang 403 Access Denied
│   │   │   │   ├── Vd1Controller.java    # Điều hướng VD1: /vd1/login, /vd1/home, /vd1/admin
│   │   │   │   └── Vd2Controller.java    # Điều hướng VD2: /vd2/login, /vd2/home, /vd2/admin
│   │   │   ├── dto/
│   │   │   │   ├── LoginDTO.java         # DTO dữ liệu biểu mẫu đăng nhập
│   │   │   │   └── UserDTO.java          # DTO thông tin người dùng (không chứa password)
│   │   │   ├── entity/
│   │   │   │   ├── Role.java             # Thực thể bảng roles (ROLE_ADMIN, ROLE_USER)
│   │   │   │   └── User.java             # Thực thể bảng users (quan hệ @ManyToOne với Role)
│   │   │   ├── mapper/
│   │   │   │   └── UserMapper.java       # MapStruct Interface ánh xạ Entity <-> UserDTO
│   │   │   ├── repository/
│   │   │   │   ├── RoleRepository.java   # Thao tác dữ liệu Role
│   │   │   │   └── UserRepository.java   # Thao tác dữ liệu User (eager fetch role, query username/email)
│   │   │   ├── security/
│   │   │   │   ├── CustomUserDetails.java   # Custom Principal chứa id, username, email, fullName, images, role
│   │   │   │   ├── Vd1UserDetailsService.java # Xác thực chỉ qua email cho luồng VD1
│   │   │   │   └── Vd2UserDetailsService.java # Xác thực qua username hoặc email cho luồng VD2
│   │   │   └── service/
│   │   │       ├── UserService.java
│   │   │       └── impl/UserServiceImpl.java # Triển khai nghiệp vụ, sử dụng MapStruct UserMapper
│   │   └── resources/
│   │       ├── application.properties    # Cấu hình MySQL, Server Port 8091, Session Cookie
│   │       ├── static/
│   │       │   ├── css/app.css           # Toàn bộ CSS phong cách chuẩn theo tài liệu của thầy
│   │       │   └── images/               # Ảnh tĩnh: user.png, avatar-default.png
│   │       └── templates/
│   │           ├── index.html            # Cổng chọn bài (Portal)
│   │           ├── error/403.html        # Giao diện thông báo lỗi truy cập trái quyền HTTP 403
│   │           ├── vd1/                  # Giao diện VD1 (Thymeleaf Fragments thuần)
│   │           │   ├── layouts/layout.html
│   │           │   ├── fragments/header.html, footer.html
│   │           │   ├── auth/login.html
│   │           │   ├── home.html
│   │           │   └── admin.html
│   │           └── vd2/                  # Giao diện VD2 (Thymeleaf Layout Dialect)
│   │               ├── layouts/layout.html
│   │               ├── fragments/header.html
│   │               ├── auth/login.html
│   │               ├── home.html
│   │               └── admin.html
│   └── test/
│       ├── java/vn/iotstar/
│       │   └── SecurityFlowIntegrationTest.java # 15 kiểm thử tự động toàn diện qua Security Filter Chain
│       └── resources/
│           └── application-test.properties       # Cấu hình H2 in-memory phục vụ kiểm thử cô lập
├── .env.example                        # Mẫu biến môi trường
├── .gitignore                          # Loại trừ thư mục build, cấu hình nhạy cảm và IDE
├── mvnw & mvnw.cmd                     # Maven Wrapper chính thức
├── pom.xml                             # Định nghĩa thư viện Spring Boot 4.1.1, Java 26, MapStruct
├── run.ps1                             # Script tự động nhận diện JDK 26, kiểm tra MySQL và khởi chạy
└── README.md                           # Tài liệu hướng dẫn chi tiết
```

---

## 6. Hướng dẫn cài đặt và khởi chạy

### Cách 1: Sử dụng Script tự động `run.ps1` (Khuyên dùng)

Mở PowerShell tại thư mục dự án và thực thi:

```powershell
.\run.ps1
```

Script sẽ tự động:
1. Nhận diện bộ cài đặt JDK 26 trên máy (`C:\Program Files\Java\jdk-26.0.2.1` hoặc biến môi trường `JAVA_HOME`).
2. Tự động kiểm tra và khởi tạo CSDL `btap09_vd12` trên MySQL Server.
3. Biên dịch dự án bằng Maven Wrapper và chạy ứng dụng tại `http://localhost:8091`.

---

### Cách 2: Khởi chạy thủ công qua lệnh Maven

1. **Thiết lập môi trường Java 26:**
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Java\jdk-26.0.2.1"
   $env:Path = "$env:JAVA_HOME\bin;$env:Path"
   ```

2. **Khởi tạo cơ sở dữ liệu MySQL:**
   Chạy tệp `database/schema.sql` và `database/sample-data.sql` bằng MySQL Client hoặc MySQL Workbench.

3. **Chạy kiểm thử tự động:**
   ```powershell
   .\mvnw.cmd clean test
   ```
   *(Kết quả: 15/15 kịch bản kiểm thử pass 100%)*

4. **Đóng gói và khởi chạy JAR:**
   ```powershell
   .\mvnw.cmd clean package -DskipTests
   java -jar target\24133045_BuiThanhPhuc_Security_VD12-1.0.0.jar
   ```

5. **Truy cập hệ thống:**
   * Cổng chọn bài: [http://localhost:8091/](http://localhost:8091/)
   * Ví dụ 1 (VD1): [http://localhost:8091/vd1/home](http://localhost:8091/vd1/home) (Đăng nhập tại: `/vd1/login`)
   * Ví dụ 2 (VD2): [http://localhost:8091/vd2/home](http://localhost:8091/vd2/home) (Đăng nhập tại: `/vd2/login`)

---

## 7. Các điểm tối ưu và sửa lỗi so với code mẫu trong PDF

Trong quá trình triển khai thực tế trên **Spring Boot 4.1.1** và **Spring Security 7**, dự án đã chủ động xử lý và tối ưu hóa các điểm hạn chế trong tài liệu mẫu của thầy:

1. **Khắc phục lỗi khởi tạo `DaoAuthenticationProvider` trên Spring Security 7:**
   * Trong Spring Security 7, phương thức setter `setUserDetailsService` và constructor không tham số đã bị hạn chế/thay thế bằng constructor injection bắt buộc: `new DaoAuthenticationProvider(userDetailsService)`. Dự án đã áp dụng đúng chuẩn mới nhất.

2. **Cách ly AuthenticationProvider giữa 2 Filter Chain:**
   * Nếu khai báo cả 2 Provider làm `@Bean` toàn cục, Spring Security sẽ gộp cả 2 vào `AuthenticationManager` cha, dẫn đến việc uỷ quyền lặp vòng (StackOverflowError). Dự án đã cô lập `DaoAuthenticationProvider` trực tiếp bên trong từng `SecurityFilterChain` tương ứng (`/vd1/**` và `/vd2/**`), giúp hai luồng hoạt động độc lập và ổn định tuyệt đối.

3. **Xử lý xung đột Bean `characterEncodingFilter`:**
   * Trong Spring Boot, `HttpEncodingAutoConfiguration` mặc định đã tạo một bean tên `characterEncodingFilter`. Việc khai báo thêm bean trùng tên trong `EncodingConfig` sẽ gây ra lỗi `BeanDefinitionOverrideException`. Dự án đã kích hoạt `spring.main.allow-bean-definition-overriding=true` và đổi tên bean đăng ký thành `customCharacterEncodingFilter`.

4. **Tương thích Thymeleaf Security Expression:**
   * Cú pháp gọi class tĩnh `T(...)` bị chặn trong môi trường sandbox của SpEL trên Spring Boot 4. Dự án đã chuẩn hóa các biểu thức SpEL trên Header để truy xuất trực tiếp các thuộc tính của `CustomUserDetails` (`fullName`, `email`, `username`, `images`, `role`) mà không gây ngoại lệ phân tích mẫu.

5. **Chuyển đổi hoàn hảo sang MySQL:**
   * Bỏ các khai báo phụ thuộc SQL Server `columnDefinition = "nvarchar(...)"`, thay thế bằng kiểu chuỗi chuẩn tương thích MySQL với bảng mã `utf8mb4_unicode_ci` hỗ trợ đầy đủ tiếng Việt có dấu.

6. **Loại bỏ các thành phần dở dang của VD3:**
   * Bỏ toàn bộ các liên kết và tham chiếu chưa hoàn thiện trong code mẫu như `CategoryRepository`, `Product`, Cloudinary, Quên mật khẩu/OTP để đảm bảo 100% không có liên kết chết (dead link) hoặc lỗi 404/500.

---

## 8. Kết quả kiểm thử thực tế

### A. Kiểm thử tự động (JUnit & Spring Security Integration Tests)
Dự án bao gồm 15 bài kiểm thử tự động toàn diện kiểm tra mọi khía cạnh bảo mật:
* `portalPageIsPublic()`: Cổng chọn bài mở công khai.
* `vd1UnauthenticatedRedirectsToLogin()`: Chưa đăng nhập khi vào `/vd1/home` sẽ bị chuyển hướng đến `/vd1/login`.
* `vd1LoginWithValidEmailSucceeds()`: Đăng nhập thành công với Email hợp lệ.
* `vd1LoginWithUsernameFails()`: Đăng nhập bằng username tại VD1 bị từ chối chính xác.
* `vd1LoginWithWrongPasswordFails()`: Mật khẩu sai bị từ chối.
* `vd1LoginWithDisabledAccountFails()`: Tài khoản bị khóa (`enabled = false`) bị từ chối.
* `vd1AdminAccessWithRoleAdminSucceeds()`: Tài khoản ADMIN truy cập `/vd1/admin` thành công.
* `vd1AdminAccessWithRoleUserDenied()`: Tài khoản USER truy cập `/vd1/admin` nhận mã lỗi HTTP 403 Forbidden.
* `vd2UnauthenticatedRedirectsToLogin()`: Chưa đăng nhập khi vào `/vd2/home` sẽ chuyển về `/vd2/login`.
* `vd2LoginWithUsernameSucceeds()`: Đăng nhập bằng Username tại VD2 thành công.
* `vd2LoginWithEmailSucceeds()`: Đăng nhập bằng Email tại VD2 thành công.
* `vd2AdminAccessWithRoleAdminSucceeds()`: ADMIN vào `/vd2/admin` thành công.
* `vd2AdminAccessWithRoleUserDenied()`: USER vào `/vd2/admin` bị chặn 403.
* `sharedSessionAcrossVd1AndVd2()`: Đăng nhập tại VD1, dùng chung session truy cập ngay VD2; Đăng xuất làm mất quyền cả 2 bên.
* `testUserMapper()`: MapStruct ánh xạ chính xác Entity sang `UserDTO` không chứa mật khẩu.

**Kết quả thực thi:**
```text
[INFO] Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---
*Bản quyền dự án thuộc về sinh viên Bùi Thanh Phúc - MSSV: 24133045 - ĐH Sư Phạm Kỹ Thuật TP.HCM (HCMUTE)*
