package com.mch.arkoted.exceptions

import java.lang.Exception

class PieceColorException(val m: String = ""): Exception() {
    override fun toString(): String {
        return "[PieceColorException]: $m"
    }
}