const tagContainer = document.getElementById('tag-container');
const tagInputField = document.getElementById('tag-input-field');
const tagInputHidden = document.getElementById('tag-input-hidden');

// 現在のタグを管理するためのSet (重複を防ぐ)
const currentTags = new Set();

// テキスト入力欄でキーが押された時のイベント
tagInputField.addEventListener('keydown', function(event) {
    // Enterキーが押されたら
    if (event.key === 'Enter') {
        // 本来のEnterキーの動作（フォーム送信など）をキャンセル
        event.preventDefault();
        
        const tagName = tagInputField.value.trim();

        // 入力が空でない、上限に達していない、かつ重複していない場合のみ追加
        if (tagName && currentTags.size < 5 && !currentTags.has(tagName)) {
            addTag(tagName);
            tagInputField.value = ''; // 入力欄をクリア
        }
    }
});

// タグを追加する関数
function addTag(name) {
    currentTags.add(name); // Setに追加
    
    // タグバッジのHTML要素を作成
    const tagBadge = document.createElement('div');
    tagBadge.classList.add('tag-badge');
    tagBadge.innerHTML = `
        <span>${name}</span>
        <span class="tag-remove-btn" data-name="${name}">×</span>
    `;
    
    // 削除ボタンのイベントリスナーを設定
    tagBadge.querySelector('.tag-remove-btn').addEventListener('click', function() {
        removeTag(name);
    });

    // 画面に表示
    tagContainer.appendChild(tagBadge);
    
    // 隠しフィールドの値を更新
    updateHiddenInput();
}

// タグを削除する関数
function removeTag(name) {
    currentTags.delete(name); // Setから削除

    // 画面から対応するバッジを削除
    const badgeToRemove = tagContainer.querySelector(`.tag-remove-btn[data-name="${name}"]`).parentElement;
    tagContainer.removeChild(badgeToRemove);
    
    // 隠しフィールドの値を更新
    updateHiddenInput();
}

// 隠しフィールドの値を更新する関数
function updateHiddenInput() {
    // Setの内容をカンマ区切りの文字列に変換してセット
    tagInputHidden.value = Array.from(currentTags).join(',');
}