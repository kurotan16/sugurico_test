// コメント投稿フォームの要素を取得
const commentForm = document.getElementById('comment-form')

// フォームが送信された時の処理を乗っ取る
commentForm.addEventListener('submit', function(event) {
    // 1. 本来のフォーム送信（ページリロード）をキャンセル
    event.preventDefault();

    // 2. フォームのデータを準備
    const formData = new FormData(commentForm);
    const actionUrl = commentForm.action; // フォームのaction属性からURLを取得
    
    // 3. Fetch APIを使ってサーバーに非同期でデータを送信
    fetch(actionUrl, {
        method: 'POST',
        body: formData,
        // CSRFトークンをヘッダーに含める (Spring Securityに必須)
        headers: {
            'X-CSRF-TOKEN': /*[[${_csrf.token}]]*/ 'dummy'
        }
    })
    .then(response => {
        // レスポンスが正常でなければエラーを投げる
        if (!response.ok) {
            throw new Error('サーバーとの通信に失敗しました。');
        }
        // レスポンスをJSONとして解析
        return response.json();
    })
    .then(newComment => {
        // 4. 成功した場合の処理
        console.log('成功:', newComment);
        // コメント一覧に新しいコメントを追加する
        addCommentToDOM(newComment);
        // フォームをリセットする
        commentForm.reset();
    })
    .catch(error => {
        // 5. 失敗した場合の処理
        console.error('エラー:', error);
        alert('コメントの投稿に失敗しました。');
    });
});

// 受け取ったコメントデータを元に、画面に新しいコメント要素を追加する関数
function addCommentToDOM(comment) {
    const commentList = document.getElementById('comment-list');
    const noCommentMessage = document.getElementById('no-comment-message');

    // 「まだコメントはありません」のメッセージを非表示にする
    if (noCommentMessage) {
        noCommentMessage.style.display = 'none';
    }
    
    // 新しいコメントのHTML要素を作成
    const newListItem = document.createElement('li');
    newListItem.style.marginBottom = '15px';
    newListItem.style.borderBottom = '1p solid #ccc';
    newListItem.style.paddingBottom = '10px';

    const formattedDate = new Date(comment.createdAt).toLocaleString('ja-JP');

    newListItem.innerHTML = `
        <strong><span>${comment.user.username}</span></strong>
        <small style="margin-left: 10px; color: #888;">${formattedDate}</small>
        <p style="margin-top: 5px;">${comment.content}</p>
    `;
    
    // コメント一覧の先頭に追加
    commentList.prepend(newListItem);
}