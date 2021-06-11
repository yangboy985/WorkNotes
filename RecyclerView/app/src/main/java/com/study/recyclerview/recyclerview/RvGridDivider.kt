package com.lepu.recure.util

import android.graphics.Point
import android.graphics.Rect
import android.view.View
import androidx.annotation.IntRange
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 注释以列表垂直向下为参考
 */
class RvGridDivider(
    private val rv: RecyclerView,
    @IntRange(from = 0) val xDivider: Int,
    @IntRange(from = 0) val yDivider: Int
) : RecyclerView.ItemDecoration() {

    var paddingListStart = 0
    var paddingListEnd = 0 // 存在上拉加载时，这个并不好使
    var paddingListLeftSide = 0
    var paddingListRightSide = 0

    private var isReverseLayout = false
    private var isVertical = true
    private var span = 0

    private val layoutManger: GridLayoutManager = rv.layoutManager as GridLayoutManager
    private val margins: MutableList<Point> = mutableListOf()

    private var rvWidth: Int
    private var rvHeight: Int

    init {
        span = layoutManger.spanCount
        isVertical = layoutManger.canScrollVertically()
        isReverseLayout = layoutManger.reverseLayout

        rvWidth = layoutManger.width
        rvHeight = layoutManger.height
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        updateMargins()
        setItemMargins(view)
    }

    private fun setItemMargins(view: View) {
        val position = rv.getChildAdapterPosition(view)
        val layoutParams = getLayoutParams(view)
        layoutParams.leftMargin = 0
        layoutParams.topMargin = 0
        layoutParams.rightMargin = 0
        layoutParams.bottomMargin = 0
        setListRowMargins(layoutParams)
        setListItemStartEndMargins(position, layoutParams)
    }

    private fun setListItemStartEndMargins(position: Int, params: GridLayoutManager.LayoutParams) {
        setStartMargin(if (isVertical) yDivider else xDivider, params)
        if (isFirstRow(position)) {
            setStartMargin(paddingListStart, params)
        }
        addPaddingListEnd(position, params)
    }

    private fun addPaddingListEnd(position: Int, params: GridLayoutManager.LayoutParams) {
        if (paddingListEnd == 0) {
            return
        }
        // 和最后一列（行）的第一个元素比较
        val isLastRow = isLastRow(position)
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

    private fun isFirstRow(position: Int): Boolean {
        if (layoutManger.spanSizeLookup == null) {
            return position < span
        }
        val spanSizeLookup = layoutManger.spanSizeLookup!!
        return spanSizeLookup.getSpanGroupIndex(position, span) == 0
    }

    private fun isLastRow(position: Int): Boolean {
        if (layoutManger.spanSizeLookup == null) {
            return (rv.adapter!!.itemCount + span - 1) / span == (position + span) / span
        }
        val spanSizeLookup = layoutManger.spanSizeLookup!!
        return spanSizeLookup.getSpanGroupIndex(position, span) ==
                spanSizeLookup.getSpanGroupIndex(rv.adapter!!.itemCount - 1, span)
    }

    private fun setStartMargin(margin: Int, params: GridLayoutManager.LayoutParams) {
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

    private fun setListRowMargins(params: GridLayoutManager.LayoutParams) {
        // 每行的第几列
        val marginForLeftSide = margins[params.spanIndex]
        val marginForRightSide = margins[params.spanIndex + params.spanSize - 1]
        if (isVertical) {
            params.leftMargin = marginForLeftSide.x
            params.rightMargin = marginForRightSide.y
        } else {
            params.topMargin = marginForLeftSide.x
            params.bottomMargin = marginForRightSide.y
        }
    }

    private fun getLayoutParams(view: View): GridLayoutManager.LayoutParams {
        return view.layoutParams as GridLayoutManager.LayoutParams
    }

    private fun updateMargins() {
        if ((isVertical && rvWidth == layoutManger.width) || (!isVertical && rvHeight == layoutManger.height)) {
            return
        }
        rvWidth = layoutManger.width
        rvHeight = layoutManger.height
        val totalSpace = if (isVertical) rv.width else rv.height
        val divider = if (isVertical) xDivider else yDivider

        // 这里是GridLayoutManager的计算
        val itemBorders = mutableListOf<Int>()
        calculateItemBorders(itemBorders, totalSpace)

        // 排除divider和padding以后的计算，这里是视觉上的item的真正分布
        val onlyItemTotalSpace =
            totalSpace - paddingListLeftSide - paddingListRightSide - divider * (span - 1)
        val itemSizes = calculateItemBorders(mutableListOf(), onlyItemTotalSpace)
        val itemLocList = calculateItemLoc(itemSizes, divider)

        calculateMargins(itemBorders, itemSizes, itemLocList)
    }

    private fun calculateMargins(
        itemBorders: MutableList<Int>,
        itemSizes: MutableList<Int>,
        itemLocList: MutableList<Int>
    ) {
        margins.clear()
        var margin: Point
        for (i in 0 until span) {
            margin = Point()
            // 相对于grid计算的item区域的margin
            margin.x = itemLocList[i] - itemBorders[i]
            margin.y = itemBorders[i + 1] - (itemLocList[i] + itemSizes[i])
            this.margins.add(margin)
        }
    }

    private fun calculateItemLoc(itemSizes: MutableList<Int>, divider: Int): MutableList<Int> {
        // 左边坐标
        val locList = mutableListOf<Int>()
        var loc = paddingListLeftSide
        for (i in 0 until span) {
            locList.add(loc)
            loc += itemSizes[i] + divider
        }
        return locList
    }

    private fun calculateItemBorders(
        itemBorders: MutableList<Int>,
        totalSpace: Int
    ): MutableList<Int> {
        itemBorders.add(0, 0)
        val sizePerSpan = totalSpace / span
        val sizePerSpanRemainder = totalSpace % span
        var consumedPixels = 0
        var additionalSize = 0
        val itemSizes: MutableList<Int> = mutableListOf()
        for (i in 1..span) {
            var itemSize = sizePerSpan
            additionalSize += sizePerSpanRemainder
            if (additionalSize > 0 && span - additionalSize < sizePerSpanRemainder) {
                itemSize += 1
                additionalSize -= span
            }
            itemSizes.add(itemSize)
            consumedPixels += itemSize
            itemBorders.add(i, consumedPixels)
        }
        return itemSizes
    }
}