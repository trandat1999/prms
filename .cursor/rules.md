# PRMS Rules (bám theo codebase hiện tại)

Mục tiêu của file này là giúp AI phát triển **đúng convention đang có** ở cả Backend (Spring Boot) và Frontend (Angular) trong repo `prms`.

---

## 0) Quy tắc repo & hygiene (bắt buộc)

- **Đường dẫn chuẩn Maven**: chỉ dùng `src/main/java/...` và `src/main/resources/...`.
  - **Không tạo file kiểu Windows path** như `src\main\java\...` (dễ sinh file trùng, như hiện repo đang có).
- **Không commit artefacts/build output**: `target/**`, `dist/**`, `*.class`.
- **Không commit log**: `logs/**` (ví dụ `logs/prms.log`).
- **Không đổi format package**: tất cả backend nằm dưới `com.tranhuudat.prms`.

---

## 1) Backend (Spring Boot) — base hiện tại

### Tech stack (thực tế theo `pom.xml`)

- **Java**: 17
- **Spring Boot**: 3.5.x
- **DB**: PostgreSQL
- **Security**: Spring Security + JWT (jjwt 0.12.x) + OAuth2 client/resource server
- **Docs**: springdoc-openapi 2.x
- **Lombok**: có dùng

---

## 2) Kiến trúc & phân lớp

Luồng chuẩn:

- **controller → service → repository → entity**

Quy tắc:

- **Controller không gọi Repository trực tiếp**.
- **Business logic nằm ở Service**.
- **Repository** chỉ lo query/data access.
- **Entity** phản ánh bảng DB (và audit/base entity theo base hiện có).

---

## 3) Quy ước API/Response/Error

### Unified response

- Tất cả API trả về `BaseResponse` (wrapper thống nhất).
- `BaseResponse` fields chuẩn:
  - `timestamp` (LocalDateTime, format `yyyy-MM-dd hh:mm:ss`)
  - `body`
  - `message`
  - `status`
  - `code`

### Controller style

- Controller trả về `ResponseEntity<BaseResponse>` và **ủy quyền toàn bộ cho Service**.
- HTTP status trong controller nên khớp với response builder ở service (ví dụ create dùng `201`).

### Validation

- **Không dùng `@Valid` ở Controller**.
- Validate request bằng `BaseService.validation(request)` rồi trả `getResponse400(...)` với `errors: Map<field,message>`.

### Exception handling

- Lỗi runtime đi qua `GlobalExceptionHandler`.
- Lỗi nghiệp vụ chủ động: **throw `AppException`** (handler map ra `400`).
- Lỗi enum deserialize: handler trả `400` và `body` là map `{field -> allowed values}`.
- Security errors: handler map `401/403/400` tùy case (JWT expired, bad credentials, access denied…).

---

## 4) i18n message & biến động (backend)

- **Chỉ dùng key i18n** trong code, không hardcode message người dùng.
- Key message nằm trong `SystemMessage`.
- Biến/tên entity dùng cho message nằm trong `SystemVariable`.
- Lấy message bằng `BaseService.getMessage(key, args...)` (MessageSource + LocaleContextHolder).
- File message: `src/main/resources/i18n/messages.properties` (đang cấu hình `spring.messages.basename=i18n/messages`).

---

## 5) DTO/Entity mapping (đúng theo pattern đang dùng)

### DTO rules

- DTO cho domain dùng format tên **`*DTO`** (ví dụ `ProjectDTO`).
- DTO thường **extend `BaseDTO`** khi phù hợp (base hiện có: id/name/description/shortDescription/code).

### Entity → DTO

- Mapping theo **constructor mapping**: `new XxxDTO(entity)`.
- Trong constructor DTO, ưu tiên `BeanUtils.copyProperties(entity, this)` (như base đang làm).

### Foreign key & relation

- Trường FK đại diện bằng `UUID ...Id` trong entity/DTO.
- Nếu cần map relation để đọc thông tin hiển thị: dùng `@ManyToOne(fetch = LAZY)` + `@JoinColumn(insertable=false, updatable=false)` để tránh JPA quản lý FK.

---

## 6) Search/Paging API convention

- Endpoint paging/search dùng **POST**.
- Request body: `{Entity}SearchRequest` và **extends `SearchRequest`** (base fields: `voided/keyword/pageSize/pageIndex`).
- Naming endpoint: theo base hiện tại đang có `POST /api/v1/<entity>/page`.

---

## 7) Repository & Query rules (theo code hiện tại)

- Repository extends `JpaRepository<..., UUID>` và khi cần filter/spec thì thêm `JpaSpecificationExecutor`.
- **Derived query** được phép cho case đơn giản (ví dụ `existsByCode`, `existsByCodeAndIdNot`).
- Query trả DTO/Page DTO:
  - Dùng **JPQL constructor projection**: `select new com.tranhuudat.prms.dto.<...>.XxxDTO(entity) ...`
  - Dùng `@Query` + `countQuery` cho paging.
- Parameter mapping ưu tiên **SpEL**: `:#{#request.keyword}` để hạn chế quá nhiều params.

---

## 8) Service rules (pattern đang dùng)

- Service theo pattern **interface + impl**: `XxxService` / `XxxServiceImpl`.
- `XxxServiceImpl` thường **extends `BaseService`** để dùng:
  - `validation(...)`
  - `getResponse200/201/204/400/404/500`
  - `getMessage(...)`
  - `getPageable(request)`
- Transaction:
  - write: `@Transactional`
  - read: `@Transactional(readOnly = true)`

---

## 9) Code style & utilities

- Dùng Lombok (ví dụ `@RequiredArgsConstructor`), ưu tiên **constructor injection**.
- Không dùng field injection kiểu `@Autowired`.
- Check collection: `CollectionUtils`.
- Check null/equality: `Objects`.
- Logging: **SLF4J** (`log.info/error(...)`), không dùng `System.out.println`.

---

## 10) Security/CORS (đúng theo config hiện tại)

- Public endpoints theo `SecurityConfig.PUBLIC_ENDPOINTS` (không tự ý mở rộng nếu không có yêu cầu).
- JWT filter chạy trước `UsernamePasswordAuthenticationFilter`.
- CORS hiện allow origin `http://localhost:4200` (FE local).

---

## 11) Frontend (Angular) — `prms-web`

### Tech stack (thực tế theo `package.json`)

- **Angular**: 20.x (Standalone components)
- **UI**: NG-ZORRO 20.x
- **i18n**: `@ngx-translate/core`
- **Loading**: `ngx-spinner`
- **RxJS**: 7.8.x
- **Styling**: SCSS + LESS
- **Prettier**:
  - `printWidth: 100`
  - `singleQuote: true`

### Kiến trúc Angular

- **Standalone-first**: không tạo Angular modules cho feature/components.
- Tổ chức theo `core/`, `shared/`, `pages/`, `app.routes.ts` (giữ đúng base hiện có).

### Common Input (bắt buộc)

- **Tất cả input trên UI** (filter/search/form) phải dùng **common input**: `shared/input` (`<app-input ...>` / class `InputCommon`).
- **Không dùng trực tiếp** `nz-input`, `nz-select`, `nz-date-picker`, `nz-input-number` trong `pages/**` trừ khi common input chưa hỗ trợ một case đặc biệt (khi đó phải **nâng cấp** `InputCommon` trước).
- Với date picker cần chọn tháng/năm: dùng `type="date"` và cấu hình `dateMode` (ví dụ `'month'`) trên `app-input`.

### CSS/SCSS rules (ưu tiên dùng style chung)

- **Ưu tiên** dùng:
  - Utility classes của **Bootstrap** (spacing, flex, grid, text, v.v.)
  - CSS/props sẵn có của **NG-ZORRO** (component tokens/classes)
- **Hạn chế** viết CSS riêng trong `*.scss` của component. Chỉ viết khi:
  - UI không thể đạt được bằng Bootstrap/NG-ZORRO; hoặc
  - Cần tinh chỉnh layout/spacing đặc biệt cho màn đó.
- Khi cần override style “bên trong” NG-ZORRO hoặc tránh ảnh hưởng component khác:
  - **Bắt buộc** scope bằng `:host ::ng-deep { ... }` (chỉ cho case đặc biệt, có mục tiêu rõ ràng).

### Tổ chức module (types/enums/const + service/model)

- Với mỗi feature/module trong `pages/<module>/...`:
  - **Tất cả `type`/`enum`/`const` dùng cho module/component** phải đặt trong **file riêng** và nằm trong **folder `models/` của module** (không khai báo rải rác trong file component).
    - Gợi ý naming: `pages/<module>/models/<module>.types.ts`, `pages/<module>/models/<module>.enums.ts`, `pages/<module>/models/<module>.const.ts`.
  - **Service và model** của module phải nằm **ngay trong folder module** (cùng khu vực `pages/<module>/...`), không đặt vào `core/` hoặc `shared/` nếu chỉ phục vụ module đó.
    - Gợi ý structure:
      - `pages/project/models/*`
      - `pages/project/services/*`

### Giao tiếp API

- HTTP calls chỉ nằm trong **services** (core/shared services), component không gọi HttpClient trực tiếp.
- RxJS: ưu tiên `Observable` và xử lý `subscribe`/pipe phù hợp, không chuyển sang Promise tuỳ tiện.

### Template syntax

- Ưu tiên **Angular control flow mới**: `@if`, `@for` (không dùng `*ngIf/*ngFor` nếu không bắt buộc).

---

## 12) Khi thêm mới một feature/domain

Backend:

- Tạo đủ: `Entity` + `Repository` + `Service`/`ServiceImpl` + `Controller` + `DTO` + `SearchRequest` (nếu có page).
- Endpoint trả `BaseResponse` và messages dùng `SystemMessage/SystemVariable`.

Frontend:

- Tạo page standalone trong `pages/<feature>/`.
- Tạo service gọi API trong `core/services` hoặc `shared/...` theo đúng base.
- Format code theo Prettier config trong `package.json`.