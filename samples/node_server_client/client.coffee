SocketThread = (require './../../socket_thread').SocketThread

client = new SocketThread server: false, spawn: false
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
