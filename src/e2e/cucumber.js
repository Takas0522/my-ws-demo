module.exports = {
    default: {
        require: [
            'step-definitions/hooks.ts',       // フック処理（DB初期化など）を最初に読み込む
            'step-definitions/**/*.ts'          // テストスクリプトが格納される場所
        ],
        requireModule: ['ts-node/register'],    // TypeScript実行用の設定
        format: [
            'summary',
            'progress-bar',                     // 実行時にプログレスバーをログ表示する設定
            'html:cucumber-report.html',        // テスト結果をHTMLファイルで出力する設定（tempディレクトリ）
            'json:../../temp/e2e/cucumber-report.json'   // JSON形式でも出力
        ],
        formatOptions: {
            snippetInterface: 'async-await'     // async/await形式のスニペットを生成
        },
        publishQuiet: true,                     // Cucumber結果の公開通知を抑制
        retry: 4                                // 失敗したテストを4回リトライ（サービス起動の不安定性に対処）
    }
}