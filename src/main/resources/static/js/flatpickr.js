// 現在の日付を取得
let maxDate = new Date();
// maxDateを3ヶ月後に設定
maxDate = maxDate.setMonth(maxDate.getMonth() + 3);

// flatpickrを初期化
flatpickr('#fromCheckinDateToCheckoutDate', {
  mode: "range", // 日付範囲選択モード
  locale: 'ja', // 日本語ロケールを使用
  minDate: 'today', // 今日以降の日付を選択可能
  maxDate: maxDate, // 3ヶ月後までの日付を選択可能
  onClose: function(selectedDates, dateStr, instance) {
    // 選択された日付を " から " で分割
    const dates = dateStr.split(" から ");
    if (dates.length === 2) {
      // チェックインとチェックアウトの日付をそれぞれの入力フィールドに設定
      document.querySelector("input[name='checkinDate']").value = dates[0];
      document.querySelector("input[name='checkoutDate']").value = dates[1];
    } else {
      // 日付が選択されていない場合、入力フィールドを空にする
      document.querySelector("input[name='checkinDate']").value = '';
      document.querySelector("input[name='checkoutDate']").value = '';
    }
  }
});