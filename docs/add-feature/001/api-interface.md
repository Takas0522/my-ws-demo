# WebAPI インターフェース

## Point Service
- **エンドポイント**:
  - `GET /points/{userId}`: ユーザーのポイント残高を取得。
  - `GET /points/history/{userId}`: ユーザーのポイント履歴を取得。
  - `POST /points/register`: ポイントを登録（内部向けAPI）。
  - `POST /points/use`: ポイントを利用（内部向けAPI）。
- **データベース**:
  - `points`: ユーザーのポイント残高を管理。
  - `point_history`: ポイント履歴を管理。

## BFF
- **エンドポイント**:
  - `GET /api/points/{userId}`: フロントエンドからポイント残高を取得。
  - `GET /api/points/history/{userId}`: フロントエンドからポイント履歴を取得。

## Auth Service
- **エンドポイント**:
  - `POST /auth-service/api/auth/verify`: JWTトークンの検証。

## User Service
- **エンドポイント**:
  - `GET /user-service/api/users/{id}`: ユーザー情報を取得。