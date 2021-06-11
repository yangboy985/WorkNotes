package com.lepu.recure.util

import android.graphics.Rect
import android.view.View
import androidx.annotation.IntRange
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

open class RvLinearDivider(
    protected val rv: RecyclerView,
    @IntRange(from = 0) protected var divider: Int
) : RecyclerView.ItemDecoration() {
    var paddingListStart = 0
    var paddingListEnd = 0 // 存在上拉加载时，这个并不好使
    var paddingListLeftSide = 0
    var paddingListRightSide = 0

    protected val layoutManger: LinearLayoutManager = rv.layoutManager as LinearLayoutManager
    protected var isReverseLayout = false
    protected var isVertical = true

    init {
        isVertical = layoutManger.canScrollVertically()
        isReverseLayout = layoutManger.reverseLayout
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = rv.getChildAdapterPosition(view)
        val layoutParams = getLayoutParams(view)
        layoutParams.leftMargin = 0
        layoutParams.topMargin = 0
        layoutParams.rightMargin = 0
        layoutParams.bottomMargin = 0
        setListRowMargins(layoutParams)
        setListItemStartEndMargins(position, layoutParams)
    }

    private fun setListItemStartEndMargins(position: Int, params: RecyclerView.LayoutParams) {
        setStartMargin(divider, params)
        if (position == 0) {
            setStartMargin(paddingListStart, params)
        }
        addPaddingListEnd(position, params)
    }

    private fun addPaddingListEnd(position: Int, params: RecyclerView.LayoutParams) {
        if (paddingListEnd == 0) {
            return
        }
        val isLastRow = position == rv.adapter!!.itemCount - 1
        if (isLastRow) {
            if (isReverseLayout) {
                if (isVertical) {
                    params.topMargin = paddingListEnd
                } else {
                    params.leftMargin = paddingListEnd
                }
            } else {
                if (isVertical) {
                    params.bottomMargin = paddingListEnd
                } else {
                    params.rightMargin = paddingListEnd
                }
            }
        }
    }

    private fun setStartMargin(margin: Int, params: RecyclerView.LayoutParams) {
        if (isReverseLayout) {
            if (isVertical) {
                params.bottomMargin = margin
            } else {
                params.rightMargin = margin
            }
        } else {
            if (isVertical) {
                params.topMargin = margin
            } else {
                params.leftMargin = margin
            }
        }
    }

    private fun setListRowMargins(params: RecyclerView.LayoutParams) {
        if (isVertical) {
            params.leftMargin = paddingListLeftSide
            params.rightMargin = paddingListRightSide
        } else {
            params.topMargin = paddingListLeftSide
            params.bottomMargin = paddingListRightSide
        }
    }

    private fun getLayoutParams(view: View): RecyclerView.LayoutParams {
        return view.layoutParams as RecyclerView.LayoutParams
    }
}