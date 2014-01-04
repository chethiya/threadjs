JavaThread = (require './java_thread').JavaThread

client = new JavaThread server: true, spawn: true, cwd: "./java/threadjs", program: "java", params: ["-cp", "target/uber-threadjs-1.0-SNAPSHOT.jar", "org.forestpin.threadjs.App"]

#params: ["exec:java", "-Dexec.mainClass=\"org.forestpin.threadjs.App\""],
#params: ["-f=\"java/threadjs/\"", "clean", "install"]

client.onRecord = (data, callback) ->
 console.log data
 callback()

client.onStarted (err, data) ->
 console.log "client started : ", err

 client.send 'getRecord', 'test', (err, data) ->
  console.log "Data recieved from server thread", err, data

test = ->
 console.log 'check'
setInterval test, 10000
