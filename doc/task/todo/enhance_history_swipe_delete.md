# [TODO] Enhance — History Swipe-to-Delete Animation

> **Ưu tiên:** Medium | **Loại:** Enhancement | **Độ phức tạp:** Medium

## Vấn đề

History list hiện dùng long-press hoặc menu để xóa. Không có swipe gesture. Trải nghiệm kém so với standard Android
patterns.

## Enhancement

- Swipe trái trên history item → reveal "Xóa" action button màu đỏ
- Item slide out với fade animation sau khi confirm xóa
- Snackbar "Đã xóa" + nút "Hoàn tác" (5 giây) để recover

## Implement

- `ItemTouchHelper` với `SimpleCallback` cho RecyclerView trong `FragmentBarcodeHistoryList.kt`
- Background drawable khi swipe: màu đỏ + icon thùng rác
- Animation item removal: `notifyItemRemoved()` với default RecyclerView animation hoặc custom fade-slide

## Files cần sửa

- `feature/tabs/history/FragmentBarcodeHistoryList.kt`
- Adapter history item
