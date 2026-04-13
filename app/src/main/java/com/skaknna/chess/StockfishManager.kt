package com.skaknna.chess

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * Simple, robust Stockfish manager using Dispatchers.IO
 * No background threads - just direct I/O with timeout polling
 */
class StockfishManager(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    companion object {
        private const val TAG = "StockfishManager"
        private const val UCI_NEW_GAME = "ucinewgame"
        private const val UCI_SETOPTION = "setoption name %s value %s"
        private const val UCI_POSITION = "position fen %s"
        private const val UCI_GO_DEPTH = "go depth %d"
        private const val UCI_GO_MOVETIME = "go movetime %d"
        private const val UCI_STOP = "stop"
        private const val UCI_QUIT = "quit"
    }

    private var process: Process? = null
    private var inputWriter: BufferedWriter? = null
    private var outputReader: BufferedReader? = null
    var isEngineReady = false
        private set
    private var isAnalyzing = false
    private val analyzeMutex = Mutex()

    data class AnalysisResult(
        val bestMove: String,
        val evaluation: Float,
        val principalVariation: String,
        val depth: Int,
        val nodes: Long
    )

    suspend fun startEngine(): String? = withContext(dispatcher) {
        return@withContext try {
            Log.d(TAG, "=== Starting Stockfish Engine ===")
            
            val binaryFile = getBinaryFile()
            Log.d(TAG, "Binary file path: ${binaryFile.absolutePath}")
            
            if (!binaryFile.exists()) {
                val errorMsg = "CRITICAL: libstockfish.so not found"
                Log.e(TAG, errorMsg)
                return@withContext errorMsg
            }
            Log.d(TAG, "OK: Binary found")

            if (!binaryFile.canExecute()) {
                binaryFile.setExecutable(true)
            }
            Log.d(TAG, "OK: Binary executable")

            // Start process
            Log.d(TAG, "Starting Stockfish process...")
            val processBuilder = ProcessBuilder(binaryFile.absolutePath)
            processBuilder.environment()["LD_LIBRARY_PATH"] = binaryFile.parent ?: ""
            processBuilder.redirectErrorStream(true)
            
            process = processBuilder.start()
            if (process == null) {
                return@withContext "Failed to start process"
            }
            Log.d(TAG, "OK: Process started")

            // Initialize streams
            Log.d(TAG, "Initializing I/O streams...")
            inputWriter = BufferedWriter(OutputStreamWriter(process!!.outputStream))
            outputReader = BufferedReader(InputStreamReader(process!!.inputStream))
            Log.d(TAG, "OK: I/O streams initialized")

            // Send UCI and wait for response
            Log.d(TAG, "Sending 'uci' command...")
            sendCommand("uci")
            
            val ready = waitForResponse("uciok", timeoutMs = 10000)
            if (!ready) {
                Log.e(TAG, "ERROR: Engine did not respond to uci")
                shutdown()
                return@withContext "Engine initialization timeout"
            }

            // Configure options
            sendCommand(String.format(UCI_SETOPTION, "Threads", "2"))
            sendCommand(String.format(UCI_SETOPTION, "Hash", "128"))

            isEngineReady = true
            Log.d(TAG, "OK: Stockfish engine started successfully")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error during startEngine: ${e.message}", e)
            shutdown()
            "Failed to start engine: ${e.message}"
        }
    }

    suspend fun analyzePosition(
    fen: String,
    depth: Int = 15,
    moveTimeMs: Int? = null
    ): AnalysisResult? = withContext(dispatcher) {
        analyzeMutex.withLock {
            Log.d(TAG, "=== Analyzing Position ===")
            
            if (!isEngineReady) {
                Log.e(TAG, "ERROR: Engine not ready for analysis")
                return@withContext null
            }

            if (fen.isBlank()) {
                Log.e(TAG, "ERROR: Invalid FEN provided")
                return@withContext null
            }

            return@withContext try {
                isAnalyzing = true
                
                Log.d(TAG, "FEN: $fen")
                Log.d(TAG, "Depth: $depth")

                sendCommand(UCI_NEW_GAME)
                sendCommand(String.format(UCI_POSITION, fen))

                val goCommand = if (moveTimeMs != null && moveTimeMs > 0) {
                    String.format(UCI_GO_MOVETIME, moveTimeMs)
                } else {
                    String.format(UCI_GO_DEPTH, depth)
                }
                sendCommand(goCommand)

                Log.d(TAG, "Waiting for Stockfish analysis results...")
                val result = parseAnalysisOutput()
                if (result == null) {
                    isEngineReady = false
                }
                result
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e(TAG, "Error analyzing position: ${e.message}", e)
                isEngineReady = false
                null
            } finally {
                isAnalyzing = false
            }
        }
    }

    suspend fun stopAnalysis() = withContext(dispatcher) {
        if (isAnalyzing) {
            sendCommand(UCI_STOP)
            isAnalyzing = false
        }
    }

    suspend fun shutdown() = withContext(dispatcher) {
        try {
            if (isAnalyzing) {
                sendCommand(UCI_STOP)
            }
            if (process != null) {
                sendCommand(UCI_QUIT)
                delay(100)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error during shutdown: ${e.message}")
        } finally {
            try {
                inputWriter?.close()
                outputReader?.close()
            } catch (e: Exception) {
                Log.w(TAG, "Error closing streams: ${e.message}")
            }
            process?.destroy()
            process?.waitFor(2000, TimeUnit.MILLISECONDS)
            
            isEngineReady = false
            isAnalyzing = false
            process = null
            inputWriter = null
            outputReader = null
        }
    }

    // ==================== Private Methods ====================

    private fun getBinaryFile(): File {
        val nativeLibraryDir = context.applicationInfo.nativeLibraryDir
        return File(nativeLibraryDir, "libstockfish.so")
    }

    private suspend fun sendCommand(command: String) = withContext(dispatcher) {
        try {
            inputWriter?.write(command)
            inputWriter?.newLine()
            inputWriter?.flush()
            Log.d(TAG, "UCI Command sent: $command")
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "Error sending command: ${e.message}", e)
        }
    }

    private suspend fun waitForResponse(
        target: String,
        timeoutMs: Long = 3000
    ): Boolean = withContext(dispatcher) {
        val startTime = System.currentTimeMillis()
        Log.d(TAG, "Waiting for '$target' response (timeout: ${timeoutMs}ms)...")
        
        try {
            while (System.currentTimeMillis() - startTime < timeoutMs) {
                ensureActive()
                
                val processDied = try {
                    process?.exitValue()
                    true
                } catch (e: IllegalThreadStateException) {
                    false
                }
                
                if (processDied) {
                    Log.e(TAG, "ERROR: Stockfish process died waiting for '$target'")
                    isEngineReady = false
                    return@withContext false
                }

                if (outputReader?.ready() == true) {
                    val line = outputReader?.readLine()
                    
                    if (line == null) {
                        Log.e(TAG, "ERROR: EOF reached waiting for '$target'")
                        isEngineReady = false
                        return@withContext false
                    }
                    
                    Log.d(TAG, "UCI Response: $line")
                    if (line.contains(target)) {
                        Log.d(TAG, "OK: Found target: '$target'")
                        return@withContext true
                    }
                } else {
                    delay(5)
                }
            }
            
            Log.e(TAG, "ERROR: Timeout waiting for '$target'")
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "ERROR: Exception waiting for response: ${e.message}", e)
        }
        
        false
    }

    private suspend fun parseAnalysisOutput(): AnalysisResult? = withContext(dispatcher) {
        var bestMove = ""
        var evaluation = 0f
        var principalVariation = ""
        var depth = 0
        var nodes = 0L
        val startTime = System.currentTimeMillis()
        val timeoutMs = 30000L
        var lineCount = 0

        try {
            Log.d(TAG, "Starting to parse analysis output...")
            
            while (System.currentTimeMillis() - startTime < timeoutMs) {
                ensureActive()
                
                // Check if process has unexpectedly exited
                val processDied = try {
                    process?.exitValue()
                    true
                } catch (e: IllegalThreadStateException) {
                    false
                }
                
                if (processDied) {
                    Log.e(TAG, "ERROR: Stockfish process died during analysis")
                    isEngineReady = false
                    break
                }

                if (outputReader?.ready() == true) {
                    val line = outputReader?.readLine()
                    if (line == null) {
                        Log.e(TAG, "ERROR: EOF reached during parsing")
                        isEngineReady = false
                        break
                    }
                    
                    lineCount++
                    Log.v(TAG, "[$lineCount] $line")

                    when {
                        line.startsWith("bestmove") -> {
                            val parts = line.split(" ")
                            if (parts.size >= 2) {
                                bestMove = parts[1]
                                Log.d(TAG, "OK: Found bestmove: $bestMove")
                            }
                            break
                        }
                        line.startsWith("info") -> {
                            evaluation = parseEvaluation(line)
                            depth = parseDepth(line)
                            principalVariation = parsePrincipalVariation(line)
                            nodes = parseNodes(line)
                            if (depth > 0) {
                                Log.v(TAG, "Info: depth=$depth eval=$evaluation nodes=$nodes")
                            }
                        }
                    }
                } else {
                    delay(5) // non-blocking wait
                }
            }

            Log.d(TAG, "Finished parsing. Total lines: $lineCount, bestMove: $bestMove")

            return@withContext if (bestMove.isNotEmpty()) {
                AnalysisResult(
                    bestMove = bestMove,
                    evaluation = evaluation,
                    principalVariation = principalVariation,
                    depth = depth,
                    nodes = nodes
                )
            } else {
                Log.e(TAG, "ERROR: No bestmove found in analysis output")
                null
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "ERROR: Error parsing analysis output: ${e.message}", e)
            null
        }
    }

    private val REGEX_EVAL = """score\s+(cp|mate)\s+(-?\d+)""".toRegex()
    private val REGEX_DEPTH = """depth\s+(\d+)""".toRegex()
    private val REGEX_PV = """pv\s+(.+)""".toRegex()
    private val REGEX_NODES = """nodes\s+(\d+)""".toRegex()

    private fun parseEvaluation(line: String): Float {
        return try {
            val matchResult = REGEX_EVAL.find(line)
            if (matchResult != null) {
                val type = matchResult.groupValues[1]
                val score = matchResult.groupValues[2].toInt()
                when (type) {
                    "cp" -> score / 100f
                    "mate" -> if (score > 0) 10f + score else -10f - abs(score)
                    else -> 0f
                }
            } else {
                0f
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing evaluation: ${e.message}")
            0f
        }
    }

    private fun parseDepth(line: String): Int {
        return try {
            REGEX_DEPTH.find(line)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun parsePrincipalVariation(line: String): String {
        return try {
            REGEX_PV.find(line)?.groupValues?.get(1)?.split(" ")?.take(6)?.joinToString(" ") ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun parseNodes(line: String): Long {
        return try {
            REGEX_NODES.find(line)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}