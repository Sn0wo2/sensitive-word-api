package com.github.sn0wo2.word

import com.github.houbb.sensitive.word.api.IWordAllow
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class WordAllow : IWordAllow {
    override fun allow(): List<String?> {
        try {
            return Files.readAllLines(Paths.get("AllowWord.obf"))
        } catch (e: IOException) {
            e.printStackTrace()
            return ArrayList()
        }
    }
}