module.exports = {
    default: {
        require: ['step-definitions/**/*.ts'],  // テストスクリプトが格納される場所
        requireModule: ['ts-node/register'],    // TypeScript実行用の設定
        format: [
            'summary',
            'progress-bar',                     // 実行時にプログレスバーをログ表示する設定
            'html:cucumber-report.html'         // テスト結果をHTMLファイルで出力する設定
        ],
        formatOptions: {
            snippetInterface: 'async-await'     // async/await形式のスニペットを生成
        },
        publishQuiet: true                      // Cucumber結果の公開通知を抑制
    }
}