JavaThread = (require './java_thread').JavaThread
util = (require './util')
logUser = util.logUser
util.setId '[NodeJS]'

client = new JavaThread
 server: true
 spawn: true
 cwd: "./java/threadjs"
 program: "java"
 params: [
#  "-Xdebug"
#  "-Xrunjdwp:transport=dt_socket,address=8999,server=y"
  "-cp"
  "target/uber-threadjs-1.0-SNAPSHOT.jar"
  "org.forestpin.threadjs.App"
 ]

#params: ["exec:java", "-Dexec.mainClass=\"org.forestpin.threadjs.App\""],
#params: ["-f=\"java/threadjs/\"", "clean", "install"]

client.onRecord = (data, callback) ->
 logUser data
 callback()

client.onStarted (err, data) ->
 logUser "client started : "

 client.send 'getRecord', 'test', (err, data) ->
  logUser "Data recieved from server thread: #{err}"
  logUser data

