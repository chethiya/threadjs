net = require 'net'
Socket = (require './socket').Socket
Thread = (require './thread').Thread
util = (require './util')
logError = util.logError

class JavaThread extends Thread
 constructor: (options) ->
  @port = options.port
  @host = options.host
  @server = options.server
  @spawn = options.spawn
  @program = options.program
  @params = options.params
  @cwd = options.cwd

  @connected = false
  @socket = null

  super()

  @_connect()

 _connect: ->
  @socket = new Socket host: @host, port: @port, server: @server
  @socket.onMessage (err, data, callback) =>
   @_onMessage err, data, callback
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
   callback data.err, data.data

exports.JavaThread = JavaThread
