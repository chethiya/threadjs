net = require 'net'
Socket = (require './socket').Socket

class JavaThread extends SocketThread
 constructor: (options) ->
  @program = 'java'
  @params = []
  @env = {}

  @port = 11010
  @host = "localhost"

  @port = options.port
  @host = options.host

  @connected = false
  @socket = null

  super()

  @_connect()

 _connect: ->
  @socket = new Socket()
  @socket.onMessage @_onMessage
  @socket.onConnected =>
   @connected = true

 _onMessage: (err, d, callback) ->
  ind = d.indexOf "\n"
  if ind is -1
   logError "Invalid message received", d
   callback JSON.stringify {err: "Invalid message received", data: {}}
   return

  method = d.substr 0, ind
  data = d.substr ind + 1
  data = JSON.parse data

  super null, method: method, data: data, (err, data) ->
   callback JSON.stringify {err: err, data: data}

 send: (method, data, callback) ->
  if not @connected
   callback "Socket is not connected", {}
   return

  msg = "#{method}\n#{JSON.stringify data}"
  @socket.send msg, (err, data) =>
   data = JSON.parse data
   callback err, data

