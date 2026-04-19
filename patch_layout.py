import xml.etree.ElementTree as ET
import re

file_path = "/Users/loitran/AndroidStudioProjects/@mckimquyen/@playstore/@prodution/@ad/260221_QrAndBarcodeScanner/app/src/main/res/layout/f_scan_barcode_from_camera.xml"
with open(file_path, "r", encoding="utf-8") as f:
    content = f.read()

# Replace constraints of imageViewFlash
content = content.replace(
    'app:layout_constraintEnd_toStartOf="@id/imageViewScanFromFile"',
    'app:layout_constraintEnd_toStartOf="@+id/imageViewBatchScan"'
)
content = content.replace(
    'app:layout_constraintEnd_toStartOf="@id/imageViewFlash"',
    'app:layout_constraintEnd_toStartOf="@+id/imageViewBatchScan"'
) # just in case

# Replace constraints of imageViewScanFromFile
content = content.replace(
    'app:layout_constraintStart_toEndOf="@id/imageViewFlash"',
    'app:layout_constraintStart_toEndOf="@+id/imageViewBatchScan"'
)

# Inject the new views after layoutFlashContainer
batch_scan_views = """
    <androidx.appcompat.widget.AppCompatImageView
        android:id="@+id/imageViewBatchScan"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:layout_constraintEnd_toStartOf="@id/imageViewScanFromFile"
        app:layout_constraintStart_toEndOf="@id/imageViewFlash"
        app:layout_constraintTop_toTopOf="@id/imageViewFlash"
        app:srcCompat="@drawable/ic_scan" />

    <androidx.appcompat.widget.AppCompatTextView
        android:id="@+id/textViewBatchScan"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:text="@string/mode_batch_scan"
        android:textColor="@color/white"
        android:textSize="@dimen/text_size_extra_small"
        android:textStyle="bold"
        app:layout_constraintEnd_toEndOf="@id/imageViewBatchScan"
        app:layout_constraintStart_toStartOf="@id/imageViewBatchScan"
        app:layout_constraintTop_toBottomOf="@id/imageViewBatchScan" />

    <FrameLayout
        android:id="@+id/layoutBatchScanContainer"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintBottom_toBottomOf="@id/textViewBatchScan"
        app:layout_constraintEnd_toEndOf="@id/textViewBatchScan"
        app:layout_constraintStart_toStartOf="@id/textViewBatchScan"
        app:layout_constraintTop_toTopOf="@id/imageViewBatchScan" />
"""

content = content.replace(
    '<androidx.appcompat.widget.AppCompatImageView\n        android:id="@+id/imageViewScanFromFile"',
    batch_scan_views + '\n    <androidx.appcompat.widget.AppCompatImageView\n        android:id="@+id/imageViewScanFromFile"'
)

# Inject RecyclerView Panel
panel_view = """
    <androidx.constraintlayout.widget.ConstraintLayout
        android:id="@+id/layoutBatchListPanel"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_marginTop="50dp"
        android:layout_marginBottom="16dp"
        android:visibility="gone"
        android:background="#DDFFFFFF"
        app:layout_constraintBottom_toTopOf="@id/buttonDecreaseZoom"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toBottomOf="@id/textViewBatchScan">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/recyclerViewBatch"
            android:layout_width="0dp"
            android:layout_height="0dp"
            android:layout_margin="8dp"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintBottom_toTopOf="@+id/buttonExportCsv" />

        <Button
            android:id="@+id/buttonExportCsv"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/action_export_csv"
            android:layout_margin="8dp"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent" />

    </androidx.constraintlayout.widget.ConstraintLayout>

</androidx.constraintlayout.widget.ConstraintLayout>
"""

content = content.replace("</androidx.constraintlayout.widget.ConstraintLayout>", panel_view)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(content)

print("Patched layout xml")
