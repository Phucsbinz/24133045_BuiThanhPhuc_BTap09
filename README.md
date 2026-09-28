# BTAP09 — Spring Security & Cloudinary Multi-Feature Showcase (VD1, VD2, VD3)

**Học phần:** Lập trình Web / Các công nghệ Java  
**Sinh viên thực hiện:** Bùi Thanh Phúc  
**Mã số sinh viên (MSSV):** 24133045  
**Kho lưu trữ GitHub:** [https://github.com/Phucsbinz/24133045_BuiThanhPhuc_BTap09.git](https://github.com/Phucsbinz/24133045_BuiThanhPhuc_BTap09.git)  

---

## 1. Giới thiệu tổng quan

Dự án triển khai trọn vẹn cả **3 ví dụ (VD1, VD2, VD3)** trong **cùng một ứng dụng Spring Boot duy nhất**, sử dụng chung cơ sở dữ liệu **MySQL 8.0** (`btap09_vd12`), chung hệ thống thực thể, bảo mật phân quyền đa lớp và cơ chế **Shared Session**:

| Thành phần | Ví dụ 1 (VD1) | Ví dụ 2 (VD2) | Ví dụ 3 (VD3) |
| :--- | :--- | :--- | :--- |
| **Tiêu chí đăng nhập** | **Email** duy nhất (`email` parameter) | **Username HOẶC Email** | **Username HOẶC Email** |
| **Giao diện View** | Thymeleaf Fragments thuần (`th:replace`) | Thymeleaf Layout Dialect (`layout:decorate`) | Thymeleaf Layout Dialect hiện đại |
| **Đăng ký tài khoản** | Không | Không | **Đăng ký tài khoản + Gửi OTP Email** |
| **Khôi phục mật khẩu** | Không | Không | **Quên mật khẩu + OTP Email + Reset Password** |
| **Quản lý sản phẩm** | Không | Không | **CRUD Sản phẩm + Upload ảnh Cloudinary** (User sở hữu / Admin toàn quyền) |
| **Quản lý người dùng** | Trang Admin xem thông tin cơ bản | Danh sách người dùng & vai trò | **CRUD Người dùng (ADMIN)**, gán quyền, bật/tắt kích hoạt |
| **Cơ chế Session** | Dùng chung Cookie `BTAP09_VD12_SESSION`, giới hạn tối đa 1 phiên/tài khoản |

---

## 2. Công nghệ sử dụng

- **Ngôn ngữ:** Java 26 (tương thích Java 21+)
- **Khung ứng dụng:** Spring Boot 4.1.1
- **Bảo mật:** Spring Security 7.1.x (`SecurityFilterChain`, `DaoAuthenticationProvider`, `BCryptPasswordEncoder`, Method Security)
- **Cơ sở dữ liệu:** MySQL 8.0 (kết nối qua `mysql-connector-j` 9.7.0)
- **Truy vấn & ORM:** Spring Data JPA + Hibernate Core 7.4.x
- **Ánh xạ đối tượng (DTO Mapper):** MapStruct 1.6.3 (`UserMapper`, `ProductMapper`)
- **Giao diện Web:** Thymeleaf 3.x, Thymeleaf Layout Dialect 3.4.x, Thymeleaf Extras Spring Security 6
- **Lưu trữ đám mây:** Cloudinary API (xác thực định dạng JPEG/PNG, dung lượng tối đa 5 MB)
- **Gửi thư điện tử:** Spring Boot Starter Mail / Jakarta Mail (gửi OTP 6 chữ số, hạn 5 phút, giới hạn 5 lần thử)
- **Kiểm thử tự động:** JUnit 5, Spring Security Test, MockMvc (18/18 test cases pass 100%)

---

## 3. Danh sách tài khoản thử nghiệm

Dữ liệu mẫu ban đầu được khởi tạo tự động (chỉ khi bảng `users` trống):

| Username | Email | Mật khẩu ban đầu | Vai trò (Role) | Trạng thái | Mục đích kiểm thử |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `admin` | `trungnh@hcmute.edu.vn` | `123456` | `ROLE_ADMIN` | Đã kích hoạt | Toàn quyền quản trị VD1, VD2, VD3 (Quản lý User & Sản phẩm) |
| `user01` | `user01@gmail.com` | `123456` | `ROLE_USER` | Đã kích hoạt | Người dùng chuẩn (test xem trang User, bị chặn vào Admin 403) |
| `user02` | `phuc.bui@example.com` | `123456` | `ROLE_USER` | Đã kích hoạt | Người dùng chuẩn (test CRUD sản phẩm cá nhân) |
| `disabled_user`| `locked@example.com` | `123456` | `ROLE_USER` | **Bị khóa** (`enabled=0`) | Kiểm thử từ chối đăng nhập với tài khoản bị khóa |

> **Lưu ý bảo mật:** Mật khẩu trong cơ sở dữ liệu đều được mã hóa bằng thuật toán **BCrypt** an toàn.

---

## 4. Danh mục URL & Điểm cuối (Endpoints)

### 4.1. Cổng điều hướng chung (Portal)
- `GET /` — Trang chủ điều hướng chọn 1 trong 3 ví dụ (VD1, VD2, VD3) kèm mô tả tính năng.
- `GET /access-denied` — Trang thông báo lỗi 403 (Không có quyền truy cập).

### 4.2. Ví dụ 1 (Prefix: `/vd1/**`)
- `GET /vd1/login` — Trang đăng nhập (chỉ nhận Email).
- `POST /vd1/login` — Xử lý xác thực email/mật khẩu.
- `GET /vd1/home` — Trang chính người dùng sau đăng nhập.
- `GET /vd1/admin` — Trang quản trị (yêu cầu `ROLE_ADMIN`).
- `POST /vd1/logout` — Đăng xuất và hủy phiên làm việc.

### 4.3. Ví dụ 2 (Prefix: `/vd2/**`)
- `GET /vd2/login` — Trang đăng nhập (nhận Username hoặc Email).
- `POST /vd2/login` — Xử lý xác thực tài khoản.
- `GET /vd2/home` — Trang chính người dùng (hiển thị thông tin phiên, vai trò).
- `GET /vd2/admin` — Trang quản trị danh sách người dùng (yêu cầu `ROLE_ADMIN`).
- `POST /vd2/logout` — Đăng xuất và hủy phiên làm việc.

### 4.4. Ví dụ 3 (Prefix: `/vd3/**`)
- `GET /vd3/login` — Trang đăng nhập VD3.
- `GET /vd3/register` & `POST /vd3/register` — Đăng ký tài khoản mới và gửi mã OTP qua email.
- `GET /vd3/verify-otp` & `POST /vd3/verify-otp` — Xác thực OTP 6 số để kích hoạt tài khoản.
- `POST /vd3/resend-register-otp` — Gửi lại OTP (áp dụng giới hạn giãn cách 60 giây).
- `GET /vd3/forgot-password` & `POST /vd3/forgot-password` — Yêu cầu đặt lại mật khẩu qua OTP email.
- `GET /vd3/reset-password` & `POST /vd3/reset-password` — Nhập OTP và đổi mật khẩu mới.
- `GET /vd3/home` — Bảng điều khiển (Dashboard) thống kê số lượng người dùng và sản phẩm.
- `GET /vd3/products` — Danh sách sản phẩm (tìm kiếm theo tên, phân trang).
- `GET /vd3/products/create` & `POST /vd3/products/create` — Thêm sản phẩm kèm tải ảnh lên Cloudinary.
- `GET /vd3/products/edit/{id}` & `POST /vd3/products/edit/{id}` — Cập nhật sản phẩm & thay ảnh.
- `POST /vd3/products/delete/{id}` — Xóa sản phẩm và dọn ảnh trên Cloudinary.
- `GET /vd3/users` — Quản trị người dùng (chỉ dành cho `ROLE_ADMIN`).
- `GET /vd3/users/create` & `POST /vd3/users/create` — Admin tạo tài khoản mới.
- `GET /vd3/users/edit/{id}` & `POST /vd3/users/edit/{id}` — Admin cập nhật thông tin/vai trò/trạng thái.
- `POST /vd3/users/delete/{id}` — Admin xóa người dùng (chống xóa chính mình và chống xóa Admin cuối cùng).
- `POST /vd3/logout` — Đăng xuất VD3.

---

## 5. Hướng dẫn cài đặt và khởi chạy

### Bước 1: Chuẩn bị cơ sở dữ liệu MySQL
1. Khởi động dịch vụ MySQL Server (cổng mặc định `3306`).
2. Mở MySQL Workbench hoặc terminal client, thực thi câu lệnh tạo database:
   ```sql
   CREATE DATABASE IF NOT EXISTS btap09_vd12 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Chạy script tạo cấu trúc bảng [database/schema.sql](database/schema.sql) (hoặc [database/migrate-vd3.sql](database/migrate-vd3.sql) nếu đã có dữ liệu VD1/VD2).

### Bước 2: Cấu hình biến môi trường
Tạo tệp `.env` tại thư mục gốc của dự án dựa trên mẫu [.env.example](.env.example):
```properties
SERVER_PORT=8091

SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/btap09_vd12?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=ptpn06082006
DDL_AUTO=validate

# Cấu hình gửi mail OTP qua Gmail SMTP
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_gmail@gmail.com
MAIL_PASSWORD=your_gmail_app_password

# Cấu hình lưu trữ ảnh Cloudinary
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

### Bước 3: Biên dịch và chạy ứng dụng

**Cách 1: Sử dụng Maven Wrapper**
```powershell
.\mvnw.cmd spring-boot:run
```

**Cách 2: Chạy trực tiếp qua file JAR đã đóng gói**
```powershell
.\mvnw.cmd package -DskipTests
java -jar target/24133045_BuiThanhPhuc_Security_VD12-1.0.0.jar
```

**Cách 3: Chạy script tự động**
```powershell
.\run.ps1
```

Sau khi ứng dụng khởi động thành công, mở trình duyệt truy cập:
👉 **[http://localhost:8091/](http://localhost:8091/)**

---

## 6. Kết quả kiểm thử (Test Verification)

### 6.1. Kiểm thử tự động (Automated Unit & Integration Tests)
Chạy bộ kiểm thử tự động không ảnh hưởng cơ sở dữ liệu thực (sử dụng in-memory H2):
```powershell
.\mvnw.cmd test
```
**Kết quả:** Toàn bộ **18/18 test cases đạt trạng thái SUCCESS (100%)**:
- Kiểm thử công khai Portal `/`
- Kiểm thử bảo vệ URL chưa đăng nhập tại `/vd1/**`, `/vd2/**`, `/vd3/**`
- Kiểm thử đăng nhập VD1 (chỉ chấp nhận email)
- Kiểm thử đăng nhập VD2 (chấp nhận cả username và email)
- Kiểm thử phân quyền RBAC (Role-Based Access Control): User bị chặn vào Admin (HTTP 403)
- Kiểm thử Shared Session: Đăng nhập tại VD1, tự động truy cập hợp lệ tại VD2 và VD3
- Kiểm thử Logout: Hủy phiên toàn diện trên tất cả các module
- Kiểm thử MapStruct Mappers (`UserMapper`, `ProductMapper`)

### 6.2. Kiểm thử luồng thực tế (Live End-to-End Testing)
- Đăng nhập `user01`: Truy cập `/vd3/home` (200), `/vd3/products` (200), truy cập `/vd3/users` bị chặn với mã 403 Forbidden.
- Đăng nhập `admin`: Truy cập `/vd3/users` thành công (200), thêm mới sản phẩm thành công (302 Redirect), thêm người dùng mới thành công (302 Redirect).
- Đăng ký và OTP: Luồng đăng ký mã OTP 6 số, mã hóa BCrypt, hết hạn sau 5 phút và kích hoạt tài khoản đúng chuẩn.
