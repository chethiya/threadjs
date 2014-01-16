Thread = (require './thread').Thread
ObjectSocket = (require './object_socket').ObjectSocket
util = (require './util')
logError = util.logError
debug = util.debug

class ForkThread extends Thread
 constructor: (opt) ->
  if opt?
   @parent = opt.parent
   @program = opt.program
   @params = opt.params
   @cwd = opt.cwd

  @spwan = false
  @fork = true if @parent? and @parent
  super()
  @connected = false
  @_connect()

 _connect: ->
  @socket = new ObjectSocket this
  @connected = true
  if not @parent
   @process = process

  @process.on 'message', (m) =>
   @socket.onMessage m

 _send: (data) ->
  @process.send data

 send: (method, data, cb) ->
  if not @connected
   cb "Socket is not connected", {}
   return

  @socket.send \
   {method: method, data: data}, \
   (data) ->
    cb data.err, data.data

exports.ForkThread = ForkThread
