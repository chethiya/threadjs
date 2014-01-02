util = require './util'
logError = util.logError
logUser util.logUser

_START = '[START]'
_END = '[END]'
_CALLBACK = '1'
_REQUEST = '0'

class Socket
 constructor: ->
  @host = options.host
  @port = options.port
  @connected = false
  @connectCbs = []
  @messageCb = null
  @_connect()

  @calls = {}
  @queue = []
  @sending = false
  @dataBuffer = ''

 _connect: ->
  logUser "Connecting to the socket #{@host}:#{@port}"
  @socket = net.createConnection(@port, @host)
  @socket.setEncoding 'utf8'
  @socket.on 'connect', ->
   logUser "Connected to the socket #{@host}:#{@port}"
   cb() for cb in @connectCbs

  @socket.on 'data', @_onData

  @socket.on 'error', (error) ->
   logUser "Error connecting to the socket #{@host}:#{@port}"
   setTimeout _connect, 10000

 onConnected: (cb) ->
  if @connected
   cb()
  else
   @connectCbs.push cb

 onMessage: (cb) ->
  @messageCb = cb


 _onData: (data) ->
  @dataBuffer += data.toString()
  @_processBuffer()

 _processBuffer = () ->
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
  else if start > 0
   logError 'Garbage data', @dataBuffer
   @dataBuffer = @dataBuffer.substr start

  if end == -1
   #Loading
   return

  msg = @dataBuffer.substr start + _START.length, end - start - _START.length
  @dataBuffer = @dataBuffer.substr end + _END.length

  @_onResponse msg

 _onResponse = (msg) ->
  data =
   reqId: null
   data: null
  err = null

  # TODO: If callback out of order invoke previous callbacks with error

  reqId = 0
  isCallback = true
  try
   callbackFlag = msg.substr 0, 1
   msg = msg.substr 1
   throw "No callback flag" if callbackFlag.length is 0
   isCallback = false if callbackFlag isnt _CALLBACK

   ind = msg.indexOf '\n'
   throw "No request Id" if ind is -1
   reqId = msg.substr 0, ind
   msg = msg.substr ind+1


   if isCallback
    if calls[reqId]?
     cb = calls[reqId]
     cb err, msg
     delete calls[reqId]
    else
     logError "No callbacked reqId", {reqId: reqId, msg: msg}
   else
    if @messageCb?
     @messageCb err, msg, (data) =>
      data = _START + _CALLABCK + reqId + "\n" + data + _END
      @queue.push data
      @_processQueue()
  catch e
   logError 'Parse Error', e
   logError 'Packet', msg
   err = 'Parse Error'

 send: (data, callback) ->
  reqId = 0
  reqId = util.randomString() while reqId is 0 or @calls[reqId]?

  data = _START + _REQUEST + reqId + "\n" + data + _END

  @calls[reqId] =
   callback: callback
   reqId: reqId

  @queue.push data

  @_processQueue()

 _processQueue = ->
  if @sending
   return

  if queue.length == 0
   return

  @sending = true
  currentReq = queue.shift()

  @socket.write currentReq, 'utf8', =>
   @sending = false
   _processQueue()
