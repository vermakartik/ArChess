package com.mch.arkoted.dtypes

import com.mch.arkoted.utils.BoardConfigurator

data class Square(val point: Point, val color: BoardConfigurator.Companion.PieceColor?, val type: BoardConfigurator.Companion.PieceType?, val empty: Boolean)