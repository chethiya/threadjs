spawn = (require 'child_process').spawn
util = require './util'
logError = util.logError
debug = util.debug

PING = "_ping"
START = "_start"
PING_INTERVAL = 1000

class Thread
 constructor: ->
  @spwan = true unless @spawn?
  @server = true unless @server?
  @program = 'coffee' unless @program?
  @params = ["./node_server/server.coffee"] unless @params?
  @env = process.env unless @env?
  @port = 11010 unless @port?
  @host = "localhost" unless @host?
  @cwd = "./" unless @cwd?

  @started = false
  @working = false
  @cbs = []
  @_init()

 _init: ->
  if @spawn
   @process = spawn @program, @params, env: @env, stdio: "inherit", cwd: @cwd

  start = =>
   @send START, {}, (err, data) =>
    started = true
    if err?
     console.log "Spawned program starter error: ", err
    else
     @_setMethods data.methods if data.methods?
     @working = true
    for cb in @cbs
     if not @working
      cb "Started with errors", {}
     else
      cb()

  cid = null
  check = =>
   @send PING, {}, (err, d) ->
    if not err?
     clearInterval cid if cid?
     start()
  check()
  cid = setInterval check, PING_INTERVAL

 _onMessage: (data, callback) ->
  unless data.method?
   logError "No method given", JSON.stringify data
   callback err: "Invalid message received", data: {}
   return

  method = data.method
  data = data.data

  if this[method]?
   this[method] data, (err, data) ->
    callback err: err, data: data
  else
   logError "Invalid method #{method}", data
   callback err: "Invalid method #{method}", data: {}

 _setMethods: (methods) ->
  for m in methods
   this[m] = (d, cb) =>
    @send m, d, (err, data) ->
     cb err, data

 onStarted: (cb) ->
  if @started
   if not @working
    cb "Started with errors", {}
   else
    cb()
   return true
  else
   @cbs.push cb
   return false

 _ping: (data, callback) ->
  callback null, {}

 _start: (data, callback) ->
  callback null, {msg: 'hi'}

exports.Thread = Thread
