package com.github.sn0wo2.word

import com.github.houbb.sensitive.word.api.IWordDeny
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class WordDeny : IWordDeny {
    override fun deny(): List<String?> {
        try {
            return Files.readAllLines(Paths.get("DenyWord.obf"))
        } catch (e: IOException) {
            e.printStackTrace()
            return ArrayList()
        }
    }
}