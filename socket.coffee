net = require 'net'
util = require './util'
logError = util.logError
logUser = util.logUser
debug = util.debug

_START = '[START]'
_END = '[END]'

RECONNECT_TIMEOUT = 5000

class Socket
 constructor: (options) ->
  @host = options.host
  @port = options.port
  @server = options.server
  @connected = false
  @connectCbs = []
  @messageCb = null
  @_connect()

  @calls = {}
  @queue = []
  @sending = false
  @dataBuffer = ''

 _createServer: ->
  logUser "Creating the server #{@host}:#{@port}"
  server = net.createServer (socket) =>
   @socket = socket
   @connected = true
   logUser 'client connected'
   @socket.setEncoding 'utf8'
   cb() for cb in @connectCbs

   @socket.on 'data', (data) =>
    @_onData data

   @socket.on 'end', ->
    logUser 'client disconnected'
    @connected = false

  server.listen @port, ->
   logUser 'server bound'

 _connect: ->
  if @server
   @_createServer()
   return

  logUser "Connecting to the socket #{@host}:#{@port}"
  @socket = net.createConnection @port, @host
  @socket.setEncoding 'utf8'
  @socket.on 'connect', =>
   logUser "Connected to the socket #{@host}:#{@port}"
   @connected = true
   cb() for cb in @connectCbs

  @socket.on 'data', (data) =>
   @_onData data

  @socket.on 'error', (error) =>
   logUser "Error connecting to the socket #{@host}:#{@port}"
   recurse = =>
    @_connect()
   setTimeout recurse, RECONNECT_TIMEOUT

 onConnected: (cb) ->
  if @connected
   cb()
  else
   @connectCbs.push cb

 onMessage: (cb) ->
  @messageCb = cb


 _onData: (data) ->
  #TODO socket pause/resume
  @dataBuffer += data.toString()
  @_processBuffer()

 _processBuffer: () ->
  start = @dataBuffer.indexOf _START
  end = @dataBuffer.indexOf _END

  if start == -1 && end == -1
   if @dataBuffer.length > 0
    logError 'Garbage data', @dataBuffer
   @dataBuffer = ''
   return
  else if start == -1 && end != -1
   logError 'No start found', @dataBuffer
   @dataBuffer = ''
   return

  #Start found
  if end != -1 && end < start
   logError 'No start found', @dataBuffer
   @dataBuffer = ''
   return

  if end == -1
   #Loading
   if start > 0
    @dataBuffer = @dataBuffer.substr start
   return

  msg = @dataBuffer.substr start + _START.length, end - start - _START.length
  @dataBuffer = @dataBuffer.substr end + _END.length

  @_onResponse msg

 _onResponse: (msg) ->
  reqId = 0
  isCallback = true
  try
   json = JSON.parse msg

   throw "No callback flag" unless json.callback?
   isCallback = json.callback

   throw "No request Id" unless json.reqId?
   reqId = json.reqId

   throw "No data" unless json.data?

   if isCallback
    if @calls[reqId]?
     cb = @calls[reqId].callback
     if cb?
      cb json.data
     delete @calls[reqId]
    else
     logError "No callbacked reqId", {reqId: reqId, msg: msg}
   else
    if @messageCb?
     @messageCb json.data, (d) =>
      d =
       callback: true
       reqId: reqId
       data: d
      @sendToQueue d
      @_processQueue()
  catch e
   logError 'Parse Error', e.stack
   logError 'Packet', msg
   err = 'Parse Error'

 sendToQueue: (json) ->
  @queue.push _START + (JSON.stringify json) + _END + "\n"

 send: (data, callback) ->
  reqId = 0
  reqId = util.randomString() while reqId is 0 or @calls[reqId]?

  data =
   callback: false
   reqId: reqId
   data: data

  @calls[reqId] =
   callback: callback
   reqId: reqId

  @sendToQueue data
  @_processQueue()

 _processQueue: ->
  if @sending
   return

  if @queue.length == 0
   return

  @sending = true
  currentReq = @queue.shift()

  @socket.write currentReq, 'utf8', =>
   @sending = false
   @_processQueue()


exports.Socket = Socket
