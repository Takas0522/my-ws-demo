# ネットワーク構成図

以下は、ポイント管理機能を含むネットワーク構成図です。

```mermaid
graph TD
    User -->|HTTPS| Frontend
    Frontend -->|REST API| BFF
    BFF -->|REST API| PointService
    PointService -->|REST API| AuthService
    PointService -->|REST API| UserService
    PointService -->|SQL| PostgreSQL
```