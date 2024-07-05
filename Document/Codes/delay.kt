sendSmsRunnable = object : Runnable {    override fun run() {
        number = "9126211842"        text = locationAndCellInfo.getCellInfo()
        sendVal = true        sendHandler.post {
            sendSMS(smppClient)        }
        handler.postDelayed(this, 15000) // 15 seconds delay    }
}