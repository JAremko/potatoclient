package potatoclient.kotlin.ipc

/**
 * Simple non-blocking tests for IPC components.
 */
object SimpleIpcTest {
    private var testsPassed = 0
    private var testsFailed = 0
    
    @JvmStatic
    fun main(args: Array<String>) {
        println("Running Simple IPC Tests...\n")
        
        testIpcKeys()
        testSocketPathGeneration()
        testServerCreation()
        testClientCreation()
        
        println("\n========================================")
        println("Test Results:")
        println("  Passed: $testsPassed")
        println("  Failed: $testsFailed")
        println("========================================")
        
        System.exit(if (testsFailed > 0) 1 else 0)
    }
    
    private fun testIpcKeys() {
        print("Testing IpcKeys... ")
        try {
            // Test that keywords are created properly
            assert(IpcKeys.MSG_TYPE.toString() == ":msg-type")
            assert(IpcKeys.EVENT.toString() == ":event")
            assert(IpcKeys.COMMAND.toString() == ":command")
            assert(IpcKeys.LOG.toString() == ":log")
            assert(IpcKeys.WINDOW.toString() == ":window")
            assert(IpcKeys.GESTURE.toString() == ":gesture")
            assert(IpcKeys.CONNECTION.toString() == ":connection")
            
            // Test new command actions
            assert(IpcKeys.ROTARY_GOTO_NDC.toString() == ":rotary-goto-ndc")
            assert(IpcKeys.CLOSE_REQUEST.toString() == ":close-request")
            
            // Test helper functions
            assert(IpcKeys.streamType("heat") == IpcKeys.HEAT)
            assert(IpcKeys.streamType("day") == IpcKeys.DAY)
            assert(IpcKeys.logLevel("ERROR") == IpcKeys.ERROR)
            assert(IpcKeys.logLevel("INFO") == IpcKeys.INFO)
            
            passed()
        } catch (e: Exception) {
            failed(e)
        }
    }
    
    private fun testSocketPathGeneration() {
        print("Testing Socket Path Generation... ")
        try {
            val pid = IpcServer.getCurrentPid()
            assert(pid > 0) { "PID should be positive" }
            
            // Test server socket path generation
            val serverPath = IpcServer.generateSocketPath("test-stream")
            assert(serverPath.toString().contains("ipc-$pid-test-stream")) { 
                "Server path should contain PID and stream name"
            }
            
            // Test client socket path generation
            val clientPath = IpcClient.generateSocketPath(12345L, "heat")
            assert(clientPath.toString().contains("ipc-12345-heat")) {
                "Client path should contain server PID and stream name"
            }
            
            passed()
        } catch (e: Exception) {
            failed(e)
        }
    }
    
    private fun testServerCreation() {
        print("Testing Server Creation... ")
        try {
            // Test that server can be created
            val server = IpcServer("test-creation")
            
            // Verify socket path
            val path = server.getSocketPath()
            assert(path.toString().contains("test-creation")) {
                "Socket path should contain stream name"
            }
            
            // Test server registry
            IpcServer.stopAll()  // Clean up first
            
            passed()
        } catch (e: Exception) {
            failed(e)
        }
    }
    
    private fun testClientCreation() {
        print("Testing Client Creation... ")
        try {
            val serverPid = 12345L
            val streamName = "test-client"
            
            // Test client creation
            val socketPath = IpcClient.generateSocketPath(serverPid, streamName)
            val client = IpcClient(socketPath, streamName)
            
            // Verify initial state
            assert(!client.isConnected()) { "Client should not be connected initially" }
            assert(!client.hasCloseRequest()) { "Should not have close request initially" }
            
            // Test callback setting
            var callbackCalled = false
            client.setOnCloseRequest {
                callbackCalled = true
            }
            // Callback is set but not called yet
            assert(!callbackCalled) { "Callback should not be called yet" }
            
            passed()
        } catch (e: Exception) {
            failed(e)
        }
    }
    
    private fun passed() {
        println("PASSED")
        testsPassed++
    }
    
    private fun failed(e: Exception) {
        println("FAILED")
        println("  Error: ${e.message}")
        e.printStackTrace()
        testsFailed++
    }
}