package az.rabita.lifestep.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class VerticalSpaceItemDecoration(
    private val verticalSpaceHeightPX: Int,
    private val lastVerticalSpaceHeightPX: Int
) : ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.bottom =
            if (parent.getChildAdapterPosition(view) != parent.adapter?.itemCount!! - 1) verticalSpaceHeightPX
            else lastVerticalSpaceHeightPX
    }

}