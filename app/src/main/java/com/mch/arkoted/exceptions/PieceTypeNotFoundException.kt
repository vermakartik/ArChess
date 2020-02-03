package com.mch.arkoted.exceptions

import java.lang.Exception

class PieceTypeNotFoundException(val m: String): Exception() {
    override fun toString(): String {
        return "[PieceTypeException]: $m"
    }
}