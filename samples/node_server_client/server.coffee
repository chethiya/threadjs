SocketThread = (require './../../socket_thread').SocketThread
util = (require './../../util')
util.setId 'Server'

server = new SocketThread server: true, spawn: false

obj = {id: 2, vendor: 'pamona', amount:12123.22, date: '12312013'}

server.getRecord = (data, callback) ->
 console.log 'server receieved', data
 callback null, obj

 send = (n) ->
  return if n > 10000
  server.send 'onRecord', obj, ->
  call = ->
   send n+1
  setTimeout call, 0
 send 0

server.onStarted (err, data) ->
 console.log "server started : ", err

test = ->
 console.log 'check'
setInterval test, 10000

