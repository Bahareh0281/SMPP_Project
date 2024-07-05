if (servingpower != null){
    if (servingpower < -50)    {
        for (part in parts) {            val sm = createSubmitSm("989126211842", "98$number", part, "UCS-2")
            println("Try to send message part")            session.submit(sm, TimeUnit.SECONDS.toMillis(50))
            Log.v("smpp", "hello")            println("Message part sent")
        }    }
    else        println("No need to change serving cell")
}