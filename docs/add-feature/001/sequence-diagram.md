# アプリケーション利用プロセス（シーケンス図）

以下は、ポイント管理機能の利用プロセスを示すシーケンス図です。

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant BFF
    participant PointService
    participant AuthService

    User ->> Frontend: ポイント画面にアクセス
    Frontend ->> BFF: APIリクエスト(JWTトークン含む)
    BFF ->> AuthService: JWT検証
    AuthService -->> BFF: 検証結果(ユーザーID含む)
    BFF ->> PointService: ポイントデータ取得(JWTトークン含む)
    PointService ->> AuthService: JWT検証(ユーザーID取得)
    AuthService -->> PointService: 検証結果(ユーザーID)
    PointService ->> PointService: DBからデータ取得
    PointService -->> BFF: データ返却
    BFF -->> Frontend: データ返却
    Frontend -->> User: ポイント情報表示
```

## 認証フローの詳細
- **BFF**: JWTトークンを検証し、リクエストの正当性を確認。
- **PointService**: JWTトークンからユーザーID（UUID形式）を取得し、リクエストパラメータにユーザー情報を含めずにDBアクセスを実施。
- **JWT構造**: JWTペイロードには `userId` クレーム（UUID形式）が必須。
