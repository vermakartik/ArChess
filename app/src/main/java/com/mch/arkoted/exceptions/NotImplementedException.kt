package com.mch.arkoted.exceptions

import java.lang.Exception

class NotImplementedException(var m: String = "") : Exception() {
    override fun toString(): String {
        return "[NotImplementedException]: $m"
    }
}