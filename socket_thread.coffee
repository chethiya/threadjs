net = require 'net'
Socket = (require './socket').Socket
Thread = (require './thread').Thread
util = (require './util')
logError = util.logError

class SocketThread extends Thread
 constructor: (options) ->
  if options?
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
  @socket.onMessage (data, callback) =>
   @_onMessage data, callback
  @socket.onConnected =>
   @connected = true

 send: (method, data, callback) ->
  if not @connected
   callback "Socket is not connected", {}
   return

  msg =
   method: method
   data: data

  @socket.send msg, (data) =>
   callback data.err, data.data

exports.SocketThread = SocketThread
