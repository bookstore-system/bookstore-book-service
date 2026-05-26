# Bookstore Book Service

`bookstore-book-service` la microservice quan ly danh muc sach trong he thong Bookstore theo kien truc Microservice.

## 1) Chuc nang da tach tu monolithic

- Quan ly sach: tao, cap nhat, xoa, xem chi tiet.
- Lay danh sach sach co phan trang, ho tro tim kiem theo tu khoa (`keyword`).
- Endpoint internal de verify sach ton tai cho cac service khac.
- Cung cap danh sach tac gia va danh muc.

## 2) Cong nghe

- Java 21
- Spring Boot 4.0.5
- Spring Data JPA
- MySQL 8
- MapStruct + Lombok
- Docker / Docker Compose

## 3) Cau truc API chinh

- `GET /api/v1/books?page=0&size=10&keyword=clean`
- `GET /api/v1/books/{id}`
- `GET /api/v1/books/{id}/verify`
- `POST /api/v1/books`
- `PUT /api/v1/books/{id}`
- `DELETE /api/v1/books/{id}`
- `GET /api/v1/categories`
- `GET /api/v1/authors`

Tat ca endpoint tra ve theo wrapper:

```json
{
  "code": 200,
  "message": "Success",
  "data": {}
}
```

## 4) Chay local (khuyen nghi)

### Cach A: chay bang Maven + MySQL local

1. Tao database:
   - DB name: `bookstore_book`
   - user/password: `bookstore/bookstore`
2. Chay service:
   ```bash
   ./mvnw spring-boot:run
   ```
3. Service mac dinh tai: `http://localhost:8080` (gateway map host port 8082 trong compose).

### Cach B: chay bang Docker Compose

```bash
docker compose up -d
```

Compose se start:
- `book-db` (MySQL)
- `book-service` (map host `8082 -> container 8080`)

## 5) Test service

### Unit/Context test

```bash
./mvnw test
```

### Manual API test nhanh (curl)

Tao sach:

```bash
curl -X POST http://localhost:8082/api/v1/books \
  -H "Content-Type: application/json" \
  -d '{
    "title":"Clean Code",
    "isbn":"9780132350884",
    "price":250000,
    "discountPrice":220000,
    "importPrice":180000,
    "stockQuantity":50,
    "description":"Sach lap trinh",
    "status":"AVAILABLE",
    "authorIds":[],
    "categoryIds":[],
    "imageUrls":["https://example.com/clean-code.jpg"]
  }'
```

Lay danh sach sach:

```bash
curl "http://localhost:8082/api/v1/books?page=0&size=10"
```

Verify sach ton tai:

```bash
curl "http://localhost:8082/api/v1/books/{bookId}/verify"
```

## 6) Luu y tich hop

- Service name: `bookstore-book-service`
- Port convention:
  - container: `8080`
  - host: `8082`
- Database rieng: `bookstore_book`
- Cac service khac (cart/order/review/ai) goi qua API/Gateway, khong truy cap truc tiep DB.
