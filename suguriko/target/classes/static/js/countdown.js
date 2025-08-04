// 残り時間を更新する関数
function updateCountdowns() {
    // "countdown"クラスを持つすべての要素を取得
    const countdownElements = document.querySelectorAll('.countdown');

    countdownElements.forEach(element => {
        // data-created-at属性から投稿日時を取得
        const createdAt = new Date(element.dataset.createdAt);
        
        // 投稿から7日後の日時を計算
        const expiryDate = new Date(createdAt.getTime());
        expiryDate.setDate(createdAt.getDate() + 7);

        // 現在時刻との差を計算 (ミリ秒)
        const now = new Date();
        const diff = expiryDate.getTime() - now.getTime();

        if (diff <= 0) {
            element.textContent = 'このログはタイムラインから消えました';
        } else {
            // ミリ秒を日、時間、分、秒に変換
            const days = Math.floor(diff / (1000 * 60 * 60 * 24));
            const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
            // const seconds = Math.floor((diff % (1000 * 60)) / 1000); // 秒単位まで表示する場合

            // 表示用の文字列を組み立てる
            let remainingText = 'あと ';
            if (days > 0) {
                remainingText += `${days}日 `;
            }
            if (hours > 0 || days > 0) {
                remainingText += `${hours}時間 `;
            }
            remainingText += `${minutes}分で消えます`;

            // 要素のテキストを更新
            element.textContent = remainingText;
        }
    });
}

// ページが読み込まれたら、すぐに一度実行
updateCountdowns();
// その後、1分ごとに繰り返し実行
setInterval(updateCountdowns, 60000); // 60000ミリ秒 = 1分