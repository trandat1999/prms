# Project Rules

## Tech Stack
- Java 17
- Spring Boot 3
- PostgreSQL
- Lombok

## Architecture
Follow layered architecture:

controller -> service -> repository -> entity

Never access repository directly from controller.

## API Rules
- Tất cả API trả về JSON đều phải trả về BaseResponse
- API get page hoặc  auto compelete phải là method POST nhận request body là class định dạnh {x}SearchRequest được extend từ SearchRequest

## DTO,ENTITY Rules
- convert từ ENTITY sang DTO sử dụng constructor, bên trong contructor sử dụng BeanUtils của spring boot

## Service Rules
- service được theo interface và impl của interface đó.
- trong interface dữ liệu json trả ra controller đều phải là BaseResponse
- mọi service đều được extend từ BaseService
- sử dụng các hàm dùng chung của BaseService để trả ra dữ liệu
- xử lý validation ở tầng service hàm bên trong BaseService
- getPageable ở trong BaseService

## Repository Rules
- bắt buộc phải có @query, hàm đơn giản thì dùng JPQL, phức tạp thì nhận entitymanager từ service để sử dụng native query, sử dụng addsclar để map sang DTO respone
- nếu dùng JPQL để trả ra dto thì select new com.***.DTO(entity)  
- sử dụng  :#{#filter.**} để lấy parameter chứ không truyền thẳng parameter làm nhiều parameter rối code

## Code RULEs
- các messges được khai báo ở systemMEssage để dùng i18n
- các message biến được khai báo ở systemvariable i18n

## Naming
- Controller: *Controller
- Service: *Service, *ServiceImpl
- Repository: *Repository

## Database
- Use JPA Specification for filtering
- Avoid N+1 queries
- Use fetch join when needed

## Code Style
- Use Lombok annotations
- Use constructor injection
- No field injection

## Logging
Use slf4j logging instead of System.out.