package com.omarltxy5.devicescope

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class Shell {

    private var process: Process? = null
    private var input: BufferedWriter? = null
    private var output: BufferedReader? = null


    var onOutput: ((String) -> Unit)? = null

    fun start() {
        try {

            process = Runtime.getRuntime().exec("sh")


            input = BufferedWriter(OutputStreamWriter(process!!.outputStream))
            output = BufferedReader(InputStreamReader(process!!.inputStream))

            startReading()
        } catch (e: Exception) {
            onOutput?.invoke("Error starting shell: ${e.message}")
        }
    }

    private fun startReading() {
        Thread {
            try {

                while (true) {
                    val line = output?.readLine() ?: break
                    onOutput?.invoke(line)
                }
            } catch (e: Exception) {
                onOutput?.invoke("Read error: ${e.message}")
            }
        }.start()
    }

    fun send(command: String) {
        try {
            input?.let {
                it.write(command)
                it.newLine()
                it.flush()
            }
        } catch (e: Exception) {
            onOutput?.invoke("Send error: ${e.message}")
        }
    }

    fun stop() {
        try {
            input?.close()
            output?.close()
            process?.destroy()
        } catch (e: Exception) {
        }
        fun sanitize(line: String): String {
            return line.replace(
                Regex("\u001B\\[[;\\d}*[-/]*[@-~]), )),
                replacement = ""
        }
    }
}