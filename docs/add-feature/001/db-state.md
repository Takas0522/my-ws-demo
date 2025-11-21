# ポイント管理機能のDB状態

## 追加されるテーブル

### `points`
ユーザーのポイント残高を管理するテーブル。

| カラム名      | 型          | 説明               |
|---------------|-------------|--------------------|
| id            | INT         | 主キー             |
| user_id       | VARCHAR(36) | ユーザーID (UUID形式、外部キー) |
| balance       | INT         | ポイント残高       |
| created_at    | DATETIME    | 作成日時           |
| updated_at    | DATETIME    | 更新日時           |

---

### `point_history`
ポイントの履歴を管理するテーブル。

| カラム名      | 型          | 説明               |
|---------------|-------------|--------------------|
| id            | INT         | 主キー             |
| user_id       | VARCHAR(36) | ユーザーID (UUID形式、外部キー) |
| operation     | VARCHAR(50) | 操作内容 (取得、使用、失効) |
| points        | INT         | 操作ポイント数（正の整数のみ、operationで加算/減算を判断）     |
| expires_at    | DATETIME    | 失効予定日時       |
| created_at    | DATETIME    | 作成日時           |

---

## 変更箇所
- 新規テーブル `points` と `point_history` を追加。