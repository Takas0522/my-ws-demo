# BFF の変更内容

## 必要な変更
- 新しいエンドポイント (`/api/points/{userId}`, `/api/points/history/{userId}`) を追加。
- ポイント管理サービスとの通信を行うクライアント (`PointServiceClient`) を作成。