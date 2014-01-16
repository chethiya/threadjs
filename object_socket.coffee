util = require './util'
logError = util.logError
logUser = util.logUser
debug = util.debug


class ObjectSocket
 constructor: (thread) ->
  @calls = {}
  @thread = thread

 onMessage: (msg) ->
  if msg.callback
   if @calls[msg.reqId]
    cb = @calls[msg.reqId].callback
    if cb?
     cb msg.data
    delete @calls[msg.reqId]
   else
    logError "No callback reqId", {msg: msg}
  else
   @thread._onMessage msg.data, (d) =>
    d =
     callback: true
     reqId: msg.reqId
     data: d
    @thread._send d

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

  @thread._send data

exports.ObjectSocket = ObjectSocket
